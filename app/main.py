from fastapi import FastAPI
from app.schemas import EmailRequest, AnalysisResponse

app = FastAPI(title="MailGuard AI Service")


@app.get("/")
def health():
    return {"message": "MailGuard AI service is running"}


@app.post("/analyze", response_model=AnalysisResponse)
def analyze(email: EmailRequest):
    # Dummy response. We replace this with the real classifier in Phase 6.
    return AnalysisResponse(
        status="SAFE",
        riskScore=10,
        suspiciousWords=[],
        explanation="This is a placeholder response.",
        recommendations=["No action needed."],
    )