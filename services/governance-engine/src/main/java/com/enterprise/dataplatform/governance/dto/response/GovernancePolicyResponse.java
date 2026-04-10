package com.enterprise.dataplatform.governance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernancePolicyResponse {

    private Long id;

    private String name;

    private String policyType;

    private String description;

    private String assetType;

    private String assetSubType;

    private Integer priority;

    private String priorityLabel;

    private String triggerCondition;

    private String actionDefinition;

    private Map<String, Object> parameters;

    private String applicableRoles;

    private Boolean enabled;

    private String status;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    private Long taskCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;
}
