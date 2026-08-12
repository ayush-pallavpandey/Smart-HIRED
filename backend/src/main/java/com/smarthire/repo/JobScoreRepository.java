package com.smarthire.repo;

import com.smarthire.model.JobScoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobScoreRepository extends JpaRepository<JobScoreEntity, Integer> {

    /** Returns scores for a job ordered by score DESC, with resume eagerly fetched. */
    @Query("SELECT js FROM JobScoreEntity js JOIN FETCH js.resume WHERE js.jobId = :jobId ORDER BY js.score DESC")
    Page<JobScoreEntity> findByJobIdOrderByScoreDesc(@Param("jobId") String jobId, Pageable pageable);
}
