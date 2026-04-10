package com.enterprise.edams.datasource.connector;

import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.connector.impl.MysqlConnector;
import com.enterprise.edams.datasource.connector.impl.PostgresqlConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源连接器工厂
 */
@Slf4j
@Component
public class DatasourceConnectorFactory {

    private final Map<String, DatasourceConnector> connectorMap = new ConcurrentHashMap<>();

    public DatasourceConnectorFactory() {
        // 注册默认连接器
        registerConnector(new MysqlConnector());
        registerConnector(new PostgresqlConnector());
        // 后续可注册更多连接器
    }

    /**
     * 注册连接器
     *
     * @param connector 连接器实例
     */
    public void registerConnector(DatasourceConnector connector) {
        connectorMap.put(connector.getDatasourceType(), connector);
        log.info("注册数据源连接器: {}", connector.getDatasourceType());
    }

    /**
     * 获取连接器
     *
     * @param datasourceType 数据源类型
     * @return 连接器实例
     */
    public DatasourceConnector getConnector(String datasourceType) {
        DatasourceConnector connector = connectorMap.get(datasourceType);
        if (connector == null) {
            throw new IllegalArgumentException("不支持的数据源类型: " + datasourceType);
        }
        return connector;
    }

    /**
     * 获取所有支持的连接器类型
     *
     * @return 连接器类型列表
     */
    public Map<String, String> getSupportedTypes() {
        Map<String, String> types = new HashMap<>();
        for (DatasourceType type : DatasourceType.values()) {
            types.put(type.name(), type.getDescription());
        }
        return types;
    }

    /**
     * 检查是否支持指定类型
     *
     * @param datasourceType 数据源类型
     * @return 是否支持
     */
    public boolean isSupported(String datasourceType) {
        try {
            DatasourceType.valueOf(datasourceType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
