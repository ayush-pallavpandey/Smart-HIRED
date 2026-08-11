import React, { useEffect, useState } from "react";

const API_BASE = process.env.REACT_APP_API_URL || "";

export default function Dashboard() {
  const [file, setFile] = useState(null);
  const [resumes, setResumes] = useState([]);
  const [selected, setSelected] = useState(null);
  const [status, setStatus] = useState("");

  async function fetchResumes() {
    try {
      const res = await fetch(`${API_BASE}/api/resumes`);
      const json = await res.json();
      setResumes(json || []);
    } catch (e) {
      setStatus("Failed to fetch resumes: " + e.message);
    }
  }

  useEffect(() => {
    fetchResumes();
  }, []);

  async function upload() {
    if (!file) {
      setStatus("Choose a file first");
      return;
    }
    setStatus("Uploading...");
    const fd = new FormData();
    fd.append("file", file);
    try {
      const res = await fetch(`${API_BASE}/api/resumes`, {
        method: "POST",
        body: fd,
      });
      const json = await res.json();
      setStatus("Uploaded: " + JSON.stringify(json));
      // refresh list
      await fetchResumes();
    } catch (e) {
      setStatus("Upload failed: " + e.message);
    }
  }

  async function selectResume(id) {
    setSelected(null);
    try {
      const res = await fetch(`${API_BASE}/api/resumes/${id}`);
      const json = await res.json();
      // The list endpoint returns ResumeEntity with text_extracted when present.
      // The GET by id returns ResumeDTO with id/filename/status in current skeleton.
      // To get full text, we rely on the list endpoint data (contains text_extracted)
      // so fetch the list and find the entity
      if (res.ok) {
        const found = resumes.find((r) => r.id === id);
        setSelected(found || json);
      } else {
        setStatus("Error fetching resume: " + JSON.stringify(json));
      }
    } catch (e) {
      setStatus("Failed to fetch resume: " + e.message);
    }
  }

  return (
      <div style={{ display: "flex", padding: 20, gap: 20 }}>
        <div style={{ width: 360 }}>
          <h3>Upload Resume</h3>
          <input type="file" onChange={(e) => setFile(e.target.files[0])} />
          <button onClick={upload} style={{ display: "block", marginTop: 8 }}>
            Upload
          </button>
          <div style={{ marginTop: 12, color: "green" }}>{status}</div>

          <h3 style={{ marginTop: 24 }}>Resumes</h3>
          <button onClick={fetchResumes}>Refresh</button>
          <ul style={{ maxHeight: 500, overflow: "auto", paddingLeft: 10 }}>
            {resumes.length === 0 && <li>(no resumes)</li>}
            {resumes.map((r) => (
                <li key={r.id} style={{ marginTop: 8 }}>
                  <div>
                    <strong>{r.filename}</strong>{" "}
                    <span style={{ color: "#666" }}>
                  ({r.status || "UNKNOWN"})
                </span>
                  </div>
                  <div style={{ marginTop: 4 }}>
                    <button onClick={() => selectResume(r.id)}>View</button>
                  </div>
                </li>
            ))}
          </ul>
        </div>

        <div style={{ flex: 1 }}>
          <h3>Resume Details</h3>
          {!selected && <div>Select a resume to view its parsed text</div>}
          {selected && (
              <div>
                <div>
                  <strong>ID:</strong> {selected.id}
                </div>
                <div>
                  <strong>Filename:</strong> {selected.filename}
                </div>
                <div>
                  <strong>Status:</strong> {selected.status}
                </div>
                <div style={{ marginTop: 12 }}>
              <textarea
                  style={{ width: "100%", height: "60vh" }}
                  readOnly
                  value={
                    // different backends: some return textExtracted or text_extracted
                      selected.textExtracted || selected.text_extracted || ""
                  }
              />
                </div>
              </div>
          )}
        </div>
      </div>
  );
}