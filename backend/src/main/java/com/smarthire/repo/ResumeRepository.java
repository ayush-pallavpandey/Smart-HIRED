package com.smarthire.repo;

import com.smarthire.model.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Integer> {
}
