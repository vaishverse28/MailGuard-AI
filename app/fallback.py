import re
from typing import List

from app.schemas import AnalysisResponse, EmailRequest

# Words that suggest credential or account theft
PHISHING_KEYWORDS = [
    "urgent", "verify your account", "verify", "password", "otp",
    "one-time password", "suspended", "account locked", "confirm your identity",
    "security alert", "credentials", "bank account", "card number", "cvv",
    "update your payment", "click here", "login",
]

# Words that suggest junk or scam mail
SPAM_KEYWORDS = [
    "lottery", "winner", "you have won", "claim reward", "claim your prize",
    "congratulations", "million", "free gift", "work from home",
    "earn money", "guaranteed", "act now", "no investment", "inheritance",
]

# Words that suggest genuine marketing mail
PROMO_KEYWORDS = [
    "sale", "discount", "% off", "offer", "unsubscribe", "newsletter",
    "deal", "coupon", "limited time", "free shipping", "new arrivals",
]

# Words that make a sender domain look like an imitation of a trusted one
SUSPICIOUS_DOMAIN_WORDS = ["security", "verify", "secure", "login", "support", "account", "update"]
SUSPICIOUS_TLDS = (".xyz", ".top", ".click", ".tk", ".gq", ".ml", ".cf", ".work", ".buzz")

# Words that point to credential theft specifically
CREDENTIAL_WORDS = {"password", "otp", "one-time password", "verify", "verify your account",
                    "credentials", "confirm your identity", "cvv", "card number", "bank account"}


def _find_hits(text: str, keywords: List[str]) -> List[str]:
    """Return the keywords found in the text, using the text's own spelling."""
    found = []
    seen = set()
    for kw in keywords:
        pattern = r"(?<!\w)" + re.escape(kw) + r"(?!\w)"
        m = re.search(pattern, text, re.IGNORECASE)
        if m and kw.lower() not in seen:
            seen.add(kw.lower())
            found.append(m.group(0))
    return found


def _suspicious_domain(sender: str) -> bool:
    domain = sender.split("@")[-1].lower().strip()
    if domain.endswith(SUSPICIOUS_TLDS):
        return True
    return "-" in domain and any(w in domain for w in SUSPICIOUS_DOMAIN_WORDS)


def fallback_classify(email: EmailRequest) -> AnalysisResponse:
    text = f"{email.subject} {email.body}"

    phishing_hits = _find_hits(text, PHISHING_KEYWORDS)
    spam_hits = _find_hits(text, SPAM_KEYWORDS)
    promo_hits = _find_hits(text, PROMO_KEYWORDS)
    bad_domain = _suspicious_domain(email.senderEmail)

    has_credential_word = any(h.lower() in CREDENTIAL_WORDS for h in phishing_hits)

    if (len(phishing_hits) >= 2 and has_credential_word) or (phishing_hits and bad_domain):
        score = 75 + min(25, len(phishing_hits) * 4 + (8 if bad_domain else 0))
        return AnalysisResponse(
            status="PHISHING",
            riskScore=min(100, score),
            suspiciousWords=phishing_hits[:10],
            explanation=(
                "This email uses urgent or account-related wording that is common in phishing attempts."
                + (" The sender address also looks suspicious." if bad_domain else "")
                + " This result comes from a basic keyword check."
            ),
            recommendations=[
                "Do not click any links in this email.",
                "Never share your password or OTP.",
                "Verify the sender through the official website or app.",
            ],
        )

    if len(spam_hits) >= 2 or (spam_hits and bad_domain):
        return AnalysisResponse(
            status="SPAM",
            riskScore=min(85, 50 + len(spam_hits) * 8 + (5 if bad_domain else 0)),
            suspiciousWords=spam_hits[:10],
            explanation=(
                "This email contains prize, reward or get-rich style wording that is typical of junk mail. "
                "This result comes from a basic keyword check."
            ),
            recommendations=[
                "Do not reply or click any links.",
                "Mark the email as spam and delete it.",
            ],
        )

    if len(promo_hits) >= 2:
        return AnalysisResponse(
            status="PROMOTIONAL",
            riskScore=min(50, 25 + len(promo_hits) * 5),
            suspiciousWords=[],
            explanation=(
                "This email looks like a marketing message with offers or discounts. "
                "This result comes from a basic keyword check."
            ),
            recommendations=[
                "Check the sender address before opening any offer links.",
                "Unsubscribe if you no longer want these emails.",
            ],
        )

    all_hits = phishing_hits + spam_hits
    return AnalysisResponse(
        status="SAFE",
        riskScore=min(30, 8 + len(all_hits) * 8),
        suspiciousWords=all_hits[:10],
        explanation=(
            "No strong warning signs were found in this email. "
            "This result comes from a basic keyword check."
        ),
        recommendations=[
            "Stay careful with unexpected links or attachments.",
            "Verify the sender if anything seems unusual.",
        ],
    )