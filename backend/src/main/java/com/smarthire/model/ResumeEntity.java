package com.smarthire.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resumes")
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "path")
    private String path;

    @Lob
    @Column(name = "text_extracted", columnDefinition = "text")
    private String textExtracted;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "status")
    private String status;

    public ResumeEntity() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTextExtracted() { return textExtracted; }
    public void setTextExtracted(String textExtracted) { this.textExtracted = textExtracted; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
