# EDAMS 生产环境部署指南

**项目名称**：企业数据资产管理系统（EDAMS）  
**部署版本**：v1.0  
**部署日期**：2026-05-11  
**文档维护人**：EDAMS运维团队  

---

## 一、部署概述

### 1.1 文档目的

本文档旨在为EDAMS项目提供完整的生产环境部署指导，确保部署过程规范化、标准化，最大限度降低生产环境部署风险。

### 1.2 适用范围

- EDAMS项目生产环境部署
- EDAMS项目测试环境部署
- EDAMS项目灾难恢复部署

### 1.3 部署前准备

**前提条件**：
- Kubernetes集群已部署并正常运行
- 所需中间件已部署（MySQL、Redis、Kafka等）
- Docker Registry已配置并可访问
- 域名和SSL证书已配置

**资源要求**：

| 组件 | CPU | 内存 | 磁盘 | 节点数 |
|------|-----|------|------|--------|
| K8s Master | 4核 | 8GB | 100GB | 3 |
| K8s Worker | 8核 | 16GB | 200GB | 5+ |
| MySQL | 8核 | 32GB | 500GB | 2 |
| Redis | 4核 | 16GB | 100GB | 3 |
| Kafka | 8核 | 16GB | 1TB | 3 |
| Elasticsearch | 8核 | 32GB | 1TB | 3 |
| Neo4j | 8核 | 32GB | 500GB | 3 |

---

## 二、环境配置

### 2.1 创建Kubernetes命名空间

```bash
# 创建生产环境命名空间
kubectl create namespace edams-prod

# 创建开发环境命名空间
kubectl create namespace edams-dev

# 创建测试环境命名空间
kubectl create namespace edams-test
```

### 2.2 配置镜像仓库密钥

```bash
# 创建镜像仓库密钥
kubectl create secret docker-registry edams-registry-secret \
  --docker-server=registry.example.com \
  --docker-username=${REGISTRY_USER} \
  --docker-password=${REGISTRY_PASSWORD} \
  --docker-email=${REGISTRY_EMAIL} \
  -n edams-prod

# 验证密钥创建
kubectl get secret edams-registry-secret -n edams-prod
```

### 2.3 配置存储类

```bash
# 查看可用存储类
kubectl get storageclass

# 创建NFS存储类（如果不存在）
cat > nfs-storage-class.yaml << EOF
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: nfs-storage
provisioner: nfs.io/provisioner
parameters:
  archiveOnDelete: "false"
reclaimPolicy: Retain
volumeBindingMode: Immediate
EOF

kubectl apply -f nfs-storage-class.yaml
```

### 2.4 配置ConfigMap和Secret

```bash
# 创建Nacos配置
cat > nacos-config.yaml << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: nacos-config
  namespace: edams-prod
data:
  mysql.host: "mysql-master.edams-prod.svc"
  mysql.port: "3306"
  mysql.database: "edams"
  redis.host: "redis-cluster.edams-prod.svc"
  redis.port: "6379"
  kafka.brokers: "kafka-0.edams-prod.svc:9092,kafka-1.edams-prod.svc:9092,kafka-2.edams-prod.svc:9092"
EOF

kubectl apply -f nacos-config.yaml

# 创建数据库密钥
kubectl create secret generic edams-mysql-secret \
  --from-literal=username=edams_user \
  --from-literal=password=${MYSQL_PASSWORD} \
  -n edams-prod

# 创建Redis密钥
kubectl create secret generic edams-redis-secret \
  --from-literal=password=${REDIS_PASSWORD} \
  -n edams-prod

# 创建JWT密钥
kubectl create secret generic edams-jwt-secret \
  --from-literal=jwt-secret=${JWT_SECRET} \
  --from-literal=jwt-expiration=86400000 \
  -n edams-prod
```

---

## 三、Helm部署

### 3.1 添加Helm仓库

```bash
# 添加EDAMS Helm仓库
helm repo add edams https://charts.edams.example.com
helm repo update

# 确认仓库添加成功
helm search repo edams
```

### 3.2 配置values文件

