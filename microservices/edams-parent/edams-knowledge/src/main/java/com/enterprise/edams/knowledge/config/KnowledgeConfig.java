package com.enterprise.edams.knowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 知识图谱配置属性类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "knowledge")
public class KnowledgeConfig {

    /**
     * 图谱配置
     */
    private GraphConfig graph = new GraphConfig();

    /**
     * 导入配置
     */
    private ImportConfig importConfig = new ImportConfig();

    @Data
    public static class GraphConfig {
        /**
         * 默认实体类型
         */
        private List<String> defaultEntityTypes;

        /**
         * 默认关系类型
         */
        private List<String> defaultRelationTypes;

        /**
         * 图谱查询深度限制
         */
        private Integer maxQueryDepth = 5;

        /**
         * 单次查询最大返回节点数
         */
        private Integer maxNodesPerQuery = 1000;
    }

    @Data
    public static class ImportConfig {
        /**
         * 批量导入大小
         */
        private Integer batchSize = 500;

        /**
         * 导入超时时间(秒)
         */
        private Integer timeout = 300;

        /**
         * 支持的文件类型
         */
        private List<String> supportedFileTypes;
    }
}
