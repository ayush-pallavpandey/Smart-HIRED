package com.smarthire.service.impl;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

import com.smarthire.config.MlProperties;
import com.smarthire.config.UploadProperties;
import com.smarthire.model.ResumeEntity;
import com.smarthire.repo.ResumeRepository;
import com.smarthire.service.ResumeService;
import com.smarthire.service.dto.JobDTO;
import com.smarthire.service.dto.ResumeDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final UploadProperties uploadProperties;
    private final MlProperties mlProperties;
    private final ResumeRepository resumeRepository;
    private final RestTemplate rest = new RestTemplate();

    public ResumeServiceImpl(UploadProperties uploadProperties, MlProperties mlProperties, ResumeRepository resumeRepository) {
        this.uploadProperties = uploadProperties;
        this.mlProperties = mlProperties;
        this.resumeRepository = resumeRepository;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(uploadProperties.getDirectory()));
    }

    @Override
    public ResumeDTO store(MultipartFile file) throws Exception {
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        String uuidName = UUID.randomUUID().toString() + "-" + filename;
        Path dest = Paths.get(uploadProperties.getDirectory(), uuidName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // Persist metadata
        ResumeEntity entity = new ResumeEntity();
        entity.setFilename(filename);
        entity.setPath(dest.toString());
        entity.setUploadedAt(Instant.now());
        entity.setStatus("PARSING");
        entity = resumeRepository.save(entity);

        // Read file bytes and send to ML parse endpoint (base64)
        byte[] bytes = Files.readAllBytes(dest);
        String b64 = Base64.getEncoder().encodeToString(bytes);
        Map<String, String> payload = Map.of(
            "filename", filename,
            "content_b64", b64
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> body = new HttpEntity<>(payload, headers);
            ResponseEntity<Map<String, Object>> resp = rest.postForEntity(mlProperties.getUrl() + "/parse", body, (Class<Map<String, Object>>) (Class<?>) Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object textObj = resp.getBody().get("text");
                String text = textObj == null ? "" : textObj.toString();
                entity.setTextExtracted(text);
                entity.setStatus("PARSED");
            } else {
                entity.setStatus("PARSE_FAILED");
            }
        } catch (Exception e) {
            entity.setStatus("PARSE_FAILED");
            // optionally store error info somewhere
        } finally {
            resumeRepository.save(entity);
        }

        return new ResumeDTO(entity.getId(), entity.getFilename(), entity.getStatus());
    }

    @Override
    public JobDTO createJob(String name, String requirements) {
        String id = UUID.randomUUID().toString();
        JobDTO job = new JobDTO(id, name, requirements, "QUEUED");
        // TODO: persist to DB
        return job;
    }

    @Override
    public void processJobAsync(String jobId) {
        new Thread(() -> {
            try {
                Map<String,Object> req = Map.of("requirement", "Java Spring Boot");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> body = new HttpEntity<>(req, headers);
                rest.postForEntity(mlProperties.getUrl() + "/score", body, (Class<Map<String, Object>>) (Class<?>) Map.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public Object getJobResults(String jobId, int page, int size) {
        return Map.of("candidates", List.of(), "total", 0);
    }

    @Override
    public ResumeDTO getResume(Integer id) {
        Optional<ResumeEntity> o = resumeRepository.findById(id);
        if (o.isEmpty()) return null;
        ResumeEntity e = o.get();
        return new ResumeDTO(e.getId(), e.getFilename(), e.getStatus());
    }
}