```bash
# 创建生产环境values文件
cat > values-prod.yaml << 'EOF'
# 全局配置
global:
  imageRegistry: registry.example.com
  imagePullSecrets: edams-registry-secret
  storageClass: nfs-storage
  
  # 环境配置
  env: prod
  timezone: Asia/Shanghai
  
  # 日志配置
  logLevel: INFO
  logFormat: json

# 数据库配置
mysql:
  enabled: false  # 使用外部MySQL
  externalHost: mysql-master.edams-prod.svc
  externalPort: 3306
  database: edams
  username: edams_user
  existingSecret: edams-mysql-secret
  connectionPool:
    minimumIdle: 10
    maximumPoolSize: 50
    connectionTimeout: 30000
    idleTimeout: 600000

# Redis配置
redis:
  enabled: false  # 使用外部Redis
  externalHost: redis-cluster.edams-prod.svc
  externalPort: 6379
  password: ""
  existingSecret: edams-redis-secret
  cluster:
    enabled: true
    nodes: 3
  connectionPool:
    maxTotal: 100
    maxIdle: 50
    minIdle: 10

# Kafka配置
kafka:
  enabled: false  # 使用外部Kafka
  brokers:
    - kafka-0.edams-prod.svc:9092
    - kafka-1.edams-prod.svc:9092
    - kafka-2.edams-prod.svc:9092
  topicPrefix: edams-prod

# Nacos配置
nacos:
  serverAddress: nacos.edams-prod.svc
  port: 8848
  namespace: prod
  username: nacos
  password: ${NACOS_PASSWORD}

# 服务网关配置
gateway:
  replicaCount: 3
  service:
    type: ClusterIP
  ingress:
    enabled: true
    className: nginx
    host: edams.example.com
    path: /
    tls:
      enabled: true
      secretName: edams-tls-secret
  resources:
    requests:
      cpu: 500m
      memory: 1Gi
    limits:
      cpu: 2000m
      memory: 2Gi
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10
    targetCPUUtilizationPercentage: 70
    targetMemoryUtilizationPercentage: 80

# 认证服务配置
auth:
  replicaCount: 3
  jwt:
    existingSecret: edams-jwt-secret
  oauth2:
    providers:
      - name: ldap
        enabled: true
        url: ldap://ldap.example.com:389
        baseDn: dc=example,dc=com
      - name: cas
        enabled: false

# 核心服务配置
services:
  asset:
    replicaCount: 3
    resources:
      requests:
        cpu: 1000m
        memory: 2Gi
      limits:
        cpu: 4000m
        memory: 4Gi
  
  lifecycle:
    replicaCount: 2
    resources:
      requests:
        cpu: 500m
        memory: 1Gi
      limits:
        cpu: 2000m
        memory: 2Gi
  
  lineage:
    replicaCount: 2
    neo4j:
      host: neo4j.edams-prod.svc
      port: 7687
      username: neo4j
      password: ${NEO4J_PASSWORD}
  
  governance:
    replicaCount: 2
    resources:
      requests:
        cpu: 1000m
        memory: 2Gi
      limits:
        cpu: 4000m
        memory: 4Gi
  
  quality:
    replicaCount: 2
    resources:
      requests:
        cpu: 1000m
        memory: 2Gi
      limits:
        cpu: 4000m
        memory: 4Gi
  
  ethics:
    replicaCount: 2
  
  classification:
    replicaCount: 2
  
  masking:
    replicaCount: 2
  
  iotEdge:
    replicaCount: 2
    mqtt:
      brokerUrl: tcp://mqtt.example.com:1883
      username: ${MQTT_USERNAME}
      password: ${MQTT_PASSWORD}
    mongodb:
      host: mongodb.edams-prod.svc
      port: 27017
      database: iot_edge
  
  llm:
    replicaCount: 2
    providers:
      openai:
        enabled: true
        apiKey: ${OPENAI_API_KEY}
        baseUrl: https://api.openai.com/v1
      zhipu:
        enabled: true
        apiKey: ${ZHIPU_API_KEY}
        baseUrl: https://open.bigmodel.cn/api/paas/v4
      qwen:
        enabled: true
        apiKey: ${QWEN_API_KEY}
        baseUrl: https://dashscope.aliyuncs.com/api/v1
  
  notification:
    replicaCount: 2
    channels:
      email:
        enabled: true
        smtp:
          host: smtp.example.com
          port: 465
          username: ${SMTP_USERNAME}
          password: ${SMTP_PASSWORD}
      webhook:
        enabled: true

# Elasticsearch配置
elasticsearch:
  enabled: false
  externalHosts:
    - es-0.edams-prod.svc:9200
    - es-1.edams-prod.svc:9200
    - es-2.edams-prod.svc:9200
  indexPrefix: edams-prod

# 监控配置
monitoring:
  enabled: true
  prometheus:
    endpoint: http://prometheus.monitoring.svc:9090
  grafana:
    adminPassword: ${GRAFANA_PASSWORD}
  alerting:
    enabled: true
    receivers:
      - name: email
        email:
          to: ops@example.com
      - name: webhook
        webhook:
          url: ${ALERT_WEBHOOK_URL}

# 日志收集配置
logging:
  enabled: true
  elasticsearch:
    host: es-0.edams-prod.svc
    port: 9200
    indexPrefix: edams-prod-logs
  retention:
    days: 30

# 链路追踪配置
tracing:
  enabled: true
  apm:
    serverUrl: http://skywalking-oap.monitoring.svc:11800
    serviceName: edams
  samplingRate: 0.1

# Pod配置
pod:
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 100
          podAffinityTerm:
            labelSelector:
              matchExpressions:
                - key: app
                  operator: In
                  values:
                    - edams
            topologyKey: kubernetes.io/hostname
  tolerations:
    - key: "node-type"
      operator: "Equal"
      value: "application"
      effect: "NoSchedule"
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    fsGroup: 1000
EOF
```

