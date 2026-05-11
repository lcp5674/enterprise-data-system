package com.enterprise.dataplatform.ethics.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsFrameworkRequest {

    @NotBlank(message = "框架编码不能为空")
    @Size(max = 64, message = "框架编码长度不能超过64")
    private String frameworkCode;

    @NotBlank(message = "框架名称不能为空")
    @Size(max = 128, message = "框架名称长度不能超过128")
    private String frameworkName;

    private String description;

    private List<String> principles;

    private String riskThreshold;

    private String category;

    private String source;

    private String tags;
}
