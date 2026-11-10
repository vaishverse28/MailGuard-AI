import json
import os
import time

from dotenv import load_dotenv
from google import genai
from google.genai import types

from app.prompts import SYSTEM_PROMPT, build_user_prompt
from app.schemas import AnalysisResponse, EmailRequest

load_dotenv()

VALID_STATUSES = {"SAFE", "SPAM", "PROMOTIONAL", "PHISHING"}

# Risk score range allowed for each category (from the spec)
SCORE_RANGES = {
    "SAFE": (0, 30),
    "PROMOTIONAL": (20, 50),
    "SPAM": (50, 85),
    "PHISHING": (75, 100),
}

MAX_ATTEMPTS = 2 # 1 first try + 1 retries


class ClassificationError(Exception):
    """Raised when the LLM fails or returns unusable output."""


_client = None


def _get_client():
    global _client
    if _client is None:
        api_key = os.getenv("LLM_API_KEY")
        if not api_key:
            raise ClassificationError("LLM_API_KEY is missing in .env")
        _client = genai.Client(
            api_key=api_key,
            http_options=types.HttpOptions(timeout=30000),  # 30 seconds
        )
    return _client


def _extract_json(text: str) -> dict:
    """Parse JSON even if the model wrapped it in ```json fences."""
    text = text.strip()
    start = text.find("{")
    end = text.rfind("}")
    if start == -1 or end == -1:
        raise ValueError("No JSON object found in response")
    return json.loads(text[start : end + 1])


def _validate(data: dict) -> AnalysisResponse:
    status = str(data.get("status", "")).strip().upper()
    if status not in VALID_STATUSES:
        raise ValueError(f"Invalid status: {status!r}")

    score = int(round(float(data.get("riskScore"))))
    score = max(0, min(100, score))  # clamp to 0-100
    low, high = SCORE_RANGES[status]
    score = max(low, min(high, score))  # clamp to the category range

    words = data.get("suspiciousWords") or []
    words = [str(w) for w in words][:10]

    explanation = str(data.get("explanation", "")).strip()
    if not explanation:
        raise ValueError("Missing explanation")

    recs = data.get("recommendations") or []
    recs = [str(r) for r in recs][:4]
    if not recs:
        raise ValueError("Missing recommendations")

    return AnalysisResponse(
        status=status,
        riskScore=score,
        suspiciousWords=words,
        explanation=explanation,
        recommendations=recs,
    )

def classify_email(email: EmailRequest) -> AnalysisResponse:
    primary = os.getenv("LLM_MODEL", "gemini-2.5-flash")
    backup = os.getenv("LLM_BACKUP_MODEL")  # optional
    prompt = build_user_prompt(email.senderEmail, email.subject, email.body)
    last_error = None

    for attempt in range(1, MAX_ATTEMPTS + 1):
        # Use the backup model on the last attempt, if one is configured
        model = backup if (backup and attempt == MAX_ATTEMPTS) else primary
        try:
            response = _get_client().models.generate_content(
                model=model,
                contents=prompt,
                config=types.GenerateContentConfig(
                    system_instruction=SYSTEM_PROMPT,
                    temperature=0.0,
                    response_mime_type="application/json",
                    thinking_config=types.ThinkingConfig(thinking_budget=0),
                ),
            )
            data = _extract_json(response.text)
            return _validate(data)
        except Exception as e:
            last_error = e
            print(f"[classifier] attempt {attempt} ({model}) failed: {e}")
            if attempt < MAX_ATTEMPTS:
                time.sleep(2 * attempt)  # wait 2s, then 4s

    raise ClassificationError(f"LLM failed after {MAX_ATTEMPTS} attempts: {last_error}")