### 3.3 执行部署

```bash
# 部署前验证配置
helm template edams edams/edams -f values-prod.yaml -n edams-prod

# 执行部署
helm upgrade --install edams edams/edams \
  -f values-prod.yaml \
  -n edams-prod \
  --create-namespace \
  --wait \
  --timeout 60m \
  --debug

# 查看部署状态
helm status edams -n edams-prod

# 查看Pod状态
kubectl get pods -n edams-prod -w
```

---

## 四、服务验证

### 4.1 健康检查

```bash
# 查看所有服务Pod状态
kubectl get pods -n edams-prod -l app.kubernetes.io/name=edams

# 查看服务日志
kubectl logs -n edams-prod -l app=edams-gateway --tail=100

# 查看服务资源使用
kubectl top pods -n edams-prod
```

### 4.2 API接口验证

```bash
# 获取Gateway地址
GATEWAY_URL=$(kubectl get ingress -n edams-prod -o jsonpath='{.items[0].spec.rules[0].host}')

# 测试健康检查接口
curl -k https://${GATEWAY_URL}/actuator/health

# 测试登录接口
curl -k -X POST https://${GATEWAY_URL}/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 获取访问令牌
TOKEN=$(curl -k -s -X POST https://${GATEWAY_URL}/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

# 测试资产管理接口
curl -k https://${GATEWAY_URL}/api/v1/assets \
  -H "Authorization: Bearer ${TOKEN}"
```

### 4.3 功能验证清单

| 序号 | 功能模块 | 测试场景 | 验证方法 | 预期结果 |
|------|---------|---------|---------|---------|
| 1 | 用户认证 | 登录 | POST /api/auth/login | 返回JWT Token |
| 2 | 用户认证 | Token验证 | GET /api/auth/validate | 返回用户信息 |
| 3 | 资产管理 | 创建资产 | POST /api/v1/assets | 返回资产ID |
| 4 | 资产管理 | 查询资产 | GET /api/v1/assets/{id} | 返回资产详情 |
| 5 | 资产管理 | 搜索资产 | GET /api/v1/assets/search?q=test | 返回搜索结果 |
| 6 | 血缘关系 | 创建血缘 | POST /api/v1/lineage | 返回血缘ID |
| 7 | 血缘关系 | 查询血缘图 | GET /api/v1/lineage/graph/{id} | 返回血缘图数据 |
| 8 | 数据质量 | 执行检查 | POST /api/v1/quality/check | 返回检查任务ID |
| 9 | 伦理评估 | 创建评估 | POST /api/v1/ethics/assessments | 返回评估ID |
| 10 | 分类分级 | 自动分类 | POST /api/v1/classification/classify | 返回分类结果 |
| 11 | 脱敏管理 | 数据脱敏 | POST /api/v1/masking/mask | 返回脱敏数据 |
| 12 | 边缘设备 | 注册设备 | POST /api/v1/iot/devices | 返回设备ID |
| 13 | LLM集成 | 调用GPT | POST /api/v1/llm/chat | 返回AI响应 |
| 14 | 通知服务 | 发送通知 | POST /api/v1/notification/send | 返回发送结果 |

