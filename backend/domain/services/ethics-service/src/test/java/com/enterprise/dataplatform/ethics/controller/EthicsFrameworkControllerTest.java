package com.enterprise.dataplatform.ethics.controller;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsFrameworkRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsFrameworkResponse;
import com.enterprise.dataplatform.ethics.service.EthicsFrameworkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EthicsFrameworkController.class)
class EthicsFrameworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EthicsFrameworkService frameworkService;

    @Autowired
    private ObjectMapper objectMapper;

    private EthicsFrameworkRequest request;
    private EthicsFrameworkResponse response;

    @BeforeEach
    void setUp() {
        request = EthicsFrameworkRequest.builder()
                .frameworkCode("EF-001")
                .frameworkName("数据伦理框架")
                .description("测试框架")
                .principles(Arrays.asList("透明性", "公平性"))
                .riskThreshold("MEDIUM")
                .build();

        response = EthicsFrameworkResponse.builder()
                .id(1L)
                .frameworkCode("EF-001")
                .frameworkName("数据伦理框架")
                .description("测试框架")
                .principles(Arrays.asList("透明性", "公平性"))
                .riskThreshold("MEDIUM")
                .status("DRAFT")
                .enabled(true)
                .build();
    }

    @Test
    void testCreateFramework() throws Exception {
        when(frameworkService.createFramework(any(EthicsFrameworkRequest.class), eq("system")))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/ethics/frameworks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.frameworkCode").value("EF-001"))
                .andExpect(jsonPath("$.data.frameworkName").value("数据伦理框架"));
    }

    @Test
    void testGetFramework() throws Exception {
        when(frameworkService.getFramework(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/ethics/frameworks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.frameworkCode").value("EF-001"));
    }

    @Test
    void testUpdateFramework() throws Exception {
        when(frameworkService.updateFramework(eq(1L), any(EthicsFrameworkRequest.class), eq("system")))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/ethics/frameworks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteFramework() throws Exception {
        mockMvc.perform(delete("/api/v1/ethics/frameworks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }
}
