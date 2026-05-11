package com.enterprise.edams.analysis.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {

    private String model;
    private String prompt;
    private Integer maxTokens;
    private Double temperature;
    private BigDecimal topP;
    private Integer stream;
    private String[] stop;
}