---

## 五、监控和告警配置

### 5.1 Prometheus配置

```bash
# 部署Prometheus Operator
kubectl apply -f prometheus-operator.yaml

# 创建Prometheus实例
cat > prometheus-instance.yaml << 'EOF'
apiVersion: monitoring.coreos.com/v1
kind: Prometheus
metadata:
  name: edams-prometheus
  namespace: monitoring
spec:
  serviceAccountName: prometheus
  serviceMonitorSelector:
    matchLabels:
      team: edams
  ruleSelector:
    matchLabels:
      team: edams
  retention: 15d
  retentionSize: 50GB
EOF

kubectl apply -f prometheus-instance.yaml

# 应用EDAMS告警规则
kubectl apply -f /workspace/infrastructure/monitoring/prometheus/edams-alerts.yml
```

### 5.2 Grafana配置

```bash
# 导入Grafana大盘
kubectl create configmap edams-grafana-dashboard \
  --from-file=grafana-dashboard.json=/workspace/infrastructure/monitoring/grafana-dashboard.json \
  -n monitoring

# 配置数据源
cat > grafana-datasource.yaml << 'EOF'
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-datasources
  namespace: monitoring
data:
  prometheus.yaml: |
    apiVersion: 1
    datasources:
      - name: Prometheus
        type: prometheus
        access: proxy
        url: http://edams-prometheus.monitoring.svc:9090
        isDefault: true
EOF

kubectl apply -f grafana-datasource.yaml
```

### 5.3 告警通知配置

```bash
# 配置Alertmanager
cat > alertmanager-config.yaml << 'EOF'
global:
  smtp_smarthost: 'smtp.example.com:465'
  smtp_from: 'alertmanager@example.com'
  smtp_auth_username: 'alertmanager@example.com'
  smtp_auth_password: '${ALERT_PASSWORD}'

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 12h
  receiver: 'default-receiver'
  routes:
    - match:
        severity: critical
      receiver: 'critical-receiver'
    - match:
        severity: warning
      receiver: 'warning-receiver'

receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'ops@example.com'
  - name: 'critical-receiver'
    email_configs:
      - to: 'critical@example.com'
    webhook_configs:
      - url: '${CRITICAL_WEBHOOK}'
  - name: 'warning-receiver'
    email_configs:
      - to: 'warning@example.com'
EOF

kubectl apply -f alertmanager-config.yaml -n monitoring
```

---

## 六、日志管理

### 6.1 ELK Stack配置

```bash
# 部署Elasticsearch Curator（索引管理）
cat > elasticsearch-curator.yaml << 'EOF'
apiVersion: batch/v1
kind: CronJob
metadata:
  name: es-index-cleanup
  namespace: monitoring
spec:
  schedule: "0 2 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: curator
            image: bitnami/elasticsearch-curator:5.8.4
            args: ["--config", "/etc/curator/config.yml", "/etc/curator/action.yml"]
            volumeMounts:
            - name: config
              mountPath: /etc/curator
          volumes:
          - name: config
            configMap:
              name: es-curator-config
          restartPolicy: OnFailure
EOF

kubectl apply -f elasticsearch-curator.yaml
```

### 6.2 日志保留策略

```bash
# 配置索引生命周期管理
cat > ilm-policy.json << 'EOF'
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_age": "1d",
            "max_size": "50gb"
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          },
          "forcemerge": {
            "max_num_segments": 1
          }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
EOF

curl -X PUT "http://es-0.edams-prod.svc:9200/_ilm/policy/edams-policy" \
  -H "Content-Type: application/json" \
  -d @ilm-policy.json
```

---

## 七、备份和恢复

### 7.1 数据库备份

