package com.smarthire.service.dto;

public class ScoredResumeDTO {
    private Integer resumeId;
    private String  filename;
    private Double  score;
    private String  status;

    public ScoredResumeDTO() {}

    public ScoredResumeDTO(Integer resumeId, String filename, Double score, String status) {
        this.resumeId = resumeId;
        this.filename = filename;
        this.score    = score;
        this.status   = status;
    }

    public Integer getResumeId()            { return resumeId; }
    public void setResumeId(Integer id)     { this.resumeId = id; }

    public String getFilename()             { return filename; }
    public void setFilename(String f)       { this.filename = f; }

    public Double getScore()                { return score; }
    public void setScore(Double s)          { this.score = s; }

    public String getStatus()               { return status; }
    public void setStatus(String s)         { this.status = s; }
}
