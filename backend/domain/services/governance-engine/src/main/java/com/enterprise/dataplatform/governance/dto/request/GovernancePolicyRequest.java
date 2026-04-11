package com.enterprise.dataplatform.governance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernancePolicyRequest {

    @NotBlank(message = "策略名称不能为空")
    private String name;

    @NotBlank(message = "策略类型不能为空")
    private String policyType;

    private String description;

    @NotNull(message = "资产类型不能为空")
    private String assetType;

    private String assetSubType;

    @NotNull(message = "优先级不能为空")
    private Integer priority;

    private String triggerCondition;

    private String actionDefinition;

    private Map<String, Object> parameters;

    private String applicableRoles;

    private Boolean enabled = true;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}
