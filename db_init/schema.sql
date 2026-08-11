CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'RECRUITER',
  created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE resumes (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id),
  filename TEXT NOT NULL,
  path TEXT,
  text_extracted TEXT,
  uploaded_at TIMESTAMP DEFAULT now(),
  status TEXT DEFAULT 'UPLOADED'
);

CREATE TABLE jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  requirements TEXT,
  created_by INT REFERENCES users(id),
  status TEXT DEFAULT 'QUEUED',
  created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE job_scores (
  id SERIAL PRIMARY KEY,
  job_id UUID REFERENCES jobs(id) ON DELETE CASCADE,
  resume_id INT REFERENCES resumes(id) ON DELETE CASCADE,
  score NUMERIC,
  matched_terms JSONB,
  created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_job_scores_job_id_score ON job_scores(job_id, score DESC);
CREATE INDEX idx_resumes_uploaded_at ON resumes(uploaded_at);
