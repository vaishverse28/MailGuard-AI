from app.fallback import fallback_classify
from app.schemas import EmailRequest

emails = [
    EmailRequest(
        senderEmail="support@paypal-security.com",
        subject="Urgent: Verify Your Account",
        body="Your account will be suspended. Click here to verify credentials and enter your password.",
    ),
    EmailRequest(
        senderEmail="winner@lucky-lottery-prize.xyz",
        subject="Congratulations! You won $1,000,000",
        body="Claim reward now. You are the lottery winner.",
    ),
    EmailRequest(
        senderEmail="deals@amazon.in",
        subject="Weekend Sale: Up to 50% off electronics",
        body="Biggest discount of the year. Unsubscribe anytime at the bottom of this email.",
    ),
    EmailRequest(
        senderEmail="rahul.sharma@mycompany.com",
        subject="Meeting notes from today",
        body="Hi team, attached are the notes from today's meeting. Please review before Friday.",
    ),
]

for e in emails:
    r = fallback_classify(e)
    print(f"{e.subject[:40]:42} -> {r.status:12} {r.riskScore:3}  {r.suspiciousWords}")