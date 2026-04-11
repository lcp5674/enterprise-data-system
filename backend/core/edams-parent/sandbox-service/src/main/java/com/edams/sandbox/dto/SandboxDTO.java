package com.edams.sandbox.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Schema(description = "沙箱创建/更新请求")
public class SandboxDTO {
    
    @NotBlank(message = "沙箱名称不能为空")
    @Schema(description = "沙箱名称", example = "测试沙箱")
    private String name;
    
    @Schema(description = "描述", example = "用于SQL演练的测试环境")
    private String description;
    
    @NotBlank(message = "沙箱类型不能为空")
    @Schema(description = "沙箱类型", example = "SQL", allowableValues = {"SQL", "API", "DATA"})
    private String sandboxType;
    
    @Schema(description = "资源配置(JSON格式)", example = "{\"database\": \"test_db\", \"max_connections\": 10}")
    private String resourceConfig;
    
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
}

@Data
@Schema(description = "SQL执行请求")
class SqlExecutionRequest {
    
    @NotBlank(message = "SQL语句不能为空")
    @Schema(description = "SQL语句", example = "SELECT * FROM users LIMIT 10")
    private String sql;
    
    @Schema(description = "执行用户ID", example = "1")
    private Long userId;
}

@Data
@Schema(description = "API测试请求")
class ApiTestRequest {
    
    @NotBlank(message = "API地址不能为空")
    @Schema(description = "API地址", example = "http://localhost:8080/api/users")
    private String apiUrl;
    
    @Schema(description = "HTTP方法", example = "GET", allowableValues = {"GET", "POST", "PUT", "DELETE"})
    private String method;
    
    @Schema(description = "请求参数(JSON格式)")
    private String parameters;
    
    @Schema(description = "测试用户ID", example = "1")
    private Long userId;
}

@Data
@Schema(description = "数据模拟请求")
class DataSimulationRequest {
    
    @NotBlank(message = "模拟类型不能为空")
    @Schema(description = "模拟类型", example = "USER_DATA")
    private String simulationType;
    
    @Schema(description = "模拟配置(JSON格式)")
    private String config;
    
    @Schema(description = "模拟用户ID", example = "1")
    private Long userId;
}