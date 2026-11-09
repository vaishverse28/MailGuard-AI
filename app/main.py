import logging

from fastapi import FastAPI
from fastapi.responses import JSONResponse

from app.classifier import ClassificationError, classify_email
from app.fallback import fallback_classify
from app.schemas import AnalysisResponse, EmailRequest

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("mailguard")

app = FastAPI(title="MailGuard AI Service")


@app.get("/")
def health():
    return {"message": "MailGuard AI service is running"}


@app.post("/analyze", response_model=AnalysisResponse)
def analyze(email: EmailRequest):
    # 1. Try the LLM first
    try:
        result = classify_email(email)
        logger.info("LLM result: %s (%s)", result.status, result.riskScore)
        return result
    except ClassificationError as e:
        logger.warning("LLM failed, switching to keyword fallback: %s", e)

    # 2. LLM failed -> rule-based fallback
    try:
        result = fallback_classify(email)
        logger.info("Fallback result: %s (%s)", result.status, result.riskScore)
        return result
    except Exception as e:
        logger.error("Fallback also failed: %s", e)

    # 3. Everything failed -> 503 as required by the spec
    return JSONResponse(
        status_code=503,
        content={
            "error": "SERVICE_UNAVAILABLE",
            "message": "Email analysis is temporarily unavailable. Please try again later.",
        },
    )