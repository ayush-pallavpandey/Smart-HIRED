package com.smarthire.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jobs")
public class JobEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "requirements", columnDefinition = "text")
    private String requirements;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "status")
    private String status = "QUEUED";

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public JobEntity() {}

    public String getId()                  { return id; }
    public void setId(String id)           { this.id = id; }

    public String getName()                { return name; }
    public void setName(String name)       { this.name = name; }

    public String getRequirements()        { return requirements; }
    public void setRequirements(String r)  { this.requirements = r; }

    public Integer getCreatedBy()          { return createdBy; }
    public void setCreatedBy(Integer c)    { this.createdBy = c; }

    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }

    public Instant getCreatedAt()          { return createdAt; }
    public void setCreatedAt(Instant t)    { this.createdAt = t; }
}
