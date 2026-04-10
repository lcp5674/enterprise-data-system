# 企业数据资产管理系统 - 系统部署文档

> 本文档为企业数据资产管理系统（EDAMS）的完整部署指南，涵盖开发、测试、生产环境的部署流程。

---

## 目录

1. [环境要求](#一、环境要求)
2. [基础设施部署](#二、基础设施部署)
3. [中间件服务部署](#三、中间件服务部署)
4. [后端微服务部署](#四、后端微服务部署)
5. [前端应用部署](#五、前端应用部署)
6. [移动端部署](#六、移动端部署)
7. [监控告警配置](#七、监控告警配置)
8. [系统验证](#八、系统验证)
9. [常见问题排查](#九、常见问题排查)

---

## 一、环境要求

### 1.1 硬件要求

#### 开发环境
| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核 | 8核 |
| 内存 | 8GB | 16GB |
| 磁盘 | 50GB SSD | 100GB SSD |
| 网络 | 100Mbps | 1Gbps |

#### 测试/生产环境
| 资源 | 测试环境 | 生产环境 |
|------|----------|----------|
| CPU | 8核 | 16核+ |
| 内存 | 16GB | 32GB+ |
| 磁盘 | 200GB SSD | 500GB SSD+ |
| 网络 | 1Gbps | 10Gbps |

### 1.2 软件要求

#### 必需组件
| 软件 | 版本 | 说明 |
|------|------|------|
| Java | 17 LTS | OpenJDK 或 Oracle JDK |
| Node.js | 18+ | 前端构建 |
| Maven | 3.9+ | 后端构建 |
| Docker | 24.0+ | 容器化部署 |
| Docker Compose | 2.20+ | 本地开发环境 |
| Git | 2.40+ | 代码管理 |

#### 可选组件
| 软件 | 版本 | 说明 |
|------|------|------|
| Kubernetes | 1.28+ | 生产环境编排 |
| Helm | 3.13+ | K8s包管理 |
| kubectl | 1.28+ | K8s命令行工具 |

### 1.3 操作系统支持

- ✅ Ubuntu 20.04/22.04 LTS
- ✅ CentOS 7/8
- ✅ Rocky Linux 8/9
- ✅ macOS 12+ (Apple Silicon/M1/M2/M3)
- ✅ Windows 11 + WSL2

### 1.4 端口规划

| 端口 | 服务 | 协议 | 说明 |
|------|------|------|------|
| 80 | Nginx | HTTP | 前端访问 |
| 443 | Nginx | HTTPS | 安全访问 |
| 8888 | API Gateway | HTTP | API网关 |
| 5432 | PostgreSQL | TCP | 主数据库 |
| 5433 | TimescaleDB | TCP | 时序数据库 |
| 6379 | Redis | TCP | 缓存服务 |
| 9200 | Elasticsearch | HTTP | 搜索引擎 |
| 7474 | Neo4j | HTTP | 图数据库 |
| 7687 | Neo4j | Bolt | 图数据库连接 |
| 9092 | Kafka | TCP | 消息队列 |
| 2181 | Zookeeper | TCP | Kafka依赖 |
| 8848 | Nacos | HTTP | 配置中心 |
| 9090 | Prometheus | HTTP | 监控系统 |
| 3000 | Grafana | HTTP | 可视化看板 |
| 16686 | Jaeger | HTTP | 链路追踪 |
| 5601 | Kibana | HTTP | 日志分析 |
| 3100 | Loki | HTTP | 日志收集 |
| 27017 | MongoDB | TCP | 文档数据库 |

---

## 二、基础设施部署

### 2.1 克隆代码仓库

```bash
# 克隆仓库
git clone https://github.com/lcp5674/enterprise-data-system.git
cd enterprise-data-system

# 切换到最新稳定分支
git checkout main
```

### 2.2 环境变量配置

```bash
# 创建环境变量文件
cat > .env << 'EOF'
# 镜像仓库
REGISTRY=your-registry.com/edams
TAG=1.0.0

# 数据库配置
POSTGRES_HOST=postgresql
POSTGRES_PORT=5432
POSTGRES_DB=edams_dev
POSTGRES_USER=edams_dev
POSTGRES_PASSWORD=edams_dev_123

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis_dev_123

# Nacos配置
NACOS_SERVER_ADDR=nacos
NACOS_PORT=8848
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos_dev_123

# Kafka配置
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# S3/MinIO配置
MINIO_ENDPOINT=minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
EOF
```

### 2.3 创建必要目录

```bash
# 创建数据目录
mkdir -p data/postgres data/redis data/elasticsearch data/neo4j data/kafka data/mongo

# 创建日志目录
mkdir -p logs/gateway logs/auth logs/user logs/asset logs/lineage logs/quality

# 设置权限
chmod -R 777 data logs
```

---

## 三、中间件服务部署

### 3.1 Docker Compose 部署（推荐）

#### 快速启动

```bash
# 进入开发环境目录
cd infrastructure/docker/dev

# 启动所有中间件服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f postgresql-dev
```

#### 分步启动（按依赖顺序）

```bash
# 1. 启动数据库服务
docker-compose up -d postgresql-dev redis-dev

# 2. 启动搜索引擎和图数据库
docker-compose up -d elasticsearch-dev neo4j-dev

# 3. 启动消息队列
docker-compose up -d zookeeper-dev kafka-dev

# 4. 启动配置中心
docker-compose up -d nacos-dev

# 5. 启动监控服务
docker-compose up -d prometheus-dev grafana-dev loki-dev jaeger-dev

# 6. 启动工具服务
docker-compose up -d minio-dev kafka-ui-dev pgadmin-dev
```

#### 验证服务健康

```bash
# 检查所有服务健康状态
for service in postgresql redis elasticsearch neo4j kafka nacos prometheus grafana; do
  echo "Checking $service..."
  docker-compose exec ${service}-dev healthcheck 2>/dev/null || echo "$service health unknown"
done

# 验证端口监听
netstat -tlnp | grep -E '5432|6379|9200|7474|9092|8848|9090|3000'
```

### 3.2 单组件部署

#### PostgreSQL 部署

```bash
# Docker 部署
docker run -d \
  --name edams-postgres \
  -e POSTGRES_DB=edams_dev \
  -e POSTGRES_USER=edams_dev \
  -e POSTGRES_PASSWORD=edams_dev_123 \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine

# 初始化数据库
docker exec -i edams-postgres psql -U edams_dev -d edams_dev < infrastructure/database/init.sql
```

#### Redis 部署

```bash
docker run -d \
  --name edams-redis \
  -p 6379:6379 \
  -e REDIS_PASSWORD=redis_dev_123 \
  -v redis_data:/data \
  redis:7.2-alpine redis-server --requirepass redis_dev_123
```

#### Elasticsearch 部署

```bash
docker run -d \
  --name edams-elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e discovery.type=single-node \
  -e ES_JAVA_OPTS=-Xms512m -Xmx512m \
  -e xpack.security.enabled=false \
  -v elasticsearch_data:/usr/share/elasticsearch/data \
  elasticsearch:8.11.0
```

#### Neo4j 部署

```bash
docker run -d \
  --name edams-neo4j \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/neo4j_dev_123 \
  -e NEO4J_server_memory_heap_max__size=1G \
  -v neo4j_data:/data \
  neo4j:5.15-community
```

#### Kafka 部署

```bash
# 启动 Zookeeper
docker run -d \
  --name edams-zookeeper \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper:7.5.0

# 启动 Kafka
docker run -d \
  --name edams-kafka \
  -p 9092:9092 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  confluentinc/cp-kafka:7.5.0
```

#### Nacos 部署

```bash
docker run -d \
  --name edams-nacos \
  -p 8848:8848 -p 9848:9848 -p 9849:9849 \
  -e MODE=standalone \
  -e SPRING_DATASOURCE_PLATFORM=sqlite \
  -e JVM_XMS=512m -e JVM_XMX=512m \
  nacos/nacos-server:v2.3.0
```

### 3.3 Kafka Topic 创建

```bash
# 连接到 Kafka 容器
docker exec -it edams-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic asset-events \
  --partitions 3 --replication-factor 1

# 创建更多 Topic
docker exec -it edams-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic quality-check-events \
  --partitions 3 --replication-factor 1

docker exec -it edams-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic notification-events \
  --partitions 3 --replication-factor 1

# 列出所有 Topic
docker exec -it edams-kafka kafka-topics \
  --bootstrap-server localhost:9092 --list
```

### 3.4 Nacos 配置导入

```bash
# 访问 Nacos 控制台
# 地址: http://localhost:8848/nacos
# 默认账号: nacos / nacos

# 导入配置文件（通过 Nacos 控制台）
# 命名空间: dev
# 配置内容: 见 microservices/edams-parent/src/main/resources/bootstrap.yml
```

---

## 四、后端微服务部署

### 4.1 环境准备

```bash
# 安装 Java 17
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# macOS
brew install openjdk@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 验证 Java 版本
java -version
# openjdk version "17.0.x"
```

### 4.2 编译构建

```bash
cd microservices/edams-parent

# 全量编译（跳过测试）
mvn clean package -DskipTests -U

# 编译特定模块
mvn clean package -pl edams-gateway -am -DskipTests
mvn clean package -pl edams-auth -am -DskipTests
```

### 4.3 服务启动顺序

```
启动顺序依赖图：
                    ┌─────────┐
                    │  Nacos  │
                    └────┬────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   ┌─────────┐      ┌─────────┐      ┌─────────┐
   │ Gateway │      │  Redis  │      │ Postgres│
   └────┬────┘      └─────────┘      └─────────┘
        │                │                │
        │                └────────┬────────┘
        │                         │
        ▼                         ▼
   ┌─────────────────────────────────┐
   │        Base Services            │
   │  (auth, user, permission)       │
   └──────────────┬──────────────────┘
                  │
                  ▼
   ┌─────────────────────────────────┐
   │     Domain Services             │
   │  (asset, lineage, quality...)   │
   └──────────────┬──────────────────┘
                  │
                  ▼
   ┌─────────────────────────────────┐
   │      Frontend Services          │
   │     (knowledge, llm...)         │
   └─────────────────────────────────┘
```

### 4.4 开发环境启动（IDE）

#### IntelliJ IDEA

1. 打开 `microservices/edams-parent/pom.xml`
2. 等待 Maven 索引完成
3. 右键 `edams-gateway` → Run
4. 依次启动其他服务

#### 启动配置示例

```bash
# Gateway (端口 8888)
-Dspring.profiles.active=dev \
-Dserver.port=8888 \
-Dnacos.server.addr=localhost:8848 \
-Dspring.datasource.url=jdbc:postgresql://localhost:5432/edams_dev

# Auth (端口 8081)
-Dspring.profiles.active=dev \
-Dserver.port=8081 \
-Dnacos.server.addr=localhost:8848 \
-Dspring.datasource.url=jdbc:postgresql://localhost:5432/edams_dev
```

### 4.5 Docker 容器部署

#### 构建镜像

```bash
# 设置镜像标签
export REGISTRY=docker.io/yourusername
export TAG=1.0.0

# 逐个构建服务镜像
cd microservices/edams-parent

# 构建网关镜像
docker build -t ${REGISTRY}/edams-gateway:${TAG} -f Dockerfile.gateway .

# 构建认证服务镜像
docker build -t ${REGISTRY}/edams-auth:${TAG} -f Dockerfile.auth .

# 构建其他服务...
```

#### Docker Compose 编排

```yaml
# infrastructure/docker/dev/services.yml
version: '3.8'

services:
  edams-gateway:
    image: ${REGISTRY}/edams-gateway:${TAG}
    container_name: edams-gateway
    ports:
      - "8888:8888"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - NACOS_SERVER_ADDR=nacos-dev:8848
    depends_on:
      - nacos-dev
      - redis-dev
    networks:
      - edams-dev

  edams-auth:
    image: ${REGISTRY}/edams-auth:${TAG}
    container_name: edams-auth
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql-dev:5432/edams_dev
    depends_on:
      - postgresql-dev
      - redis-dev
    networks:
      - edams-dev

networks:
  edams-dev:
    external: true
```

### 4.6 Kubernetes 部署

#### Helm Chart 部署

```bash
cd infrastructure/helm/edams

# 添加 Helm 依赖
helm dependency build

# 安装 EDAMS
helm install edams . \
  --namespace edams --create-namespace \
  --set global.environment=prod \
  --set global.registry.server=your-registry.com \
  --set global.domain=edams.your-company.com \
  --set gateway.replicaCount=3 \
  --set gateway.ingress.enabled=true

# 查看部署状态
kubectl get pods -n edams
kubectl get svc -n edams

# 查看日志
kubectl logs -n edams -l app=gateway -f
```

#### 更新部署

```bash
# 更新镜像版本
helm upgrade edams . \
  --set gateway.image.tag=v1.1.0 \
  --set auth.image.tag=v1.1.0 \
  -n edams

# 回滚到上一版本
helm rollback edams -n edams
```

---

## 五、前端应用部署

### 5.1 开发环境

```bash
cd edams-web

# 安装依赖
npm install

# 开发模式启动（Mock 数据）
npm run dev

# 开发模式启动（对接后端）
npm run dev:dev

# 访问地址: http://localhost:8000
```

### 5.2 构建生产版本

```bash
# 构建测试环境
npm run build:test

# 构建生产环境
npm run build:prod

# 构建产物在 dist/ 目录
ls -la dist/
```

### 5.3 Nginx 部署

```bash
# 构建产物复制到 Nginx
sudo cp -r dist/* /usr/share/nginx/html/

# 配置 Nginx
sudo cat > /etc/nginx/conf.d/edams.conf << 'EOF'
server {
    listen 80;
    server_name edams.your-company.com;

    root /usr/share/nginx/html;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;

        # 缓存静态资源
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # API 代理
    location /api/ {
        proxy_pass http://edams-gateway:8888/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket 支持
    location /ws/ {
        proxy_pass http://edams-gateway:8888/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# 测试并重载 Nginx
sudo nginx -t
sudo systemctl reload nginx
```

### 5.4 Docker 部署

```dockerfile
# Dockerfile.web
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build:prod

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```bash
# 构建并运行
docker build -t edams-web:1.0.0 -f Dockerfile.web .
docker run -d -p 8000:80 --name edams-web edams-web:1.0.0
```

---

## 六、移动端部署

### 6.1 环境准备

```bash
# 安装 Flutter SDK
# macOS
brew install flutter

# 验证 Flutter
flutter doctor
flutter doctor -v
```

### 6.2 项目配置

```bash
cd edams-mobile

# 获取依赖
flutter pub get

# 配置后端 API 地址
# 编辑 lib/config/app_config.dart
const String apiBaseUrl = 'http://your-api-server.com/api';
```

### 6.3 运行与构建

```bash
# 运行调试版本
flutter run

# 构建 Android APK
flutter build apk --release

# 构建 iOS
flutter build ios --release

# 构建 Web
flutter build web
```

### 6.4 Android 签名配置

```bash
# 生成签名密钥
keytool -genkey -v -keystore edams-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias edams

# 配置签名信息
# android/app/build.gradle
android {
    signingConfigs {
        release {
            keyAlias 'edams'
            keyPassword 'your-key-password'
            storeFile file('edams-release.jks')
            storePassword 'your-store-password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

---

## 七、监控告警配置

### 7.1 Prometheus 配置

```yaml
# infrastructure/monitoring/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  # 微服务指标
  - job_name: 'edams-microservices'
    kubernetes_sd_configs:
      - role: pod
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_label_app]
        regex: 'edams-.+'
        action: keep
      - source_labels: [__meta_kubernetes_pod_container_port_number]
        regex: "8080|8081|8082"
        action: keep

  # 中间件指标
  - job_name: 'middleware'
    static_configs:
      - targets:
          - 'postgres-exporter:9187'
          - 'redis-exporter:9121'
          - 'kafka-exporter:9308'
```

### 7.2 Grafana 数据源配置

```yaml
# infrastructure/monitoring/grafana/datasources/prometheus.yml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false
```

### 7.3 告警规则

```yaml
# infrastructure/monitoring/prometheus/rules/edams-alerts.yml
groups:
  - name: edams-alerts
    rules:
      - alert: ServiceDown
        expr: up{job="edams-microservices"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "服务 {{ $labels.instance }} 不可用"

      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "服务 {{ $labels.instance }} 内存使用率过高"
```

### 7.4 链路追踪配置

```yaml
# application.yml (Spring Boot)
spring:
  application:
    name: edams-gateway
  sleuth:
    sampler:
      probability: 1.0
    zipkin:
      base-url: http://jaeger:14268
```

---

## 八、系统验证

### 8.1 健康检查脚本

```bash
#!/bin/bash
# scripts/health-check.sh

BASE_URL=${1:-http://localhost}

echo "=== EDAMS 系统健康检查 ==="
echo "检查时间: $(date)"
echo ""

# 1. 检查中间件服务
check_service() {
    local name=$1
    local url=$2
    local expected=$3

    response=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    if [ "$response" = "$expected" ]; then
        echo "✅ $name: 正常 (HTTP $response)"
    else
        echo "❌ $name: 异常 (HTTP $response)"
    fi
}

echo "--- 中间件服务 ---"
check_service "PostgreSQL" "http://localhost:5432" "???"
check_service "Redis" "http://localhost:6379" "???"
check_service "Elasticsearch" "http://localhost:9200" "200"
check_service "Neo4j" "http://localhost:7474" "200"
check_service "Kafka" "http://localhost:9092" "???"
check_service "Nacos" "http://localhost:8848/nacos" "200"
check_service "Prometheus" "http://localhost:9090/-/healthy" "200"
check_service "Grafana" "http://localhost:3000/api/health" "200"

echo ""
echo "--- 微服务 ---"
check_service "API Gateway" "http://localhost:8888/actuator/health" "200"
check_service "Auth Service" "http://localhost:8081/actuator/health" "200"

echo ""
echo "--- 前端 ---"
check_service "Web UI" "http://localhost:8000" "200"
```

### 8.2 API 接口测试

```bash
# 1. 获取访问令牌
TOKEN=$(curl -s -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.data.token')

echo "获取 Token: ${TOKEN:0:50}..."

# 2. 获取仪表盘数据
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8888/api/dashboard/stats | jq .

# 3. 搜索资产
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8888/api/assets/search?keyword=test | jq .

# 4. 查看血缘
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8888/api/lineage/stats | jq .
```

### 8.3 验收检查清单

| 检查项 | 预期结果 | 实际结果 | 状态 |
|--------|----------|----------|------|
| PostgreSQL 连接 | 正常 | | ☐ |
| Redis 连接 | 正常 | | ☐ |
| Elasticsearch 连接 | 正常 | | ☐ |
| Neo4j 连接 | 正常 | | ☐ |
| Kafka 连接 | 正常 | | ☐ |
| Nacos 控制台 | 可访问 | | ☐ |
| 用户登录 | 成功 | | ☐ |
| 资产搜索 | 返回结果 | | ☐ |
| 资产详情 | 正常显示 | | ☐ |
| 血缘查看 | 正常显示 | | ☐ |
| 质量检测 | 正常执行 | | ☐ |
| 前端加载 | <3秒 | | ☐ |
| API 响应 | <200ms | | ☐ |

---

## 九、常见问题排查

### 9.1 数据库连接问题

```bash
# 问题: 连接 PostgreSQL 超时
# 排查步骤:

# 1. 检查服务状态
docker-compose ps postgresql-dev

# 2. 检查端口监听
netstat -tlnp | grep 5432

# 3. 检查日志
docker-compose logs postgresql-dev | tail -50

# 4. 测试连接
psql -h localhost -p 5432 -U edams_dev -d edams_dev

# 解决方案: 重启服务
docker-compose restart postgresql-dev
```

### 9.2 服务注册失败

```bash
# 问题: 微服务无法注册到 Nacos
# 排查步骤:

# 1. 检查 Nacos 状态
curl http://localhost:8848/nacos/v1/console/health/readiness

# 2. 检查网络连通性
docker exec -it edams-gateway ping nacos-dev

# 3. 检查配置
docker exec -it edams-gateway env | grep NACOS

# 解决方案: 检查网络和配置
docker-compose down && docker-compose up -d
```

### 9.3 Kafka 消息积压

```bash
# 问题: Kafka 消息消费延迟
# 排查步骤:

# 1. 查看 Topic 状态
docker exec -it edams-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list

# 2. 查看消费者组滞后
docker exec -it edams-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group edams-consumer \
  --describe

# 3. 查看 Topic 消息数量
docker exec -it edams-kafka kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe --topic asset-events

# 解决方案: 增加消费者数量
kubectl scale deployment edams-consumer --replicas=3 -n edams
```

### 9.4 前端无法访问后端 API

```bash
# 问题: 浏览器跨域错误
# 排查步骤:

# 1. 检查 Gateway CORS 配置
grep -A 10 "cors" microservices/edams-parent/edams-gateway/src/main/resources/application.yml

# 2. 检查 Nginx 代理配置
cat /etc/nginx/conf.d/edams.conf | grep -A 20 "location /api"

# 解决方案: 更新配置
# 1. Gateway 添加 CORS 配置
# 2. 或通过 Nginx 代理
```

### 9.5 内存溢出 (OOM)

```bash
# 问题: Java 服务内存溢出
# 排查步骤:

# 1. 查看容器内存使用
docker stats

# 2. 查看 JVM 堆内存
docker exec -it edams-gateway jstat -gc $(jps | grep gateway | awk '{print $1}')

# 3. 查看日志
docker logs edams-gateway | grep -i "outofmemory\|heap"

# 解决方案: 调整 JVM 参数
# docker-compose.yml
environment:
  - JAVA_OPTS=-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError
```

### 9.6 日志查看

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f edams-gateway

# 查看最近 100 行日志
docker-compose logs --tail=100 edams-auth

# 搜索错误日志
docker-compose logs | grep -i error

# 导出日志文件
docker-compose logs > edams-logs-$(date +%Y%m%d).log
```

---

## 附录

### A. 快速部署脚本

```bash
#!/bin/bash
# scripts/quick-deploy.sh

set -e

echo "=== EDAMS 快速部署脚本 ==="

# 1. 克隆代码
if [ ! -d "enterprise-data-system" ]; then
    git clone https://github.com/lcp5674/enterprise-data-system.git
fi
cd enterprise-data-system

# 2. 启动中间件
cd infrastructure/docker/dev
docker-compose up -d

# 3. 等待服务就绪
echo "等待服务启动..."
sleep 30

# 4. 启动后端
cd ../../..
cd microservices/edams-parent
./mvnw spring-boot:run -pl edams-gateway &

# 5. 启动前端
cd ../../edams-web
npm install
npm run dev &

echo "部署完成!"
echo "访问地址:"
echo "  前端: http://localhost:8000"
echo "  Gateway: http://localhost:8888"
echo "  Nacos: http://localhost:8848/nacos"
echo "  Grafana: http://localhost:3000"
```

### B. 联系方式

- **技术支持邮箱**: support@edams.com
- **技术支持电话**: 400-xxx-xxxx
- **在线文档**: https://docs.edams.com

---

**文档版本**: v1.0.0
**最后更新**: 2026-04-10
**维护团队**: 企业数据资产管理团队
