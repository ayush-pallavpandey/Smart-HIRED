package com.smarthire.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_scores")
public class JobScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "resume_id")
    private Integer resumeId;

    @Column(name = "score")
    private Double score;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Joined for convenience in result projection
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", insertable = false, updatable = false)
    private ResumeEntity resume;

    public JobScoreEntity() {}

    public Integer getId()               { return id; }
    public void setId(Integer id)        { this.id = id; }

    public String getJobId()             { return jobId; }
    public void setJobId(String jobId)   { this.jobId = jobId; }

    public Integer getResumeId()         { return resumeId; }
    public void setResumeId(Integer r)   { this.resumeId = r; }

    public Double getScore()             { return score; }
    public void setScore(Double score)   { this.score = score; }

    public Instant getCreatedAt()        { return createdAt; }
    public void setCreatedAt(Instant t)  { this.createdAt = t; }

    public ResumeEntity getResume()      { return resume; }
}
