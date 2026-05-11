package com.enterprise.edams.analysis.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DatasourceConnectorFactory {

    private final Map<String, DatasourceConnector> connectors = new HashMap<>();

    public DatasourceConnectorFactory(
            MySQLConnector mysqlConnector,
            PostgreSQLConnector postgresqlConnector,
            ClickHouseConnector clickhouseConnector) {

        registerConnector(mysqlConnector);
        registerConnector(postgresqlConnector);
        registerConnector(clickhouseConnector);
    }

    private void registerConnector(DatasourceConnector connector) {
        connectors.put(connector.getConnectorType().toUpperCase(), connector);
        log.info("Registered datasource connector: {}", connector.getConnectorType());
    }

    public DatasourceConnector getConnector(String datasourceType) {
        DatasourceConnector connector = connectors.get(datasourceType.toUpperCase());
        if (connector == null) {
            log.warn("No connector found for datasource type: {}, falling back to MySQL", datasourceType);
            return connectors.get("MYSQL");
        }
        return connector;
    }

    public DatasourceConnector getConnector(DatasourceConnectionInfo connectionInfo) {
        return getConnector(connectionInfo.getDatasourceType());
    }

    public List<String> getSupportedTypes() {
        return List.copyOf(connectors.keySet());
    }

    public void registerConnector(DatasourceConnector connector, String type) {
        connectors.put(type.toUpperCase(), connector);
    }
}
