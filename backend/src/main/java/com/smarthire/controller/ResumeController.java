package com.smarthire.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smarthire.service.ResumeService;
import com.smarthire.service.dto.ResumeDTO;
import com.smarthire.repo.ResumeRepository;
import com.smarthire.repo.JobRepository;
import com.smarthire.model.ResumeEntity;

@RestController
@RequestMapping("/api")
public class ResumeController {

    @Autowired private ResumeService    resumeService;
    @Autowired private ResumeRepository resumeRepository;
    @Autowired private JobRepository    jobRepository;

    // ── Resumes ──────────────────────────────────────────────────────────────

    @PostMapping("/resumes")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            var resume = resumeService.store(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "resumeId", resume.getId(),
                    "status",   resume.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() == null ? "unknown error" : e.getMessage()));
        }
    }

    /** Paginated resume list — supports 1000+ resumes. Default page=0, size=20. */
    @GetMapping("/resumes")
    public ResponseEntity<?> listResumes(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ResumeEntity> result = resumeRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt")));
        return ResponseEntity.ok(Map.of(
                "content", result.getContent(),
                "total",   result.getTotalElements(),
                "pages",   result.getTotalPages(),
                "page",    page
        ));
    }

    /** Convenience: all resumes without pagination (for small dashboards). */
    @GetMapping("/resumes/all")
    public ResponseEntity<?> listAllResumes() {
        return ResponseEntity.ok(resumeRepository.findAll(
                Sort.by(Sort.Direction.DESC, "uploadedAt")));
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<?> getResume(@PathVariable Integer id) {
        ResumeDTO dto = resumeService.getResume(id);
        if (dto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not_found"));
        return ResponseEntity.ok(dto);
    }

    // ── Jobs ─────────────────────────────────────────────────────────────────

    @PostMapping("/jobs")
    public ResponseEntity<?> createJob(@RequestBody Map<String, String> body) {
        var job = resumeService.createJob(body.get("name"), body.get("requirements"));
        resumeService.processJobAsync(job.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("jobId", job.getId(), "status", job.getStatus()));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .<ResponseEntity<?>>map(j -> ResponseEntity.ok(Map.of(
                        "jobId",        j.getId(),
                        "name",         j.getName(),
                        "requirements", j.getRequirements(),
                        "status",       j.getStatus(),
                        "createdAt",    j.getCreatedAt().toString()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not_found")));
    }

    @GetMapping("/jobs/{jobId}/results")
    public ResponseEntity<?> getResults(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(resumeService.getJobResults(jobId, page, size));
    }
}
