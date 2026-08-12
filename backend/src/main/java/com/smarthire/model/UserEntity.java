package com.smarthire.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** RECRUITER or ADMIN */
    @Column(name = "role", nullable = false)
    private String role = "RECRUITER";

    public UserEntity() {}

    public Integer getId()                  { return id; }
    public void setId(Integer id)           { this.id = id; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getPasswordHash()         { return passwordHash; }
    public void setPasswordHash(String h)   { this.passwordHash = h; }

    public String getRole()                 { return role; }
    public void setRole(String role)        { this.role = role; }
}
