package com.smarthire.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smarthire.service.ResumeService;
import com.smarthire.service.dto.ResumeDTO;
import com.smarthire.repo.ResumeRepository;
import com.smarthire.model.ResumeEntity;

// allow frontend dev server
@CrossOrigin(origins = "http://localhost:3000")
    @RestController
@RequestMapping("/api")
public class ResumeController {
@Autowired
 private ResumeService resumeService;
@Autowired
private ResumeRepository resumeRepository;
 @PostMapping("/resumes")  public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            var resume = resumeService.store(file);
                      return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                                    "resumeId", resume.getId(),
                              "status", resume.getStatus()
                      ));
            } catch (Exception e) {
                      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
                   }
          }
        // List all resumes (DEV only)
          @GetMapping("/resumes")
  public ResponseEntity<?> listResumes() {
              List<ResumeEntity> resumes = resumeRepository.findAll();
              return ResponseEntity.ok(resumes);
           }

           @GetMapping("/resumes/{id}")
  public ResponseEntity<?> getResume(@PathVariable Integer id) {
               ResumeDTO dto = resumeService.getResume(id);
              if (dto == null) {
                      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not_found"));
              }
                return ResponseEntity.ok(dto);
            }

    @PostMapping("/jobs")
  public ResponseEntity<?> createJob(@RequestBody Map<String, String> body) {
                var job = resumeService.createJob(body.get("name"), body.get("requirements"));
                resumeService.processJobAsync(job.getId());
               return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("jobId", job.getId(), "status", job.getStatus()));
            }
            @GetMapping("/jobs/{jobId}/results")
  public ResponseEntity<?> getResults(@PathVariable String jobId,
                                       @RequestParam(defaultValue="0") int page,
                                      @RequestParam(defaultValue="20") int size) {
               var results = resumeService.getJobResults(jobId, page, size);
               return ResponseEntity.ok(results);
          }
}