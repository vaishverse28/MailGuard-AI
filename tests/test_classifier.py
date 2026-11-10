import json
import sys
import time
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from fastapi.testclient import TestClient

from app.fallback import fallback_classify
from app.main import app
from app.schemas import EmailRequest

SCORE_RANGES = {
    "SAFE": (0, 30),
    "PROMOTIONAL": (20, 50),
    "SPAM": (50, 85),
    "PHISHING": (75, 100),
}

REQUIRED_FIELDS = {"status", "riskScore", "suspiciousWords", "explanation", "recommendations"}


def run(use_fallback_only: bool = False):
    samples = json.loads((ROOT / "tests" / "sample_emails.json").read_text(encoding="utf-8"))
    client = TestClient(app)

    lines = []
    passed = 0
    mode = "FALLBACK ONLY" if use_fallback_only else "FULL (LLM + fallback)"
    header = f"MailGuard AI test run - {datetime.now():%Y-%m-%d %H:%M:%S} - mode: {mode}"
    lines.append(header)
    lines.append("-" * 95)

    for s in samples:
        payload = {k: s[k] for k in ("senderEmail", "subject", "body")}
        started = time.time()
        try:
            if use_fallback_only:
                data = fallback_classify(EmailRequest(**payload)).model_dump()
            else:
                resp = client.post("/analyze", json=payload)
                if resp.status_code != 200:
                    raise RuntimeError(f"HTTP {resp.status_code}: {resp.text[:100]}")
                data = resp.json()
            elapsed = time.time() - started
            source = "FALLBACK" if "basic keyword check" in data.get("explanation", "") else "LLM"

            missing = REQUIRED_FIELDS - set(data)
            low, high = SCORE_RANGES[s["expected"]]
            status_ok = data.get("status") == s["expected"]
            score_ok = low <= data.get("riskScore", -1) <= high
            ok = status_ok and score_ok and not missing

            result = "PASS" if ok else "FAIL"
            note = ""
            if missing:
                note += f" missing={sorted(missing)}"
            if not status_ok:
                note += f" got_status={data.get('status')}"
            if not score_ok:
                note += f" score_out_of_range({low}-{high})"
            line = (
                f"#{s['id']:<2} {result}  expected={s['expected']:<12} "
                f"got={data.get('status'):<12} score={data.get('riskScore'):<3} "
                f"{elapsed:4.1f}s  via={source}{note}"
            )
        except Exception as e:
            ok = False
            line = f"#{s['id']:<2} FAIL  expected={s['expected']:<12} error: {e}"

        passed += 1 if ok else 0
        lines.append(line)
        print(line)
        if not use_fallback_only:
            time.sleep(2) # be gentle with the free-tier rate limit
    
    llm_count = sum(1 for l in lines if "via=LLM" in l)
    lines.append(f"\nAnswered by LLM: {llm_count}/{len(samples)}")
    print(f"\nAnswered by LLM: {llm_count}/{len(samples)}")

    summary = f"\nResult: {passed}/{len(samples)} passed"
    lines.append(summary)
    print(summary)

    log_name = "test_results_fallback.log" if use_fallback_only else "test_results.log"
    (ROOT / "tests" / log_name).write_text("\n".join(lines), encoding="utf-8")
    print(f"Log saved to tests/{log_name}")


if __name__ == "__main__":
    run(use_fallback_only="--fallback" in sys.argv)