package com.smarthire;

import com.smarthire.repo.ResumeRepository;
import com.smarthire.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests — Spring context with H2, full Security filter chain.
 * Each test registers a fresh user and carries a valid JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ResumeApiTest {

    @Autowired MockMvc         mvc;
    @Autowired ResumeRepository repo;
    @Autowired UserRepository   userRepo;

    private String token;

    @BeforeEach
    void authenticate() throws Exception {
        // Register (or re-use) a test recruiter and capture JWT
        String body = """
                {"username":"testuser","password":"testpass","role":"RECRUITER"}
                """;
        MvcResult res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        // If 409 (user already exists), login instead
        if (res.getResponse().getStatus() == 409) {
            res = mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"testuser","password":"testpass"}
                                    """))
                    .andReturn();
        }

        String json = res.getResponse().getContentAsString();
        // extract token from {"token":"..."}
        token = json.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void listResumes_returns_200_for_authenticated_user() throws Exception {
        mvc.perform(get("/api/resumes/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void listResumes_paginated_returns_200() throws Exception {
        mvc.perform(get("/api/resumes?page=0&size=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void unauthenticated_request_returns_403() throws Exception {
        mvc.perform(get("/api/resumes/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void upload_realPdf_returns_201_with_status() throws Exception {
        byte[] pdfBytes = Files.readAllBytes(
                Paths.get("../uploaded_resumes/AdityaSingh_Resume.pdf"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "AdityaSingh_Resume.pdf", "application/pdf", pdfBytes);

        mvc.perform(multipart("/api/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumeId", notNullValue()))
                .andExpect(jsonPath("$.status", oneOf("PARSED", "PARSE_FAILED")));
    }

    @Test
    void upload_then_list_contains_resume() throws Exception {
        byte[] pdfBytes = Files.readAllBytes(
                Paths.get("../uploaded_resumes/22BCS15656_Mohammad-Saiful-Haque_Resume_Newgen.pdf"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "saiful_test.pdf", "application/pdf", pdfBytes);

        mvc.perform(multipart("/api/resumes")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/resumes/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].filename", hasItem("saiful_test.pdf")));
    }

    @Test
    void getResume_notFound_returns_404() throws Exception {
        mvc.perform(get("/api/resumes/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("not_found")));
    }

    @Test
    void auth_register_and_login_roundtrip() throws Exception {
        // Register new unique user
        String user = "roundtrip_" + System.currentTimeMillis();
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"pass123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role", is("RECRUITER")));

        // Login with same credentials
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void login_bad_credentials_returns_401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("invalid credentials")));
    }
}
