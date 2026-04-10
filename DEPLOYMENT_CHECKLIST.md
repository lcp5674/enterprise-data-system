# 企业数据资产管理系统 - 部署检查清单

## 部署前检查

### 1. 环境要求

| 组件 | 最低版本 | 推荐版本 | 检查命令 |
|------|----------|----------|----------|
| Java | 17 | 17 LTS | `java -version` |
| Node.js | 18 | 20 LTS | `node -v` |
| Docker | 24.0 | 24.0+ | `docker --version` |
| Kubernetes | 1.28 | 1.28+ | `kubectl version` |
| Helm | 3.13 | 3.14+ | `helm version` |

### 2. 基础设施服务

| 服务 | 端口 | 健康检查URL | 必需 |
|------|------|-------------|------|
| PostgreSQL | 5432 | `pg_isready` | ✅ |
| Redis | 6379 | `redis-cli ping` | ✅ |
| Elasticsearch | 9200 | `curl localhost:9200` | ✅ |
| Neo4j | 7474 | `curl localhost:7474` | ✅ |
| Kafka | 9092 | `kafka-topics --list` | ✅ |
| Nacos | 8848 | `curl localhost:8848/nacos` | ✅ |
| Prometheus | 9090 | `curl localhost:9090/-/healthy` | ✅ |
| Grafana | 3000 | `curl localhost:3000/api/health` | ✅ |

### 3. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE edams_dev;
CREATE DATABASE edams_metrics;

-- 创建用户
CREATE USER edams_dev WITH PASSWORD 'edams_dev_123';
GRANT ALL PRIVILEGES ON DATABASE edams_dev TO edams_dev;
```

### 4. Docker Compose 启动

```bash
cd infrastructure/docker/dev

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f postgresql-dev
docker-compose logs -f nacos-dev
```

### 5. 后端服务启动

```bash
cd microservices/edams-parent

# 编译所有模块
mvn clean package -DskipTests

# 启动网关服务（端口8888）
java -jar edams-gateway/target/edams-gateway.jar &

# 启动认证服务（端口8081）
java -jar edams-auth/target/edams-auth.jar &

# 启动其他微服务...
```

### 6. 前端启动

```bash
cd edams-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问地址: http://localhost:8000
```

### 7. 部署验证

```bash
# 1. 检查网关健康
curl http://localhost:8888/actuator/health

# 2. 检查认证服务
curl http://localhost:8081/actuator/health

# 3. 登录测试
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 4. 检查前端
curl http://localhost:8000
```

### 8. Kubernetes 部署（生产环境）

```bash
cd infrastructure/helm/edams

# 添加 Helm 仓库
helm repo add bitnami https://charts.bitnami.com/bitnami

# 安装 EDAMS
helm install edams . -n edams --create-namespace \
  --set global.registry.server=your-registry.com \
  --set global.domain=edams.your-company.com
```

### 9. 监控验证

| 监控项 | 地址 | 说明 |
|--------|------|------|
| Prometheus | http://localhost:9090 | 指标收集 |
| Grafana | http://localhost:3000 | 可视化看板 |
| Kibana | http://localhost:5601 | 日志分析 |
| Jaeger | http://localhost:16686 | 链路追踪 |
| Nacos | http://localhost:8848 | 配置中心 |

### 10. 部署完成确认

- [ ] 所有中间件服务运行正常
- [ ] 后端微服务注册到 Nacos
- [ ] 前端可正常访问
- [ ] 用户可以登录系统
- [ ] 可以搜索和查看资产
- [ ] 监控面板显示正常数据

## 回滚方案

```bash
# Docker Compose 回滚
docker-compose down
docker-compose -f docker-compose.backup.yml up -d

# Kubernetes 回滚
helm rollback edams -n edams
```
