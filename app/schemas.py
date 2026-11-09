from typing import List, Literal
from pydantic import BaseModel, Field


class EmailRequest(BaseModel):
    senderEmail: str
    subject: str
    body: str


class AnalysisResponse(BaseModel):
    status: Literal["SAFE", "SPAM", "PROMOTIONAL", "PHISHING"]
    riskScore: int = Field(ge=0, le=100)
    suspiciousWords: List[str]
    explanation: str
    recommendations: List[str]