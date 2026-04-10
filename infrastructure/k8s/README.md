# 企业数据资产管理系统 - Kubernetes生产部署配置

## 概述

本目录包含企业数据资产管理系统的Kubernetes生产部署配置，遵循云原生最佳实践。

## 部署架构

### 命名空间设计
- `edams-prod`: 生产环境主命名空间
- `edams-monitoring`: 监控组件命名空间
- `edams-logging`: 日志组件命名空间
- `edams-cert-manager`: 证书管理命名空间

### 配置结构
```
k8s/
├── namespaces/           # 命名空间配置
├── configs/             # ConfigMap和Secret配置
├── deployments/         # Deployment配置
├── services/           # Service配置
├── storage/           # 存储配置（PV/PVC/StorageClass）
├── ingress/           # Ingress配置
├── hpa/              # 水平自动扩缩容配置
├── pdb/              # PodDisruptionBudget配置
└── README.md
```

## 部署流程

### 1. 初始化命名空间
```bash
kubectl apply -f namespaces/
```

### 2. 部署存储配置
```bash
kubectl apply -f storage/
```

### 3. 部署配置文件和密钥
```bash
kubectl apply -f configs/
```

### 4. 部署数据库和中间件
```bash
# 部署数据库（选择一种方案）
kubectl apply -f database/statefulsets/
# 或使用云服务（不部署StatefulSet）
```

### 5. 部署微服务
```bash
kubectl apply -f deployments/
```

### 6. 部署服务发现和网络
```bash
kubectl apply -f services/
kubectl apply -f ingress/
```

### 7. 部署监控和告警
```bash
kubectl apply -f monitoring/
```

## 资源需求

### 最小生产配置
- 节点数: 6个（3个Master，3个Worker）
- Worker节点配置: 16核32GB内存 200GB存储
- 命名空间: `edams-prod`

### 推荐生产配置
- 节点数: 10个（3个Master，7个Worker）
- Worker节点配置: 32核64GB内存 500GB存储
- 可用区: 跨3个可用区部署

## 安全配置

### 网络策略
- 默认拒绝所有入站流量
- 按服务开放所需端口
- 使用NetworkPolicy限制Pod间通信

### RBAC配置
- 最小权限原则
- 服务账号绑定特定权限
- 审计日志记录所有操作

### 密钥管理
- 敏感配置存储在Secrets中
- 使用KMS加密Secrets
- 定期轮换密钥

## 监控指标

### 需要监控的关键指标
- 应用层: QPS、响应时间、错误率、JVM指标
- 中间件层: 连接数、队列深度、延迟
- 基础设施: CPU、内存、磁盘、网络
- 业务指标: 资产数量、血缘覆盖率、质量评分

## 更新策略

### 滚动更新
```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
```

### 蓝绿部署
- 使用标签选择器实现流量切换
- 部署验证后切换流量
- 旧版本保留一段时间用于回滚

### 金丝雀发布
- 使用Istio实现流量百分比控制
- 逐步增加新版本流量
- 监控指标确保稳定性

## 故障恢复

### Pod故障
- 使用Readiness/Liveness探针
- 自动重启失败Pod
- 节点故障时自动迁移

### 服务故障
- 多个实例保证高可用
- 负载均衡自动规避故障实例
- 断路器模式防止级联故障

### 数据故障
- 数据库集群高可用
- 定期备份和恢复测试
- 跨可用区数据复制

## 性能优化

### 资源限制
```yaml
resources:
  requests:
    cpu: "500m"
    memory: "1Gi"
  limits:
    cpu: "1000m"
    memory: "2Gi"
```

### 调度优化
- 使用节点亲和性
- 应用反亲和性保证高可用
- 针对工作负载选择合适的节点类型

### 存储优化
- 使用本地SSD提高IO性能
- 数据分级存储（热数据/冷数据）
- 自动扩容存储容量

## 运维脚本

### 常用命令
```bash
# 查看命名空间状态
./scripts/k8s-status.sh

# 部署单个服务
./scripts/deploy-service.sh edams-asset v1.2.3

# 回滚到上一个版本
./scripts/rollback-service.sh edams-asset

# 清理旧镜像
./scripts/cleanup-images.sh

# 健康检查
./scripts/health-check.sh
```

## 升级策略

### 版本控制
- 使用Git管理所有配置
- 配置变更通过PR审查
- 记录所有变更历史

### 测试流程
1. 开发环境测试
2. 测试环境验证
3. 预发布环境灰度
4. 生产环境发布

### 回滚预案
- 保留最近3个版本
- 快速回滚脚本
- 回滚验证流程

## 联系方式

- 架构师: architecture-team@enterprise.com
- DevOps: devops-team@enterprise.com
- 运维: ops-team@enterprise.com