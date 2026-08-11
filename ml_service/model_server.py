from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
from sentence_transformers import SentenceTransformer, util
import numpy as np
import os
import base64
import io

# Parsing libs
import pdfplumber
import docx

app = FastAPI(title="SmartHire ML Service")
MODEL_NAME = os.environ.get("MODEL_NAME", "all-mpnet-base-v2")
model = SentenceTransformer(MODEL_NAME)


class EmbedRequest(BaseModel):
    texts: list[str]


class ScoreRequest(BaseModel):
    requirement: str
    resumes: list[str]  # list of raw text strings


class ParseRequest(BaseModel):
    filename: str
    content_b64: str


@app.post("/embed")
def embed(req: EmbedRequest):
    emb = model.encode(req.texts, convert_to_numpy=True, show_progress_bar=False)
    return {"embeddings": emb.tolist()}


@app.post("/score")
def score(req: ScoreRequest):
    if not req.resumes:
        return {"results": []}
    r_emb = model.encode([req.requirement], convert_to_numpy=True)[0]
    resumes_emb = model.encode(req.resumes, convert_to_numpy=True, show_progress_bar=False)
    sims = util.cos_sim(r_emb, resumes_emb)[0].cpu().numpy()
    results = [{"index": i, "score": float(s)} for i, s in enumerate(sims)]
    results.sort(key=lambda x: x["score"], reverse=True)
    return {"results": results}


def extract_text_from_pdf_bytes(b: bytes) -> str:
    text_parts = []
    with pdfplumber.open(io.BytesIO(b)) as pdf:
        for page in pdf.pages:
            text_parts.append(page.extract_text() or "")
    return "\n".join(text_parts)


def extract_text_from_docx_bytes(b: bytes) -> str:
    doc = docx.Document(io.BytesIO(b))
    paragraphs = [p.text for p in doc.paragraphs if p.text]
    return "\n".join(paragraphs)


@app.post("/parse")
def parse(req: ParseRequest):
    filename = req.filename or ""
    try:
        b = base64.b64decode(req.content_b64)
    except Exception as e:
        return {"error": f"invalid base64 content: {e}"}

    lowered = filename.lower()
    try:
        if lowered.endswith(".pdf"):
            text = extract_text_from_pdf_bytes(b)
        elif lowered.endswith(".docx") or lowered.endswith(".doc"):
            text = extract_text_from_docx_bytes(b)
        else:
            try:
                text = b.decode("utf-8")
            except Exception:
                text = b.decode("latin-1", errors="ignore")
        return {"text": text}
    except Exception as e:
        return {"error": f"parsing_failed: {e}"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)
