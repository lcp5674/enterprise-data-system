package com.enterprise.dataplatform.classification.dto.request;

import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRuleRequest {

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    private String ruleDescription;

    @NotNull(message = "规则类型不能为空")
    private ClassificationRuleType ruleType;

    @NotNull(message = "敏感级别不能为空")
    private SensitivityLevel sensitivityLevel;

    private String pattern;

    private String dataType;

    private List<String> keywords;

    private String columnPattern;

    @NotNull(message = "优先级不能为空")
    private Integer priority;

    private Double confidenceThreshold;

    private Boolean autoClassify;

    private Boolean triggerApproval;
}
