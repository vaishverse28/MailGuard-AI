import os
from dotenv import load_dotenv
from google import genai

load_dotenv()

api_key = os.getenv("LLM_API_KEY")
model = os.getenv("LLM_MODEL", "gemini-2.5-flash")

if not api_key:
    raise SystemExit("LLM_API_KEY not found. Check your .env file.")

client = genai.Client(api_key=api_key)

response = client.models.generate_content(
    model=model,
    contents="Reply with exactly one word: hello",
)
print("Gemini says:", response.text)