```bash
# 创建数据库备份脚本
cat > backup-mysql.sh << 'EOF'
#!/bin/bash
set -e

# 配置
BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_HOST="mysql-master.edams-prod.svc"
MYSQL_PORT="3306"
MYSQL_USER="edams_backup"
MYSQL_PASSWORD="${BACKUP_PASSWORD}"
RETENTION_DAYS=30

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 执行备份
echo "Starting MySQL backup at $(date)"
mysqldump -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD} \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --all-databases | gzip > ${BACKUP_DIR}/edams_full_${DATE}.sql.gz

# 计算校验和
sha256sum ${BACKUP_DIR}/edams_full_${DATE}.sql.gz > ${BACKUP_DIR}/edams_full_${DATE}.sql.gz.sha256

# 上传到对象存储
aws s3 cp ${BACKUP_DIR}/edams_full_${DATE}.sql.gz s3://edams-backup/mysql/

# 清理过期备份
find ${BACKUP_DIR} -name "edams_full_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
find ${BACKUP_DIR} -name "edams_full_*.sha256" -mtime +${RETENTION_DAYS} -delete

echo "Backup completed at $(date)"
EOF

chmod +x backup-mysql.sh

# 配置定时任务
(crontab -l 2>/dev/null; echo "0 3 * * * /opt/scripts/backup-mysql.sh >> /var/log/backup.log 2>&1") | crontab -
```

### 7.2 配置备份

```bash
# 创建配置备份脚本
cat > backup-config.sh << 'EOF'
#!/bin/bash
set -e

BACKUP_DIR="/backup/config"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份Kubernetes配置
kubectl get all,configmap,secret -n edams-prod -o yaml > ${BACKUP_DIR}/edams-k8s-config_${DATE}.yaml

# 备份Helm配置
helm get values edams -n edams-prod > ${BACKUP_DIR}/edams-helm-values_${DATE}.yaml

# 备份Nacos配置
curl -s http://nacos:nacos@${NACOS_URL}:8848/nacos/v1/cs/configs?search=accurate&dataId=&group=DEFAULT_GROUP&pageNo=1&pageSize=100 > ${BACKUP_DIR}/nacos-config_${DATE}.json

# 备份到对象存储
aws s3 sync ${BACKUP_DIR} s3://edams-backup/config/
EOF

chmod +x backup-config.sh
```

### 7.3 恢复流程

```bash
# 数据库恢复
cat > restore-mysql.sh << 'EOF'
#!/bin/bash
set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <backup_file>"
    exit 1
fi

BACKUP_FILE=$1
MYSQL_HOST="mysql-master.edams-prod.svc"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASSWORD="${MYSQL_ROOT_PASSWORD}"

echo "Starting database restore from ${BACKUP_FILE}"
gunzip -c ${BACKUP_FILE} | mysql -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD}
echo "Database restore completed"
EOF

chmod +x restore-mysql.sh
```

---

## 八、灾难恢复

### 8.1 灾难恢复计划

```bash
# 创建灾难恢复脚本
cat > disaster-recovery.sh << 'EOF'
#!/bin/bash
set -e

DR_SCRIPT_VERSION="1.0"
NAMESPACE="edams-prod"
BACKUP_BUCKET="s3://edams-backup"

echo "=== EDAMS灾难恢复脚本 v${DR_SCRIPT_VERSION} ==="
echo "开始时间: $(date)"

# 1. 检查备份
echo "[1/6] 检查最新备份..."
LATEST_DB_BACKUP=$(aws s3 ls ${BACKUP_BUCKET}/mysql/ | sort | tail -n 1 | awk '{print $4}')
LATEST_CONFIG_BACKUP=$(aws s3 ls ${BACKUP_BUCKET}/config/ | sort | tail -n 1 | awk '{print $4}')

if [ -z "$LATEST_DB_BACKUP" ]; then
    echo "错误: 未找到数据库备份"
    exit 1
fi

echo "最新数据库备份: ${LATEST_DB_BACKUP}"
echo "最新配置备份: ${LATEST_CONFIG_BACKUP}"

# 2. 停止所有服务
echo "[2/6] 停止所有服务..."
kubectl scale deployment --all -n ${NAMESPACE} --replicas=0

# 3. 恢复数据库
echo "[3/6] 恢复数据库..."
aws s3 cp ${BACKUP_BUCKET}/mysql/${LATEST_DB_BACKUP} /tmp/
./restore-mysql.sh /tmp/${LATEST_DB_BACKUP}

# 4. 恢复配置
echo "[4/6] 恢复Kubernetes配置..."
aws s3 cp ${BACKUP_BUCKET}/config/ /tmp/config-backup/ --recursive
kubectl apply -f /tmp/config-backup/edams-k8s-config_*.yaml

# 5. 重启服务
echo "[5/6] 重启所有服务..."
kubectl scale deployment --all -n ${NAMESPACE} --replicas=1

# 6. 验证
echo "[6/6] 验证服务状态..."
sleep 60
kubectl get pods -n ${NAMESPACE}
curl -k https://edams.example.com/actuator/health

echo "=== 灾难恢复完成 ==="
echo "结束时间: $(date)"
EOF

chmod +x disaster-recovery.sh
```

