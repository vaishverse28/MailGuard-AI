from app.classifier import classify_email
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
        body="Claim reward now. Send your details to receive the lottery prize.",
    ),
    EmailRequest(
        senderEmail="deals@amazon.in",
        subject="Weekend Sale: Up to 50% off electronics",
        body="Check out our biggest weekend deals. Unsubscribe anytime at the bottom of this email.",
    ),
    EmailRequest(
        senderEmail="rahul.sharma@mycompany.com",
        subject="Meeting notes from today",
        body="Hi team, attached are the notes from today's meeting. Please review before Friday.",
    ),
]

for e in emails:
    print("=" * 60)
    print(e.subject)
    result = classify_email(e)
    print(result.model_dump_json(indent=2))