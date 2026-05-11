# EDAMS Docker 部署指南

## 环境要求

### 硬件要求
- CPU: 8核+
- 内存: 32GB+
- 磁盘: 100GB+ (取决于数据量)

### 软件要求
- Docker: 24.0+
- Docker Compose: 2.20+
- 操作系统: Ubuntu 20.04+ / CentOS 7+ / macOS

## 快速部署

### 1. 克隆项目
```bash
git clone <项目地址>
cd <项目目录>
```

### 2. 一键部署
```bash
cd docker
chmod +x deploy.sh
./deploy.sh
```

### 3. 验证部署
```bash
./health-check.sh
```

## 手动部署步骤

### 步骤1: 安装Docker (如未安装)
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | bash
sudo usermod -aG docker $USER

# CentOS/RHEL
curl -fsSL https://get.docker.com | bash
sudo systemctl start docker
sudo systemctl enable docker
```

### 步骤2: 安装Docker Compose
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 步骤3: 配置环境变量
```bash
cd docker
cp .env.example .env
# 编辑 .env 文件，根据需要修改配置
vim .env
```

### 步骤4: 构建并启动所有服务
```bash
# 构建所有镜像 (首次部署)
docker-compose build --no-cache

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 步骤5: 等待服务就绪
```bash
# 等待基础服务就绪 (约5-10分钟)
./wait-for-services.sh
```

### 步骤6: 验证服务健康
```bash
# 运行健康检查
./health-check.sh

# 查看所有服务日志
docker-compose logs -f
```

## 服务访问地址

### API服务
| 服务 | 端口 | 地址 |
|------|------|------|
| API Gateway | 8080 | http://localhost:8080 |
| 认证服务 | 8081 | http://localhost:8081 |
| 资产服务 | 8082 | http://localhost:8082 |
| 权限服务 | 8083 | http://localhost:8083 |
| 分析服务 | 8084 | http://localhost:8084 |
| 知识图谱 | 8085 | http://localhost:8085 |
| LLM服务 | 8086 | http://localhost:8086 |
| 通知服务 | 8087 | http://localhost:8087 |
| 工作流服务 | 8088 | http://localhost:8088 |
| 生命周期服务 | 8089 | http://localhost:8089 |
| AI运维服务 | 8090 | http://localhost:8090 |
| 智能助手 | 8091 | http://localhost:8091 |
| 智能分析 | 8092 | http://localhost:8092 |
| 用户服务 | 8093 | http://localhost:8093 |
| 协作服务 | 8094 | http://localhost:8094 |
| 报表服务 | 8095 | http://localhost:8095 |

### 管理和监控
| 服务 | 端口 | 地址 | 默认账号 |
|------|------|------|----------|
| Nacos | 8848 | http://localhost:8848/nacos | nacos/nacos |
| Sentinel | 8858 | http://localhost:8858 | sentinel/sentinel |
| Kibana | 5601 | http://localhost:5601 | - |
| Neo4j Browser | 7474 | http://localhost:7474 | neo4j/neo4j123 |
| Kafka UI | 8090 | http://localhost:8090 | - |
| Prometheus | 9090 | http://localhost:9090 | - |
| Grafana | 3000 | http://localhost:3000 | admin/admin |
| Jaeger | 16686 | http://localhost:16686 | - |

## 常用运维命令

### 启动服务
```bash
docker-compose up -d
```

### 停止服务
```bash
docker-compose down
```

### 重启服务
```bash
docker-compose restart
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f gateway

# 查看最近100行日志
docker-compose logs --tail=100
```

### 进入容器
```bash
docker exec -it edams-gateway bash
```

### 重新构建单个服务
```bash
docker-compose build --no-cache gateway
docker-compose up -d gateway
```

### 清理资源
```bash
# 停止并删除容器
docker-compose down

# 删除镜像
docker-compose down --rmi all

# 删除所有数据卷
docker-compose down -v
```

## 数据持久化

所有数据通过Docker Volume持久化：
- `mysql_data` - MySQL数据
- `redis_data` - Redis数据
- `es_data` - Elasticsearch数据
- `neo4j_data` - Neo4j数据
- `kafka_data` - Kafka数据
- `grafana_data` - Grafana数据

## 性能调优

### JVM内存配置
在`.env`文件中修改:
```env
JVM_XMS=1g
JVM_XMX=2g
```

### MySQL配置
编辑docker-compose.yml中的mysql服务command部分。

### Elasticsearch配置
```yaml
environment:
  - ES_JAVA_OPTS=-Xms2g -Xmx2g
```

## 故障排除

### 服务启动失败
```bash
# 查看详细日志
docker-compose logs <服务名>

# 检查端口占用
netstat -tlnp | grep <端口号>

# 重启单个服务
docker-compose restart <服务名>
```

### 数据库连接失败
```bash
# 检查MySQL健康状态
docker exec edams-mysql mysqladmin ping -h localhost -uroot -proot123456

# 查看MySQL日志
docker-compose logs mysql
```

### 内存不足
```bash
# 检查Docker资源使用
docker stats

# 增加Docker可用内存 (Docker Desktop设置)
```

## API测试

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

### 获取Token
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 卸载

```bash
# 停止所有服务
docker-compose down

# 删除所有数据卷 (谨慎操作)
docker-compose down -v

# 删除镜像
docker system prune -a
```
