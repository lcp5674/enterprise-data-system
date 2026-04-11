package com.enterprise.dataplatform.admin.dto.response;

import lombok.Data;
import java.util.List;

/**
 * Service Health Response DTO
 */
@Data
public class ServiceHealthResponse {
    private String serviceName;
    private String status; // UP, DOWN, DEGRADED
    private Double responseTime; // ms
    private Integer instances;
    private String message;
    private List<InstanceHealth> instancesDetail;

    @Data
    public static class InstanceHealth {
        private String instanceId;
        private String host;
        private Integer port;
        private String status;
    }
}
