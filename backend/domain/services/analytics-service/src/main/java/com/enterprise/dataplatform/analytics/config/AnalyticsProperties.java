package com.enterprise.dataplatform.analytics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Analytics service configuration properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "analytics")
public class AnalyticsProperties {

    private AssetConfig asset = new AssetConfig();
    private QualityConfig quality = new QualityConfig();
    private BehaviorConfig behavior = new BehaviorConfig();

    @Data
    public static class AssetConfig {
        private HeatmapConfig heatmap = new HeatmapConfig();
        private TrendingConfig trending = new TrendingConfig();

        @Data
        public static class HeatmapConfig {
            private int topN = 100;
            private int timeRangeDays = 30;
        }

        @Data
        public static class TrendingConfig {
            private int updateIntervalSeconds = 300;
            private int minAccessCount = 10;
        }
    }

    @Data
    public static class QualityConfig {
        private TrendConfig trend = new TrendConfig();

        @Data
        public static class TrendConfig {
            private int aggregationIntervalMinutes = 60;
            private int retentionDays = 90;
        }
    }

    @Data
    public static class BehaviorConfig {
        private int sessionTimeoutMinutes = 30;
        private int maxEventsPerSession = 1000;
        private int analysisWindowDays = 7;
    }
}
