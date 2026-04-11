# EDAMS 部署指南

## 目录

- [概述](#概述)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [本地开发环境](#本地开发环境)
- [生产环境部署](#生产环境部署)
- [CI/CD流水线](#cicd流水线)
- [配置说明](#配置说明)
- [故障排除](#故障排除)

---

## 概述

企业数据资产管理系统（EDAMS）基于Spring Cloud Alibaba微服务架构，包含：

- **后端服务**: 29个微服务
  - edams-parent: 21个服务（网关、认证、权限、通知等）
  - services: 8个服务（元数据、血缘、质量、治理等）
- **前端应用**: React + UmiJS
- **移动端应用**: Flutter

---

## 环境要求

### 硬件要求

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发环境 | 4核+ | 8GB+ | 50GB+ |
| 测试环境 | 8核+ | 16GB+ | 100GB+ |
| 生产环境 | 16核+ | 32GB+ | 200GB+ |

### 软件要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | OpenJDK或Eclipse Temurin |
| Maven | 3.9+ | Java构建工具 |
| Node.js | 18+ | 前端构建 |
| Docker | 24+ | 容器化 |
| Docker Compose | 2.20+ | 本地开发 |
| Git | 2.40+ | 版本控制 |

### 基础设施服务

| 服务 | 版本 | 端口 | 说明 |
|------|------|------|------|
| MySQL | 8.0 | 3306 | 主数据库 |
| Redis | 7 | 6379 | 缓存 |
| Nacos | 2.2.3 | 8848 | 注册中心/配置中心 |
| Elasticsearch | 8.11 | 9200 | 搜索引擎 |
| Neo4j | 5.14 | 7474/7687 | 图数据库 |
| Kafka | 3.6 | 9092 | 消息队列 |
| Sentinel | 1.8.6 | 8858 | 流量控制 |

---

## 快速开始

### 1. 克隆代码

```bash
git clone https://github.com/enterprise/edams.git
cd edams
```

### 2. 启动基础设施

```bash
cd docker
docker-compose -f docker-compose.yml up -d
```

等待所有服务健康检查通过：

```bash
docker-compose ps
```

### 3. 构建后端服务

```bash
# 构建edams-parent
cd microservices/edams-parent
mvn clean package -DskipTests

# 构建services
cd ../services
mvn clean package -DskipTests
```

### 4. 构建前端

```bash
cd edams-web
npm install
npm run build:prod
```

### 5. 启动所有服务

```bash
# 使用Docker Compose启动完整环境
docker-compose -f docker-compose.dev.yml up -d
```

---

## 本地开发环境

### 使用Docker Compose

```bash
# 启动完整开发环境
docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose.dev.yml ps

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f

# 停止所有服务
docker-compose -f docker-compose.dev.yml down
```

### 单独启动服务

```bash
# 只启动基础设施
docker-compose -f docker-compose.yml up -d

# 启动特定微服务
docker-compose -f docker-compose.dev.yml up edams-gateway edams-auth
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:8000 | Web应用 |
| Nacos | http://localhost:8848/nacos | 注册中心 (nacos/nacos) |
| Sentinel | http://localhost:8858 | 流量控制 |
| Elasticsearch | http://localhost:9200 | 搜索引擎 |
| Kibana | http://localhost:5601 | ES可视化 |
| Neo4j | http://localhost:7474 | 图数据库 (neo4j/neo4j123) |
| Kafka UI | http://localhost:8090 | 消息队列管理 |
| Prometheus | http://localhost:9090 | 监控 |
| Grafana | http://localhost:3000 | 可视化 (admin/admin) |
| Jaeger | http://localhost:16686 | 链路追踪 |
| Zipkin | http://localhost:9412 | 备用链路追踪 |

---

## 生产环境部署

### 方式一：Kubernetes部署

#### 前置条件

- Kubernetes集群 1.28+
- Helm 3.12+
- kubectl配置完成

#### 部署步骤

```bash
# 1. 创建命名空间
kubectl create namespace edams-prod

# 2. 添加Helm仓库
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# 3. 部署基础设施 (使用Bitnami Charts)
helm install mysql bitnami/mysql -n edams-prod -f values/mysql.yaml
helm install redis bitnami/redis -n edams-prod -f values/redis.yaml

# 4. 部署EDAMS应用
helm install edams ./charts/edams -n edams-prod -f values/prod.yaml

# 5. 检查部署状态
kubectl get pods -n edams-prod
kubectl get svc -n edams-prod

# 6. 查看日志
kubectl logs -n edams-prod -l app=edams-gateway -f
```

#### 蓝绿部署

```bash
# 部署新版本
helm upgrade edams ./charts/edams -n edams-prod \
  --set image.tag=v2.0.0 \
  --wait --timeout 15m

# 回滚
helm rollback edams -n edams-prod
```

#### 滚动更新

```bash
# 更新镜像版本
kubectl set image deployment/edams-gateway \
  edams-gateway=registry.example.com/edams/edams-gateway:v2.0.0 \
  -n edams-prod

# 检查滚动更新状态
kubectl rollout status deployment/edams-gateway -n edams-prod
```

### 方式二：Docker Swarm部署

```bash
# 初始化Swarm
docker swarm init

# 部署堆栈
docker stack deploy -c docker-stack.yml edams

# 查看服务
docker service ls

# 查看日志
docker service logs edams_edams-gateway -f
```

---

## CI/CD流水线

### GitLab CI/CD

项目已配置完整的GitLab CI/CD流水线 (`.gitlab-ci.yml`)：

#### 流水线阶段

1. **build:backend-parent** - 构建edams-parent (21个服务)
2. **build:services** - 构建services (8个服务)
3. **build:frontend** - 构建React前端
4. **build:mobile** - 构建Flutter移动端
5. **test** - 运行测试
6. **security** - 安全扫描
7. **package** - Docker镜像打包
8. **deploy:test** - 部署测试环境
9. **deploy:staging** - 部署预发布环境
10. **deploy:prod** - 部署生产环境

#### 流水线配置

在GitLab Settings > CI/CD > Variables中配置：

| 变量名 | 说明 | 必需 |
|--------|------|------|
| `CI_REGISTRY` | Docker镜像仓库地址 | 是 |
| `CI_REGISTRY_USER` | 镜像仓库用户名 | 是 |
| `CI_REGISTRY_PASSWORD` | 镜像仓库密码 | 是 |
| `DINGTALK_TOKEN` | 钉钉机器人Token | 否 |

#### 触发流水线

```bash
# 推送到main分支自动触发
git push origin main

# 手动触发
git push -o ci.variable="DEPLOY_PROD=true" origin main
```

### Jenkins CI/CD

项目也提供了Jenkins声明式流水线 (`Jenkinsfile`)：

#### Jenkins配置要求

- Jenkins 2.450+
- 安装插件：
  - Docker Pipeline
  - Kubernetes CLI
  - SonarQube Scanner
  - Slack Notification
  - DingTalk

#### Jenkins凭据

| 凭据ID | 类型 | 说明 |
|--------|------|------|
| `docker-registry-url` | Secret Text | Docker镜像仓库地址 |
| `docker-registry-credentials` | Username/Password | 镜像仓库认证 |
| `sonar-host-url` | Secret Text | SonarQube地址 |
| `sonar-token` | Secret Text | SonarQube Token |
| `test-kubeconfig` | kubeconfig | 测试环境K8S配置 |
| `staging-kubeconfig` | kubeconfig | 预发布环境K8S配置 |
| `prod-kubeconfig` | kubeconfig | 生产环境K8S配置 |
| `slack-token` | Secret Text | Slack Token |
| `dingtalk-token` | Secret Text | 钉钉机器人Token |

---

## 配置说明

### 环境变量

#### 后端服务通用配置

```bash
# Spring Cloud Nacos
SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848
SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=nacos:8848

# 数据库
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/edams
SPRING_DATASOURCE_USERNAME=edams
SPRING_DATASOURCE_PASSWORD=edams123

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=redis123

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Elasticsearch
SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200
```

#### 前端配置

```bash
# API地址
REACT_APP_API_BASE_URL=http://localhost:8080
REACT_APP_ENV=development
```

### Nacos配置

在Nacos控制台导入配置：

```bash
# 导入配置命名空间
curl -X POST 'http://localhost:8848/nacos/v1/console/namespaces' \
  -d 'customNamespaceId=edams-prod&namespaceName=EDAMS-Production&namespaceDesc=EDAMS生产环境'
```

### Kubernetes配置示例

```yaml
# values/prod.yaml
replicaCount: 3

image:
  repository: registry.example.com/edams
  pullPolicy: IfNotPresent

resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 1000m
    memory: 1Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

persistence:
  enabled: true
  storageClass: "ssd"
  size: 20Gi
```

---

## 故障排除

### 常见问题

#### 1. 服务启动失败

```bash
# 查看服务日志
docker-compose logs -f <service-name>

# 检查健康状态
docker-compose ps

# 重启服务
docker-compose restart <service-name>
```

#### 2. 数据库连接失败

```bash
# 检查MySQL状态
docker-compose exec mysql mysql -u root -p

# 检查连接
nc -zv mysql 3306
```

#### 3. Kafka消息堆积

```bash
# 查看消费者组
docker-compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list

# 重置消费者偏移量
docker-compose exec kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group <group-id> \
  --reset-offsets \
  --to-earliest \
  --topic <topic-name> \
  --execute
```

#### 4. 内存不足

```bash
# 检查内存使用
docker stats

# 清理未使用的镜像
docker image prune -a

# 清理未使用的卷
docker volume prune
```

#### 5. 流水线构建失败

```bash
# 查看GitLab Runner日志
kubectl logs -n gitlab-runner -l app=gitlab-runner -f

# 重新触发流水线
git commit --allow-empty -m "Trigger CI" && git push
```

### 健康检查

```bash
# 检查所有服务健康状态
for service in gateway auth user; do
  echo "Checking edams-$service..."
  curl -sf http://localhost:8080/actuator/health && echo " OK" || echo " FAILED"
done
```

### 日志聚合

```bash
# 查看所有服务最近日志
docker-compose logs --tail=100

# 搜索错误日志
docker-compose logs | grep -i error

# 导出日志到文件
docker-compose logs > edams-logs-$(date +%Y%m%d).log
```

---

## 附录

### 服务端口映射

| 服务 | 端口 | 说明 |
|------|------|------|
| edams-gateway | 8080 | API网关 |
| edams-auth | 8081 | 认证服务 |
| edams-user | 8082 | 用户服务 |
| edams-permission | 8083 | 权限服务 |
| edams-notification | 8084 | 通知服务 |
| edams-knowledge | 8085 | 知识服务 |
| edams-llm | 8086 | 大模型服务 |
| edams-chatbot | 8087 | 对话服务 |
| edams-workflow | 8088 | 工作流服务 |
| edams-lifecycle | 8089 | 生命周期服务 |
| lineage-service | 8091 | 血缘服务 |
| quality-service | 8092 | 质量服务 |
| standard-service | 8093 | 标准服务 |
| governance-engine | 8094 | 治理引擎 |
| edams-frontend | 8000 | 前端 |

### 联系支持

- 运维团队: devops@enterprise.com
- 技术支持: support@enterprise.com
- 紧急联系: +86-xxx-xxxx-xxxx

---

*文档版本: 1.0.0*
*最后更新: 2026-04-11*
