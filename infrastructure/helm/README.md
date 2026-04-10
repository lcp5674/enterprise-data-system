# EDAMS Helm Charts

企业数据资产管理系统的完整Helm Charts配置，支持生产环境部署。

## 目录结构

```
infrastructure/helm/
├── edams/                    # 主应用Chart
│   ├── Chart.yaml           # Chart定义
│   ├── values.yaml          # 默认配置值
│   ├── values-dev.yaml      # 开发环境配置
│   ├── values-staging.yaml  # 预生产环境配置
│   ├── values-prod.yaml     # 生产环境配置
│   ├── README.md            # Chart说明文档
│   └── templates/           # Kubernetes模板
│       ├── _helpers.tpl     # 模板助手函数
│       ├── gateway/         # 网关服务模板
│       ├── auth/            # 认证服务模板
│       ├── asset/           # 资产管理服务模板
│       ├── discovery/       # 资产发现服务模板
│       ├── governance/      # 资产治理服务模板
│       ├── knowledge/       # 知识智能服务模板
│       ├── operation/       # 运营支撑服务模板
│       ├── infrastructure/  # 基础设施模板
│       └── monitoring/      # 监控模板
└── scripts/                 # 部署脚本和工具
```

## 部署说明

### 1. 安装依赖

```bash
# 添加Bitnami仓库
helm repo add bitnami https://charts.bitnami.com/bitnami

# 更新仓库
helm repo update

# 安装Chart依赖
cd infrastructure/helm/edams
helm dependency update
```

### 2. 配置环境变量

```bash
# 设置环境变量
export NAMESPACE=edams-prod
export ENVIRONMENT=prod
export DOMAIN=edams.enterprise.com
export REGISTRY=registry.enterprise.com

# 创建命名空间
kubectl create namespace $NAMESPACE
```

### 3. 部署系统

#### 使用values文件部署
```bash
# 开发环境部署
helm upgrade --install edams-dev ./edams \
  --namespace edams-dev \
  --values values-dev.yaml \
  --set global.environment=dev

# 预生产环境部署
helm upgrade --install edams-staging ./edams \
  --namespace edams-staging \
  --values values-staging.yaml \
  --set global.environment=staging

# 生产环境部署（需确认）
helm upgrade --install edams-prod ./edams \
  --namespace edams-prod \
  --values values-prod.yaml \
  --set global.environment=prod \
  --dry-run   # 先测试配置
```

#### 使用secrets文件部署
```bash
# 创建secrets文件
cat > secrets.yaml <<EOF
postgresql:
  auth:
    password: $(openssl rand -hex 16)
    postgresPassword: $(openssl rand -hex 16)
redis:
  auth:
    password: $(openssl rand -hex 16)
rabbitmq:
  auth:
    password: $(openssl rand -hex 16)
EOF

# 部署带secrets的Chart
helm upgrade --install edams ./edams \
  --namespace $NAMESPACE \
  --values values-prod.yaml \
  --values secrets.yaml
```

### 4. 卸载系统

```bash
# 删除Helm release
helm uninstall edams --namespace $NAMESPACE

# 清理PVC（可选）
kubectl delete pvc --all --namespace $NAMESPACE --now
```

## 生产环境部署最佳实践

### 1. 安全配置
- 启用TLS并配置证书
- 使用非root用户运行容器
- 限制容器权限
- 配置网络策略

### 2. 高可用配置
- 设置多副本部署
- 配置Pod反亲和性
- 启用HPA自动扩缩容
- 配置持久化存储

### 3. 监控配置
- 启用Prometheus监控
- 配置Grafana仪表盘
- 设置告警规则
- 配置日志收集

### 4. 备份配置
- 配置数据库备份
- 使用Velero进行集群备份
- 定期测试恢复流程

## 环境配置示例

### values-dev.yaml（开发环境）
```yaml
global:
  environment: dev
  domain: dev.edams.enterprise.com

# 减少资源分配
gateway:
  replicaCount: 1
  resources:
    requests:
      cpu: 100m
      memory: 256Mi
    limits:
      cpu: 200m
      memory: 512Mi

# 使用本地数据库
postgresql:
  enabled: true
  architecture: standalone
```

### values-prod.yaml（生产环境）
```yaml
global:
  environment: prod
  domain: edams.enterprise.com
  tls:
    enabled: true
    issuer: letsencrypt-prod

# 高可用配置
gateway:
  replicaCount: 3
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10

# 生产级数据库
postgresql:
  enabled: true
  architecture: replication
  primary:
    persistence:
      size: 100Gi
    resources:
      requests:
        cpu: 2
        memory: 4Gi
```

## 故障排除

### 1. 部署失败
```bash
# 查看部署状态
helm status edams --namespace $NAMESPACE

# 查看Pod状态
kubectl get pods --namespace $NAMESPACE

# 查看事件
kubectl get events --namespace $NAMESPACE --sort-by=.lastTimestamp

# 查看日志
kubectl logs deployment/edams-gateway --namespace $NAMESPACE
```

### 2. 数据库连接问题
```bash
# 测试数据库连接
kubectl exec -it deployment/edams-postgresql -- psql -U edams_admin -d edams_prod

# 查看数据库日志
kubectl logs statefulset/edams-postgresql --namespace $NAMESPACE
```

### 3. 网络问题
```bash
# 测试服务连接
kubectl run curl-test --image=curlimages/curl -it --rm --restart=Never -- \
  curl http://edams-gateway.edams-prod.svc.cluster.local

# 检查网络策略
kubectl get networkpolicies --namespace $NAMESPACE
```

## 更新策略

### 滚动更新
```bash
# 更新镜像版本
helm upgrade edams ./edams \
  --namespace $NAMESPACE \
  --set gateway.image.tag=v1.2.0 \
  --set auth.image.tag=v1.2.0
```

### 金丝雀发布
```bash
# 创建一个canary release
helm upgrade --install edams-canary ./edams \
  --namespace edams-prod \
  --values values-prod.yaml \
  --set gateway.replicaCount=1 \
  --set gateway.image.tag=v1.2.0-canary \
  --set global.environment=prod
```

### 蓝绿部署
```bash
# 部署新版本
helm upgrade --install edams-v2 ./edams \
  --namespace edams-prod-v2 \
  --values values-prod.yaml \
  --set global.domain=edams-v2.enterprise.com

# 切换流量
kubectl patch service edams-gateway \
  --namespace edams-prod \
  --patch '{"spec":{"selector":{"version":"v2"}}}'
```

## 文档资源

- [Helm 官方文档](https://helm.sh/docs/)
- [Bitnami Charts](https://github.com/bitnami/charts)
- [Kubernetes 最佳实践](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Prometheus Operator](https://prometheus-operator.dev/)