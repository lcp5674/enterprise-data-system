package com.enterprise.edams.llm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * LLM配置属性类
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    /**
     * 提供商配置
     */
    private Map<String, ProviderConfig> providers;

    /**
     * 模型选择策略配置
     */
    private StrategyConfig strategy;

    /**
     * 配额配置
     */
    private DefaultQuotaConfig quota;

    /**
     * 熔断配置
     */
    private CircuitBreakerConfig circuitBreaker;

    @Data
    public static class ProviderConfig {
        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * API基础地址
         */
        private String baseUrl;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 模型列表
         */
        private List<ModelConfig> models;
    }

    @Data
    public static class ModelConfig {
        /**
         * 模型名称
         */
        private String name;

        /**
         * 显示名称
         */
        private String displayName;

        /**
         * 最大Token数
         */
        private Integer maxTokens;

        /**
         * 输入价格(元/千Token)
         */
        private BigDecimal inputPrice;

        /**
         * 输出价格(元/千Token)
         */
        private BigDecimal outputPrice;

        /**
         * 能力标识
         */
        private String capabilities;
    }

    @Data
    public static class StrategyConfig {
        /**
         * 默认策略
         */
        private String defaultStrategy;

        /**
         * 允许自动降级
         */
        private Boolean allowFallback;

        /**
         * 最大重试次数
         */
        private Integer maxRetries;

        /**
         * 重试间隔(ms)
         */
        private Integer retryDelay;
    }

    @Data
    public static class DefaultQuotaConfig {
        /**
         * 默认每日Token配额
         */
        private Integer defaultDailyTokens;

        /**
         * 默认每分钟请求配额
         */
        private Integer defaultRpm;

        /**
         * 配额检查模式
         */
        private String checkMode;
    }

    @Data
    public static class CircuitBreakerConfig {
        /**
         * 失败率阈值
         */
        private Integer failureRateThreshold;

        /**
         * 滑动窗口大小
         */
        private Integer slidingWindowSize;

        /**
         * 熔断持续时间(s)
         */
        private Integer waitDurationInOpenState;

        /**
         * 半开状态允许的调用次数
         */
        private Integer permittedCallsInHalfOpenState;
    }
}
