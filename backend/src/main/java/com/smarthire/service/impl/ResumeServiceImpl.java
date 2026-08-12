package com.smarthire.service.impl;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

import com.smarthire.config.MlProperties;
import com.smarthire.config.UploadProperties;
import com.smarthire.model.JobEntity;
import com.smarthire.model.JobScoreEntity;
import com.smarthire.model.ResumeEntity;
import com.smarthire.repo.JobRepository;
import com.smarthire.repo.JobScoreRepository;
import com.smarthire.repo.ResumeRepository;
import com.smarthire.service.ResumeService;
import com.smarthire.service.dto.JobDTO;
import com.smarthire.service.dto.ResumeDTO;
import com.smarthire.service.dto.ScoredResumeDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);

    private final UploadProperties uploadProperties;
    private final MlProperties mlProperties;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final JobScoreRepository jobScoreRepository;
    private final RestTemplate rest = new RestTemplate();

    public ResumeServiceImpl(UploadProperties uploadProperties,
                             MlProperties mlProperties,
                             ResumeRepository resumeRepository,
                             JobRepository jobRepository,
                             JobScoreRepository jobScoreRepository) {
        this.uploadProperties   = uploadProperties;
        this.mlProperties       = mlProperties;
        this.resumeRepository   = resumeRepository;
        this.jobRepository      = jobRepository;
        this.jobScoreRepository = jobScoreRepository;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadProperties.getDirectory()));
    }

    // ── Upload & parse ───────────────────────────────────────────────────────

    @Override
    public ResumeDTO store(MultipartFile file) throws Exception {
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        String uuidName = UUID.randomUUID().toString() + "-" + filename;
        Path dest = Paths.get(uploadProperties.getDirectory(), uuidName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        ResumeEntity entity = new ResumeEntity();
        entity.setFilename(filename);
        entity.setPath(dest.toString());
        entity.setUploadedAt(Instant.now());
        entity.setStatus("PARSING");
        entity = resumeRepository.save(entity);

        byte[] bytes = Files.readAllBytes(dest);
        String b64   = Base64.getEncoder().encodeToString(bytes);
        Map<String, String> payload = Map.of("filename", filename, "content_b64", b64);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> body = new HttpEntity<>(payload, headers);
            ResponseEntity<Map<String, Object>> resp = rest.postForEntity(
                    mlProperties.getUrl() + "/parse", body,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object textObj = resp.getBody().get("text");
                String text = textObj == null ? "" : textObj.toString();
                entity.setTextExtracted(text);
                entity.setStatus("PARSED");
            } else {
                entity.setStatus("PARSE_FAILED");
            }
        } catch (Exception e) {
            log.warn("ML parse failed for {}: {}", filename, e.getMessage());
            entity.setStatus("PARSE_FAILED");
        } finally {
            resumeRepository.save(entity);
        }

        return new ResumeDTO(entity.getId(), entity.getFilename(), entity.getStatus());
    }

    // ── Job management ───────────────────────────────────────────────────────

    @Override
    public JobDTO createJob(String name, String requirements) {
        JobEntity job = new JobEntity();
        job.setId(UUID.randomUUID().toString());
        job.setName(name);
        job.setRequirements(requirements);
        job.setStatus("QUEUED");
        job.setCreatedAt(Instant.now());
        jobRepository.save(job);
        return toJobDTO(job);
    }

    @Override
    @Async
    public void processJobAsync(String jobId) {
        JobEntity job = jobRepository.findById(jobId).orElse(null);
        if (job == null) { log.warn("processJobAsync: jobId {} not found", jobId); return; }

        job.setStatus("PROCESSING");
        jobRepository.save(job);

        // Fetch all PARSED resumes (handles 1000+)
        List<ResumeEntity> resumes = resumeRepository.findAll()
                .stream()
                .filter(r -> "PARSED".equals(r.getStatus())
                          && r.getTextExtracted() != null
                          && !r.getTextExtracted().isBlank())
                .toList();

        if (resumes.isEmpty()) {
            job.setStatus("DONE");
            jobRepository.save(job);
            return;
        }

        List<String> texts = resumes.stream().map(ResumeEntity::getTextExtracted).toList();

        try {
            // Call ML /score: {requirement, resumes:[text,...]}
            Map<String, Object> req = Map.of("requirement", job.getRequirements(), "resumes", texts);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpBody = new HttpEntity<>(req, headers);

            ResponseEntity<Map<String, Object>> resp = rest.postForEntity(
                    mlProperties.getUrl() + "/score", httpBody,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> results =
                        (List<Map<String, Object>>) resp.getBody().get("results");

                if (results != null) {
                    List<JobScoreEntity> scores = new ArrayList<>();
                    for (Map<String, Object> r : results) {
                        int idx = ((Number) r.get("index")).intValue();
                        double score = ((Number) r.get("score")).doubleValue();
                        JobScoreEntity js = new JobScoreEntity();
                        js.setJobId(jobId);
                        js.setResumeId(resumes.get(idx).getId());
                        js.setScore(score);
                        scores.add(js);
                    }
                    // Batch-save all scores efficiently
                    jobScoreRepository.saveAll(scores);
                    log.info("Job {} scored {} resumes", jobId, scores.size());
                }
            }
            job.setStatus("DONE");
        } catch (Exception e) {
            log.error("Job {} scoring failed: {}", jobId, e.getMessage());
            job.setStatus("FAILED");
        }
        jobRepository.save(job);
    }

    @Override
    public Object getJobResults(String jobId, int page, int size) {
        Page<JobScoreEntity> scored = jobScoreRepository
                .findByJobIdOrderByScoreDesc(jobId, PageRequest.of(page, size));

        List<ScoredResumeDTO> candidates = scored.getContent().stream().map(js -> {
            ResumeEntity r = js.getResume();
            return new ScoredResumeDTO(
                    r != null ? r.getId() : js.getResumeId(),
                    r != null ? r.getFilename() : "unknown",
                    js.getScore(),
                    r != null ? r.getStatus() : "?"
            );
        }).toList();

        return Map.of(
                "jobId",       jobId,
                "candidates",  candidates,
                "total",       scored.getTotalElements(),
                "pages",       scored.getTotalPages(),
                "page",        page
        );
    }

    @Override
    public ResumeDTO getResume(Integer id) {
        return resumeRepository.findById(id)
                .map(e -> new ResumeDTO(e.getId(), e.getFilename(), e.getStatus()))
                .orElse(null);
    }

    private JobDTO toJobDTO(JobEntity e) {
        return new JobDTO(e.getId(), e.getName(), e.getRequirements(), e.getStatus());
    }
}
