package com.enterprise.dataplatform.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequest {

    @NotBlank(message = "Group name is required")
    private String groupName;

    private String description;
    private String parentId;
    private java.util.Map<String, String> tags;
}