---

## 九、运维手册

### 9.1 日常运维命令

```bash
# 查看所有EDAMS服务状态
kubectl get pods -n edams-prod -o wide

# 查看服务日志
kubectl logs -n edams-prod -l app=edams-gateway --tail=100 -f

# 进入Pod调试
kubectl exec -it -n edams-prod <pod-name> -- /bin/bash

# 重启服务
kubectl rollout restart deployment/edams-gateway -n edams-prod

# 查看资源使用
kubectl top pods -n edams-prod

# 查看事件
kubectl get events -n edams-prod --sort-by='.lastTimestamp'
```

### 9.2 扩缩容操作

```bash
# 扩容Gateway服务
kubectl scale deployment edams-gateway -n edams-prod --replicas=5

# 缩容Gateway服务
kubectl scale deployment edams-gateway -n edams-prod --replicas=2

# 修改HPA配置
kubectl patch hpa edams-gateway -n edams-prod -p '{"spec":{"maxReplicas":20}}'
```

### 9.3 更新升级

```bash
# 更新Helm配置
helm upgrade edams edams/edams \
  -f values-prod-new.yaml \
  -n edams-prod \
  --wait

# 回滚到上一个版本
helm rollback edams -n edams-prod

# 回滚到指定版本
helm rollback edams 3 -n edams-prod
```

---

## 十、故障处理

### 10.1 常见问题处理

| 问题现象 | 可能原因 | 排查命令 | 解决方案 |
|---------|---------|---------|---------|
| Pod启动失败 | 镜像拉取失败 | kubectl describe pod | 检查镜像仓库配置 |
| Pod启动失败 | 配置错误 | kubectl logs pod | 检查配置ConfigMap |
| Pod启动失败 | 资源不足 | kubectl describe node | 增加资源配额 |
| 服务响应慢 | 内存不足 | kubectl top pod | 增加内存限制 |
| 服务响应慢 | 数据库连接池满 | 查看监控 | 调整连接池大小 |
| 服务无响应 | 网络问题 | 检查网络策略 | 检查安全组规则 |
| 服务无响应 | Pod被驱逐 | kubectl describe pod | 调整资源请求值 |

### 10.2 紧急联系清单

| 角色 | 姓名 | 电话 | 邮箱 | 职责 |
|------|------|------|------|------|
| 运维负责人 | | | | 基础设施问题 |
| DBA负责人 | | | | 数据库问题 |
| 安全负责人 | | | | 安全事件 |
| 开发负责人 | | | | 应用问题 |
| 项目经理 | | | | 整体协调 |

---

## 十一、验收标准

### 11.1 功能验收

- [ ] 所有20个核心服务正常运行
- [ ] 健康检查接口全部返回UP
- [ ] 用户认证流程正常
- [ ] 资产管理CRUD功能正常
- [ ] 血缘关系查询正常
- [ ] 数据质量检查正常
- [ ] 伦理评估功能正常
- [ ] 分类分级功能正常
- [ ] 脱敏管理功能正常
- [ ] 边缘设备接入正常
- [ ] LLM集成调用正常

### 11.2 性能验收

- [ ] API响应时间P95 ≤ 500ms
- [ ] 并发用户数 ≥ 1000
- [ ] QPS ≥ 5000
- [ ] 系统可用性 ≥ 99.9%

### 11.3 安全验收

- [ ] HTTPS访问正常
- [ ] JWT认证正常
- [ ] 权限控制正常
- [ ] 敏感数据脱敏正常
- [ ] 审计日志正常

---

**文档版本**：v1.0  
**创建日期**：2026-05-11  
**最后更新**：2026-05-11  
**维护周期**：每季度审查一次  
**审批人**：________________  
**审批日期**：________________  
