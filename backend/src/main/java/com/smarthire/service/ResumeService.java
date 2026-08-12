package com.smarthire.service;

import org.springframework.web.multipart.MultipartFile;
import com.smarthire.service.dto.JobDTO;
import com.smarthire.service.dto.ResumeDTO;

public interface ResumeService {
    ResumeDTO store(MultipartFile file) throws Exception;
    JobDTO    createJob(String name, String requirements);
    void      processJobAsync(String jobId);
    Object    getJobResults(String jobId, int page, int size);
    ResumeDTO getResume(Integer id);
}
