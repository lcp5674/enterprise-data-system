package com.enterprise.dataplatform.ethics.domain.dto.response;

import com.enterprise.dataplatform.ethics.domain.entity.EthicsFramework;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsFrameworkResponse {

    private Long id;
    private String frameworkCode;
    private String frameworkName;
    private String description;
    private List<String> principles;
    private String riskThreshold;
    private Integer version;
    private String status;
    private Boolean enabled;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private String tags;
    private String category;
    private String source;

    public static EthicsFrameworkResponse fromEntity(EthicsFramework entity) {
        return EthicsFrameworkResponse.builder()
                .id(entity.getId())
                .frameworkCode(entity.getFrameworkCode())
                .frameworkName(entity.getFrameworkName())
                .description(entity.getDescription())
                .principles(entity.getPrinciples())
                .riskThreshold(entity.getRiskThreshold() != null ? entity.getRiskThreshold().name() : null)
                .version(entity.getVersion())
                .status(entity.getStatus())
                .enabled(entity.getEnabled())
                .creator(entity.getCreator())
                .createTime(entity.getCreateTime())
                .updater(entity.getUpdater())
                .updateTime(entity.getUpdateTime())
                .tags(entity.getTags())
                .category(entity.getCategory())
                .source(entity.getSource())
                .build();
    }
}
