package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.datasource.DatasourceConnectionInfo;
import com.enterprise.edams.analysis.datasource.DatasourceConnector;
import com.enterprise.edams.analysis.datasource.DatasourceConnectorFactory;
import com.enterprise.edams.analysis.exception.AnalysisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceConnectionService {

    private final DatasourceConnectorFactory connectorFactory;

    public DatasourceConnectionInfo getDatasourceConnectionInfo(Long datasourceId) {
        log.info("Getting datasource connection info for id: {}", datasourceId);

        try {
            return fetchDatasourceFromEDAMS(datasourceId);
        } catch (Exception e) {
            log.error("Failed to get datasource info: {}", e.getMessage(), e);
            throw new AnalysisException("DATASOURCE_FETCH_FAILED", "获取数据源信息失败: " + e.getMessage());
        }
    }

    public Connection getConnection(Long datasourceId) {
        DatasourceConnectionInfo connectionInfo = getDatasourceConnectionInfo(datasourceId);
        return getConnection(connectionInfo);
    }

    public Connection getConnection(DatasourceConnectionInfo connectionInfo) {
        try {
            DatasourceConnector connector = connectorFactory.getConnector(connectionInfo);
            return connector.getConnection(connectionInfo);
        } catch (Exception e) {
            log.error("Failed to connect to datasource: {}", e.getMessage(), e);
            throw new AnalysisException("DATASOURCE_CONNECTION_FAILED",
                    "连接数据源失败: " + connectionInfo.getDatasourceName() + " - " + e.getMessage());
        }
    }

    private DatasourceConnectionInfo fetchDatasourceFromEDAMS(Long datasourceId) {
        log.debug("Fetching datasource {} from EDAMS system", datasourceId);

        try {
            Object datasourceConfig = getDatasourceConfigFromSystem(datasourceId);
            
            if (datasourceConfig == null) {
                throw new AnalysisException("DATASOURCE_NOT_FOUND", 
                        "数据源不存在: " + datasourceId + "，请先在系统中配置数据源");
            }

            return convertToConnectionInfo(datasourceConfig);

        } catch (AnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching datasource from EDAMS: {}", e.getMessage(), e);
            throw new AnalysisException("DATASOURCE_FETCH_ERROR", 
                    "从EDAMS系统获取数据源配置失败: " + e.getMessage());
        }
    }

    private Object getDatasourceConfigFromSystem(Long datasourceId) {
        log.debug("Attempting to fetch datasource {} from EDAMS datasource service", datasourceId);
        
        try {
            Class<?> datasourceServiceClass = Class.forName(
                    "com.enterprise.edams.datasource.service.DatasourceConfigService");
            
            Object datasourceService = getSpringBean(datasourceServiceClass);
            
            if (datasourceService != null) {
                java.lang.reflect.Method getByIdMethod = datasourceServiceClass.getMethod("getById", Long.class);
                Object result = getByIdMethod.invoke(datasourceService, datasourceId);
                
                if (result != null) {
                    log.info("Successfully fetched datasource {} from EDAMS", datasourceId);
                    return result;
                }
            }
        } catch (ClassNotFoundException e) {
            log.warn("DatasourceConfigService not found in classpath, using fallback registry");
        } catch (Exception e) {
            log.warn("Could not fetch datasource from service, using fallback: {}", e.getMessage());
        }
        
        return getFromFallbackRegistry(datasourceId);
    }

    private Object getSpringBean(Class<?> beanClass) {
        try {
            Class<?> applicationContextClass = Class.forName("org.springframework.context.ApplicationContext");
            Object applicationContext = getStaticField(
                    Class.forName("org.springframework.beans.factory.annotation.ApplicationContextArgumentResolver"),
                    "APPLICATION_CONTEXT_ATTRIBUTE"
            );
            
            if (applicationContext == null) {
                try {
                    java.lang.reflect.Method getBeanMethod = applicationContextClass.getMethod("getBean", Class.class);
                    Object context = getSpringApplicationContext();
                    if (context != null) {
                        return getBeanMethod.invoke(context, beanClass);
                    }
                } catch (Exception e) {
                    log.debug("Could not get Spring bean: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("ApplicationContext access failed: {}", e.getMessage());
        }
        return null;
    }

    private Object getSpringApplicationContext() {
        try {
            Class<?> contextHolderClass = Class.forName(
                    "org.springframework.web.context.support.WebApplicationContextUtils");
            java.lang.reflect.Method getRequiredWebApplicationContextMethod = 
                    contextHolderClass.getMethod("getRequiredWebApplicationContext", 
                            Class.forName("jakarta.servlet.ServletContext"));
            return getRequiredWebApplicationContextMethod.invoke(null, getServletContext());
        } catch (Exception e) {
            log.debug("Could not get WebApplicationContext: {}", e.getMessage());
        }
        
        try {
            Class<?>玄Class = Class.forName("org.springframework.context.ApplicationContextProvider");
            java.lang.reflect.Method getMethod = 玄Class.getMethod("getContext");
            return getMethod.invoke(null);
        } catch (Exception e) {
            log.debug("Could not get ApplicationContext from provider: {}", e.getMessage());
        }
        
        return null;
    }

    private Object getServletContext() {
        return null;
    }

    private Object getStaticField(Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private DatasourceConnectionInfo getFromFallbackRegistry(Long datasourceId) {
        log.debug("Using fallback registry for datasource: {}", datasourceId);
        
        return DatasourceRegistry.getDatasource(datasourceId);
    }

    private DatasourceConnectionInfo convertToConnectionInfo(Object datasourceConfig) {
        if (datasourceConfig == null) {
            return null;
        }

        try {
            DatasourceConnectionInfo.DatasourceConnectionInfoBuilder builder = DatasourceConnectionInfo.builder();

            java.lang.reflect.Method[] methods = datasourceConfig.getClass().getMethods();
            
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName();
                
                try {
                    if (methodName.equals("getId") || methodName.equals("getDatasourceId")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof Long) {
                            builder.datasourceId((Long) value);
                        }
                    } else if (methodName.equals("getDatasourceName") || methodName.equals("getName")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.datasourceName((String) value);
                        }
                    } else if (methodName.equals("getDatasourceCode") || methodName.equals("getCode")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.datasourceCode((String) value);
                        }
                    } else if (methodName.equals("getDatasourceType") || methodName.equals("getType")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.datasourceType((String) value);
                        }
                    } else if (methodName.equals("getHost")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.host((String) value);
                        }
                    } else if (methodName.equals("getPort")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof Integer) {
                            builder.port((Integer) value);
                        } else if (value instanceof Long) {
                            builder.port(((Long) value).intValue());
                        }
                    } else if (methodName.equals("getDatabaseName") || methodName.equals("getDatabase") || methodName.equals("getDbName")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.databaseName((String) value);
                        }
                    } else if (methodName.equals("getUsername") || methodName.equals("getUser")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.username((String) value);
                        }
                    } else if (methodName.equals("getPassword")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.password((String) value);
                        }
                    } else if (methodName.equals("getJdbcUrl")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.jdbcUrl((String) value);
                        }
                    } else if (methodName.equals("getSchema") || methodName.equals("getSchemaName")) {
                        Object value = method.invoke(datasourceConfig);
                        if (value instanceof String) {
                            builder.schema((String) value);
                        }
                    }
                } catch (Exception e) {
                    log.trace("Could not invoke method {}: {}", methodName, e.getMessage());
                }
            }

            DatasourceConnectionInfo info = builder.build();
            
            if (info.getJdbcUrl() == null || info.getJdbcUrl().isEmpty()) {
                info.setJdbcUrl(info.getJdbcUrlTemplate());
            }

            log.info("Converted datasource config to connection info: {}", info.getDatasourceName());
            return info;

        } catch (Exception e) {
            log.error("Failed to convert datasource config to connection info: {}", e.getMessage(), e);
            throw new AnalysisException("CONVERSION_ERROR", "数据源配置转换失败: " + e.getMessage());
        }
    }

    public boolean testConnection(Long datasourceId) {
        try (Connection conn = getConnection(datasourceId)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            log.warn("Datasource connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public static class DatasourceRegistry {
        private static final java.util.Map<Long, DatasourceConnectionInfo> datasources = 
                new java.util.concurrent.ConcurrentHashMap<>();

        static {
            initializeDefaultDatasources();
        }

        private DatasourceRegistry() {
        }

        private static void initializeDefaultDatasources() {
            String mysqlHost = System.getenv("MYSQL_HOST");
            String mysqlPort = System.getenv("MYSQL_PORT");
            String mysqlDb = System.getenv("MYSQL_DATABASE");
            String mysqlUser = System.getenv("MYSQL_USER");
            String mysqlPass = System.getenv("MYSQL_PASSWORD");

            if (mysqlHost == null) mysqlHost = "localhost";
            if (mysqlPort == null) mysqlPort = "3306";
            if (mysqlDb == null) mysqlDb = "edams";
            if (mysqlUser == null) mysqlUser = "root";
            if (mysqlPass == null) mysqlPass = "root";

            datasources.put(1L, DatasourceConnectionInfo.builder()
                    .datasourceId(1L)
                    .datasourceCode("MYSQL_PROD")
                    .datasourceName("MySQL生产库")
                    .datasourceType("MYSQL")
                    .host(mysqlHost)
                    .port(Integer.parseInt(mysqlPort))
                    .databaseName(mysqlDb)
                    .username(mysqlUser)
                    .password(mysqlPass)
                    .additionalParams("useSSL=false&serverTimezone=Asia/Shanghai")
                    .build());

            String pgHost = System.getenv("POSTGRES_HOST");
            String pgPort = System.getenv("POSTGRES_PORT");
            String pgDb = System.getenv("POSTGRES_DATABASE");
            String pgUser = System.getenv("POSTGRES_USER");
            String pgPass = System.getenv("POSTGRES_PASSWORD");

            if (pgHost == null) pgHost = "localhost";
            if (pgPort == null) pgPort = "5432";
            if (pgDb == null) pgDb = "edams";
            if (pgUser == null) pgUser = "postgres";
            if (pgPass == null) pgPass = "postgres";

            datasources.put(2L, DatasourceConnectionInfo.builder()
                    .datasourceId(2L)
                    .datasourceCode("POSTGRES_PROD")
                    .datasourceName("PostgreSQL生产库")
                    .datasourceType("POSTGRESQL")
                    .host(pgHost)
                    .port(Integer.parseInt(pgPort))
                    .databaseName(pgDb)
                    .username(pgUser)
                    .password(pgPass)
                    .build());

            String chHost = System.getenv("CLICKHOUSE_HOST");
            String chPort = System.getenv("CLICKHOUSE_PORT");
            String chDb = System.getenv("CLICKHOUSE_DATABASE");
            String chUser = System.getenv("CLICKHOUSE_USER");
            String chPass = System.getenv("CLICKHOUSE_PASSWORD");

            if (chHost == null) chHost = "localhost";
            if (chPort == null) chPort = "8123";
            if (chDb == null) chDb = "default";
            if (chUser == null) chUser = "default";
            if (chPass == null) chPass = "";

            datasources.put(3L, DatasourceConnectionInfo.builder()
                    .datasourceId(3L)
                    .datasourceCode("CLICKHOUSE_PROD")
                    .datasourceName("ClickHouse分析库")
                    .datasourceType("CLICKHOUSE")
                    .host(chHost)
                    .port(Integer.parseInt(chPort))
                    .databaseName(chDb)
                    .username(chUser)
                    .password(chPass)
                    .build());

            log.info("Initialized {} datasources from environment variables", datasources.size());
        }

        public static void registerDatasource(DatasourceConnectionInfo datasource) {
            datasources.put(datasource.getDatasourceId(), datasource);
            log.info("Registered datasource: {} ({})", datasource.getDatasourceName(), datasource.getDatasourceCode());
        }

        public static DatasourceConnectionInfo getDatasource(Long datasourceId) {
            DatasourceConnectionInfo info = datasources.get(datasourceId);
            
            if (info == null) {
                throw new AnalysisException("DATASOURCE_NOT_FOUND", 
                        "数据源不存在: " + datasourceId + 
                        "，请检查数据源ID是否正确，或在系统中配置该数据源");
            }
            
            return info;
        }

        public static java.util.List<DatasourceConnectionInfo> getAllDatasources() {
            return new java.util.ArrayList<>(datasources.values());
        }

        public static void removeDatasource(Long datasourceId) {
            datasources.remove(datasourceId);
            log.info("Removed datasource: {}", datasourceId);
        }

        public static boolean contains(Long datasourceId) {
            return datasources.containsKey(datasourceId);
        }
    }
}
