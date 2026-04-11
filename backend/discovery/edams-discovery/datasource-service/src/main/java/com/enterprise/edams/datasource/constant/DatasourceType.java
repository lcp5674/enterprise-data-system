package com.enterprise.edams.datasource.constant;

/**
 * 数据源类型枚举
 */
public enum DatasourceType {
    /**
     * MySQL数据库
     */
    MYSQL("MySQL", "jdbc:mysql://${host}:${port}/${database}"),
    
    /**
     * Oracle数据库
     */
    ORACLE("Oracle", "jdbc:oracle:thin:@${host}:${port}:${database}"),
    
    /**
     * PostgreSQL数据库
     */
    POSTGRESQL("PostgreSQL", "jdbc:postgresql://${host}:${port}/${database}"),
    
    /**
     * SQL Server数据库
     */
    SQLSERVER("SQL Server", "jdbc:sqlserver://${host}:${port};DatabaseName=${database}"),
    
    /**
     * MongoDB数据库
     */
    MONGODB("MongoDB", "mongodb://${host}:${port}/${database}"),
    
    /**
     * Hive数据仓库
     */
    HIVE("Hive", "jdbc:hive2://${host}:${port}/${database}"),
    
    /**
     * Spark计算引擎
     */
    SPARK("Spark", "jdbc:hive2://${host}:${port}"),
    
    /**
     * Kafka消息队列
     */
    KAFKA("Kafka", "${host}:${port}"),
    
    /**
     * Elasticsearch搜索引擎
     */
    ELASTICSEARCH("Elasticsearch", "http://${host}:${port}"),
    
    /**
     * Redis缓存
     */
    REDIS("Redis", "${host}:${port}"),
    
    /**
     * S3对象存储
     */
    S3("Amazon S3", "s3://${bucket}"),
    
    /**
     * HDFS文件系统
     */
    HDFS("HDFS", "hdfs://${host}:${port}"),
    
    /**
     * REST API接口
     */
    REST_API("REST API", "${host}:${port}"),
    
    /**
     * 阿里云MaxCompute
     */
    MAXCOMPUTE("MaxCompute", "http://service.odps.aliyun.com/api/${project}"),
    
    /**
     * 阿里云DataHub
     */
    DATAHUB("DataHub", "http://dh-${region}.aliyuncs.com:8080");

    private final String description;
    private final String defaultConnectionPattern;

    DatasourceType(String description, String connectionPattern) {
        this.description = description;
        this.defaultConnectionPattern = connectionPattern;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultConnectionPattern() {
        return defaultConnectionPattern;
    }
}
