FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN apt-get update && apt-get install -y build-essential git libxml2 libxslt1.1 libz-dev libjpeg-dev poppler-utils \
  && pip install --no-cache-dir -r requirements.txt \
  && apt-get remove -y build-essential git && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*
COPY model_server.py .
EXPOSE 8001
CMD ["uvicorn", "model_server:app", "--host", "0.0.0.0", "--port", "8001"]
