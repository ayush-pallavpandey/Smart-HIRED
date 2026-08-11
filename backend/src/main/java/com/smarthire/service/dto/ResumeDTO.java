package com.smarthire.service.dto;

public class ResumeDTO {
    private Integer id;
    private String filename;
    private String status;

    public ResumeDTO() {}

    public ResumeDTO(Integer id, String filename, String status) {
        this.id = id;
        this.filename = filename;
        this.status = status;
    }

    public Integer getId() { return id; }
    public String getFilename() { return filename; }
    public String getStatus() { return status; }
    public void setId(Integer id) { this.id = id; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setStatus(String status) { this.status = status; }
}
