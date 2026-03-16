import os
import mimetypes
import requests
from google import genai

API_KEY = os.getenv("GEMINI_API_KEY")
if not API_KEY:
    raise RuntimeError("GEMINI_API_KEY environment variable not set")

# Reuse your existing store:
STORE_ID = "fileSearchStores/moneybuddyrecommendationsou-nvvxvhp3ycil"

# If you ever want to create a new store instead, uncomment below:
# client = genai.Client(api_key=API_KEY)
# store = client.file_search_stores.create(
#     config={"display_name": "moneybuddy-recommendation-sources"}
# )
# STORE_ID = store.name
# print("STORE_ID:", STORE_ID)

DOC_PATHS = [
    "rag_docs/budgeting.md",
    "rag_docs/emergency_fund.md",
    "rag_docs/fixed_deposit.md",
    "rag_docs/carbon.md",
]

def upload_to_file_search_store(store_id: str, file_path: str, api_key: str):
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"File not found: {file_path}")

    mime_type, _ = mimetypes.guess_type(file_path)
    if mime_type is None:
        mime_type = "text/markdown"

    url = (
        f"https://generativelanguage.googleapis.com/upload/v1beta/"
        f"{store_id}:uploadToFileSearchStore?key={api_key}"
    )

    filename = os.path.basename(file_path)

    with open(file_path, "rb") as f:
        files = {
            "file": (filename, f, mime_type)
        }
        data = {
            "display_name": filename
        }

        response = requests.post(url, files=files, data=data, timeout=120)

    if not response.ok:
        raise RuntimeError(
            f"Upload failed for {file_path}\n"
            f"Status: {response.status_code}\n"
            f"Body: {response.text}"
        )

    return response.json()

def main():
    print("Using store:", STORE_ID)

    for path in DOC_PATHS:
        result = upload_to_file_search_store(STORE_ID, path, API_KEY)
        print(f"Uploaded: {path}")
        print(result)

if __name__ == "__main__":
    main()