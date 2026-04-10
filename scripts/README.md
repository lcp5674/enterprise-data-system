# EDAMS运维脚本

企业数据资产管理系统的运维脚本工具集，用于系统管理、监控、备份和故障排查。

## 目录结构

```
scripts/
├── deploy/                     # 部署脚本
│   ├── setup-environment.sh   # 环境初始化脚本
│   ├── deploy-application.sh  # 应用部署脚本
│   └── upgrade-cluster.sh     # 集群升级脚本
├── monitoring/                # 监控脚本
│   ├── check-system-health.sh # 系统健康检查
│   ├── check-metrics.sh      # 指标检查
│   └── alert-test.sh         # 告警测试
├── backup/                   # 备份恢复脚本
│   ├── backup-and-restore.sh # 主备份恢复脚本
│   ├── backup-database.sh    # 数据库备份
│   └── test-restore.sh       # 恢复测试
├── database/                 # 数据库管理
│   ├── migrate-database.sh   # 数据库迁移
│   ├── query-performance.sh  # 性能查询
│   └── cleanup-data.sh       # 数据清理
├── security/                 # 安全脚本
│   ├── audit-log.sh          # 审计日志
│   ├── security-scan.sh      # 安全扫描
│   └── compliance-check.sh   # 合规检查
└── utils/                    # 工具脚本
    ├── k8s-operations.sh     # K8S操作帮助
    ├── network-troubleshoot.sh # 网络故障排查
    └── log-analyzer.sh       # 日志分析器
```

## 快速开始

### 1. 环境准备

```bash
# 安装依赖
./scripts/utils/install-dependencies.sh

# 配置环境变量
cp .env.example .env
vi .env
```

### 2. 系统健康检查

```bash
# 运行系统健康检查
./scripts/monitoring/check-system-health.sh

# 指定命名空间检查
NAMESPACE=edams-prod ./scripts/monitoring/check-system-health.sh
```

### 3. 执行系统备份

```bash
# 完整系统备份
./scripts/backup/backup-and-restore.sh backup full

# 增量备份
./scripts/backup/backup-and-restore.sh backup incremental

# 列出可用备份
./scripts/backup/backup-and-restore.sh list
```

## 脚本详解

### 1. 环境初始化脚本 (`setup-environment.sh`)

**功能**: 初始化生产环境的所有基础设施组件。

```bash
# 创建生产环境
./scripts/deploy/setup-environment.sh

# 自定义环境
ENVIRONMENT=prod \
DOMAIN=edams.company.com \
NAMESPACE=edams-prod \
./scripts/deploy/setup-environment.sh

# 输出示例
[INFO] 2024-12-01 12:00:00 开始设置环境: prod
[INFO] 2024-12-01 12:00:00 域名: edams.company.com
[INFO] 2024-12-01 12:00:00 命名空间: edams-prod
[SUCCESS] 2024-12-01 12:05:00 环境设置完成！
```

### 2. 系统健康检查脚本 (`check-system-health.sh`)

**功能**: 检查所有组件状态，生成健康报告。

```bash
# 基本检查
./scripts/monitoring/check-system-health.sh

# 自定义命名空间和接收人
NAMESPACE=edams-staging \
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/XXX \
EMAIL_RECIPIENTS="team@company.com" \
./scripts/monitoring/check-system-health.sh

# 输出示例
[INFO] 开始EDAMS系统健康检查...
[INFO] 命名空间: edams-prod
✅ gateway-7f8b6c5d9-abc12: Running
✅ auth-5d8c4b3a2-def45: Running
❌ redis-master-0: CrashLoopBackOff
[ERROR] 系统检查发现错误！
```

### 3. 备份恢复脚本 (`backup-and-restore.sh`)

**功能**: 支持完整系统备份、增量备份和灾难恢复。

```bash
# 执行完整备份
RETENTION_DAYS=7 \
S3_BUCKET=edams-backup-prod \
./scripts/backup/backup-and-restore.sh backup full

# 列出备份
./scripts/backup/backup-and-restore.sh list

# 恢复系统（谨慎使用）
./scripts/backup/backup-and-restore.sh restore /backups/edams_20241201_120000
```

## 自动化运维

### 使用Cron定时任务

```bash
# 设置每日健康检查
0 8 * * * cd /opt/edams && ./scripts/monitoring/check-system-health.sh >> /var/log/edams/health-check.log 2>&1

# 设置每日备份
0 2 * * * cd /opt/edams && ./scripts/backup/backup-and-restore.sh backup full >> /var/log/edams/backup.log 2>&1

# 清理旧备份
0 3 * * 0 cd /opt/edams && ./scripts/backup/backup-and-restore.sh cleanup >> /var/log/edams/cleanup.log 2>&1
```

### GitLab CI/CD集成

```yaml
# .gitlab-ci.yml
health_check:
  stage: deploy
  script:
    - ./scripts/monitoring/check-system-health.sh
  only:
    - schedules

backup_production:
  stage: deploy
  script:
    - ./scripts/backup/backup-and-restore.sh backup full
  variables:
    KUBECONFIG: /kubeconfig.yaml
  only:
    refs:
      - master
    schedule:
      - "0 2 * * *"
```

