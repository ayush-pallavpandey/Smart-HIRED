package com.smarthire.service.dto;

public class JobDTO {
    private String id;
    private String name;
    private String requirements;
    private String status;

    public JobDTO() {}

    public JobDTO(String id, String name, String requirements, String status) {
        this.id = id;
        this.name = name;
        this.requirements = requirements;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRequirements() { return requirements; }
    public String getStatus() { return status; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public void setStatus(String status) { this.status = status; }
}
