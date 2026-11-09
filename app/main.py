from fastapi import FastAPI

app = FastAPI(title="MailGuard AI Service")

@app.get("/")
def health():
    return {"message": "MailGuard AI service is running"}