## 故障排查

### 1. 系统无法启动

```bash
# 检查所有Pod状态
kubectl get pods -n edams-prod --sort-by={.status.phase}

# 查看详细事件
kubectl get events -n edams-prod --sort-by=.lastTimestamp

# 检查Ingress配置
kubectl get ingress -n edams-prod -o yaml | yq '.spec.rules[].host'

# 使用网络故障排查脚本
./scripts/utils/network-troubleshoot.sh
```

### 2. 数据库问题

```bash
# 检查数据库连接
kubectl exec -n edams-prod deployment/postgresql -- pg_isready -U postgres

# 查看数据库日志
kubectl logs -n edams-prod statefulset/postgresql --tail=100

# 检查磁盘空间
kubectl exec -n edams-prod postgresql-0 -- df -h

# 使用性能查询脚本
./scripts/database/query-performance.sh
```

### 3. 网络问题

```bash
# 测试服务连通性
./scripts/utils/network-troubleshoot.sh --service=gateway --port=8080

# 检查DNS解析
kubectl run test-dns --image=busybox --restart=Never --rm -it -- nslookup gateway.edams-prod.svc.cluster.local

# 检查网络策略
kubectl get networkpolicies -n edams-prod -o yaml
```

## 安全最佳实践

### 1. 权限管理

```bash
# 最小权限原则
# 每个脚本只授予必要的权限
./scripts/utils/k8s-operations.sh --action=read-only

# 定期审计
./scripts/security/audit-log.sh --namespace=edams-prod --days=7
```

### 2. 数据加密

```bash
# 加密敏感数据
export DB_PASSWORD=$(openssl rand -base64 32)
export JWT_SECRET=$(openssl rand -base64 64)

# 使用Kubernetes Secrets
kubectl create secret generic edams-secrets \
  --from-literal=db-password=$DB_PASSWORD \
  --from-literal=jwt-secret=$JWT_SECRET \
  --namespace=edams-prod
```

### 3. 合规检查

```bash
# 运行安全扫描
./scripts/security/security-scan.sh --namespace=edams-prod

# 检查合规性
./scripts/security/compliance-check.sh --standard=gdpr
```

## 监控告警集成

### 1. 告警规则

```yaml
# prometheus-alerts.yaml
groups:
- name: edams.alerts
  rules:
  - alert: EDAMSComponentDown
    expr: kube_deployment_status_replicas_unavailable{deployment=~"edams-.*"} > 0
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "EDAMS组件 {{ $labels.deployment }} 不可用"
      description: "{{ $labels.deployment }} 已经持续不可用超过5分钟"
```

### 2. 告警测试

```bash
# 测试告警系统
./scripts/monitoring/alert-test.sh --alert=EDAMSComponentDown --severity=critical

# 验证告警通知
./scripts/monitoring/alert-test.sh --channel=slack --recipient="@devops-team"
```

## 性能优化

### 1. 脚本性能

```bash
# 使用并行处理
./scripts/monitoring/check-system-health.sh --parallel

# 缓存结果
./scripts/monitoring/check-metrics.sh --cache-ttl=300
```

### 2. 资源优化

```bash
# 调整资源限制
kubectl get pods -n edams-prod -o json | jq '.items[].spec.containers[].resources'

# 优化Pod亲和性
kubectl get pod -n edams-prod -o wide
```

## 文档更新与维护

### 1. 添加新脚本

```bash
# 创建新脚本模板
cp scripts/templates/new-script-template.sh scripts/utils/your-new-script.sh

# 更新文档
./scripts/utils/update-docs.sh --script=your-new-script.sh
```

### 2. 测试脚本

```bash
# 运行单元测试
./scripts/utils/test-scripts.sh --script=check-system-health

# 集成测试
./scripts/utils/test-scripts.sh --environment=staging
```

## 故障恢复演练

### 1. 定期演练

```bash
# 每月执行灾难恢复演练
./scripts/backup/test-restore.sh --environment=dr-test

# 记录演练结果
./scripts/utils/dr-report.sh --event=monthly-drill
```

### 2. 应急预案

```bash
# 执行应急预案
./scripts/emergency/response.sh --incident=database-outage

# 通知相关人员
./scripts/emergency/notify.sh --team=devops --priority=critical
```

## 贡献指南

### 1. 开发新功能

```bash
# Fork项目并创建分支
git checkout -b feature/new-script

# 开发脚本
vi scripts/feature/new-feature.sh

# 添加测试
vi tests/feature/new-feature-test.sh

# 提交PR
git push origin feature/new-script
```

### 2. 代码质量

- shellcheck 所有脚本
- 使用set -euo pipefail
- 添加详细注释
- 包含参数验证
- 提供使用示例

## 支持与联系方式

| 问题类型 | 联系方式 | 响应时间 |
|----------|----------|----------|
| 紧急故障 | Slack: #edams-devops | 24/7 |
| 技术支持 | Email: devops@company.com | 工作日9-18点 |
| 功能请求 | Jira: EDAMS Project | 1-2工作日 |

---

> **重要**：生产环境使用前请充分测试，并确保有回滚计划。