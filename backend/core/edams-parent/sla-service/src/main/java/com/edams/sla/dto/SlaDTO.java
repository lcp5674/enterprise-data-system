package com.edams.sla.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema(description = "SLA协议创建/更新请求")
public class SlaDTO {
    
    @NotBlank(message = "协议名称不能为空")
    @Schema(description = "协议名称", example = "API响应时间SLA")
    private String name;
    
    @Schema(description = "描述", example = "API接口响应时间监控协议")
    private String description;
    
    @NotBlank(message = "服务名称不能为空")
    @Schema(description = "服务名称", example = "auth-service")
    private String serviceName;
    
    @NotBlank(message = "服务类型不能为空")
    @Schema(description = "服务类型", example = "API", allowableValues = {"API", "DATABASE", "SYSTEM"})
    private String serviceType;
    
    @NotBlank(message = "监控对象不能为空")
    @Schema(description = "监控对象", example = "/api/auth/login")
    private String targetObject;
    
    @NotBlank(message = "指标类型不能为空")
    @Schema(description = "指标类型", example = "RESPONSE_TIME", allowableValues = {"RESPONSE_TIME", "AVAILABILITY", "ERROR_RATE"})
    private String metricType;
    
    @NotNull(message = "阈值数值不能为空")
    @Schema(description = "阈值数值", example = "200")
    private Double thresholdValue;
    
    @NotBlank(message = "阈值单位不能为空")
    @Schema(description = "阈值单位", example = "ms", allowableValues = {"ms", "%", "count"})
    private String thresholdUnit;
    
    @NotNull(message = "警告级别不能为空")
    @Schema(description = "警告级别", example = "300")
    private Double warningLevel;
    
    @NotNull(message = "严重级别不能为空")
    @Schema(description = "严重级别", example = "500")
    private Double criticalLevel;
    
    @NotNull(message = "所有者ID不能为空")
    @Schema(description = "所有者ID", example = "1")
    private Long ownerId;
    
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}

@Data
@Schema(description = "SLA报告生成请求")
class SlaReportRequest {
    
    @NotNull(message = "协议ID不能为空")
    @Schema(description = "协议ID", example = "1")
    private Long agreementId;
    
    @NotBlank(message = "报告周期不能为空")
    @Schema(description = "报告周期", example = "DAILY", allowableValues = {"DAILY", "WEEKLY", "MONTHLY"})
    private String reportPeriod;
    
    @NotNull(message = "生成用户ID不能为空")
    @Schema(description = "生成用户ID", example = "1")
    private Long userId;
}