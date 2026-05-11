# EDAMS 生产部署前检查清单

**项目名称**：企业数据资产管理系统（EDAMS）  
**检查清单版本**：v1.0  
**检查日期**：2026-05-11  
**检查人**：资深软件工程团队  

---

## 一、环境准备检查

### 1.1 基础设施检查

| 序号 | 检查项 | 检查标准 | 检查方法 | 状态 | 检查人 | 检查时间 |
|------|--------|---------|---------|------|--------|---------|
| 1 | Kubernetes集群 | 版本≥1.27 | `kubectl version` | ☐ | | |
| 2 | Docker Registry | 已部署并可访问 | 访问控制台 | ☐ | | |
| 3 | NFS/对象存储 | 容量≥500GB | 查看存储配置 | ☐ | | |
| 4 | 负载均衡器 | 已配置ELB/CLB | 检查配置 | ☐ | | |
| 5 | 域名解析 | DNS已配置 | `nslookup`验证 | ☐ | | |
| 6 | SSL证书 | 证书有效且已配置 | 检查证书有效期 | ☐ | | |

### 1.2 中间件检查

| 序号 | 中间件 | 版本要求 | 检查方法 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | MySQL | ≥8.0.33 | `mysql --version` | ☐ | 主从或集群部署 |
| 2 | Redis | ≥6.0 | `redis-cli ping` | ☐ | Cluster模式 |
| 3 | Kafka | ≥3.6 | `kafka-topics.sh` | ☐ | 3节点集群 |
| 4 | Elasticsearch | ≥8.11 | `curl localhost:9200` | ☐ | 3节点集群 |
| 5 | Neo4j | ≥5.14 | `neo4j status` | ☐ | Cluster模式 |
| 6 | MongoDB | ≥6.0 | `mongosh --eval` | ☐ | Replica Set |
| 7 | Nacos | ≥2.2 | 访问控制台 | ☐ | Cluster模式 |
| 8 | RabbitMQ | ≥3.12 | 访问管理界面 | ☐ | (可选) |

### 1.3 网络和安全检查

| 序号 | 检查项 | 检查标准 | 检查方法 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | VPC/网络隔离 | 网络划分完成 | 检查网络拓扑 | ☐ | 生产/测试网络隔离 |
| 2 | 安全组规则 | 端口开放正确 | 检查入站/出站规则 | ☐ | 最小权限原则 |
| 3 | 防火墙规则 | 规则已配置 | `iptables -L` | ☐ | |
| 4 | VPN/跳板机 | 运维通道可用 | 测试连接 | ☐ | |
| 5 | 密钥管理 | Vault/KMS已配置 | 验证密钥访问 | ☐ | |

---

## 二、应用程序检查

### 2.1 镜像构建检查

| 序号 | 检查项 | 检查标准 | 检查方法 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | 基础镜像 | 使用官方认证镜像 | 检查Dockerfile | ☐ | |
| 2 | 镜像安全扫描 | 无高危漏洞 | Trivy/Clair扫描 | ☐ | |
| 3 | 镜像标签 | 正确版本标签 | 检查镜像列表 | ☐ | |
| 4 | 镜像签名 | 已签名验证 | 检查签名 | ☐ | (可选) |

### 2.2 配置文件检查

| 序号 | 配置文件 | 检查项 | 检查方法 | 状态 | 说明 |
|------|---------|--------|---------|------|------|
| 1 | application-prod.yml | 生产环境配置 | 审查配置内容 | ☐ | |
| 2 | application.yml | 通用配置 | 审查配置内容 | ☐ | |
| 3 | bootstrap.yml | 服务注册配置 | 检查Nacos地址 | ☐ | |
| 4 | config.yaml | 运维配置 | 检查配置项 | ☐ | |
| 5 | 加密配置 | 敏感信息加密 | 验证加密方式 | ☐ | |

### 2.3 数据库检查

| 序号 | 检查项 | 检查标准 | 检查方法 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | 数据库连接 | 连接池配置正确 | 测试连接 | ☐ | |
| 2 | 字符集 | UTF8MB4 | 检查表字符集 | ☐ | |
| 3 | 索引检查 | 索引已创建 | 检查执行计划 | ☐ | |
| 4 | 初始化脚本 | schema.sql已执行 | 检查表结构 | ☐ | |
| 5 | 数据迁移 | Flyway/Liquibase | 检查版本记录 | ☐ | |
| 6 | 备份策略 | 备份已配置 | 检查备份记录 | ☐ | |

---

## 三、部署清单

### 3.1 核心服务部署顺序

| 序号 | 服务名称 | 模块路径 | 依赖服务 | 启动命令 | 状态 | 启动时间 | 检查人 |
|------|---------|---------|---------|---------|------|---------|-------|
| 1 | Nacos | discovery/nacos | - | helm install | ☐ | | |
| 2 | edams-common | core/edams-common | - | 编译依赖 | ☐ | | |
| 3 | edams-gateway | core/edams-gateway | Nacos | kubectl apply | ☐ | | |
| 4 | edams-auth | core/edams-auth | Nacos, MySQL | kubectl apply | ☐ | | |
| 5 | edams-user | core/edams-user | Nacos, MySQL | kubectl apply | ☐ | | |
| 6 | edams-permission | core/edams-permission | Nacos, MySQL | kubectl apply | ☐ | | |
| 7 | edams-asset | core/edams-asset | Nacos, MySQL, ES | kubectl apply | ☐ | | |
| 8 | edams-lifecycle | core/edams-lifecycle | Nacos, MySQL | kubectl apply | ☐ | | |
| 9 | lineage-service | domain/services/lineage | Nacos, Neo4j | kubectl apply | ☐ | | |
| 10 | governance-engine | domain/services/governance | Nacos, MySQL, Kafka | kubectl apply | ☐ | | |
| 11 | quality-service | domain/services/quality | Nacos, MySQL, Kafka | kubectl apply | ☐ | | |
| 12 | ethics-service | domain/services/ethics | Nacos, MySQL | kubectl apply | ☐ | | |
| 13 | classification-service | domain/services/classification | Nacos, MySQL | kubectl apply | ☐ | | |
| 14 | masking-service | domain/services/masking | Nacos, MySQL | kubectl apply | ☐ | | |
| 15 | iot-edge-service | domain/services/iot-edge | Nacos, MQTT, MongoDB | kubectl apply | ☐ | | |
| 16 | edams-llm-client | core/edams-llm-client | - | 编译依赖 | ☐ | | |
| 17 | edams-llm | core/edams-llm | Nacos, LLM-Client | kubectl apply | ☐ | | |
| 18 | edams-notification | core/edams-notification | Nacos, MySQL | kubectl apply | ☐ | | |
| 19 | edams-workflow | core/edams-workflow | Nacos, MySQL | kubectl apply | ☐ | | |
| 20 | edams-report | core/edams-report | Nacos, MySQL | kubectl apply | ☐ | | |

### 3.2 Helm Chart部署命令

```bash
# 1. 添加Helm仓库
helm repo add edams https://charts.edams.example.com
helm repo update

# 2. 创建命名空间
kubectl create namespace edams-prod

# 3. 配置values文件
cat > values-prod.yaml << EOF
global:
  imageRegistry: registry.example.com
  imagePullSecrets: edams-registry-secret
  storageClass: nfs-storage

mysql:
  host: mysql-master.edams-prod.svc
  port: 3306
  database: edams
  username: edams_user
  existingSecret: edams-mysql-secret

redis:
  host: redis-cluster.edams-prod.svc
  port: 6379
  password: ""
  existingSecret: edams-redis-secret

kafka:
  brokers: 
    - kafka-0.edams-prod.svc:9092
    - kafka-1.edams-prod.svc:9092
    - kafka-2.edams-prod.svc:9092

nacos:
  serverAddress: nacos.edams-prod.svc
  port: 8848
  namespace: prod

ingress:
  enabled: true
  className: nginx
  host: edams.example.com
  tls:
    - secretName: edams-tls-secret
      hosts:
        - edams.example.com
EOF

# 4. 安装EDAMS
helm upgrade --install edams edams/edams \
  -n edams-prod \
  -f values-prod.yaml \
  --wait --timeout 30m
```

---

## 四、健康检查

### 4.1 服务健康检查

| 序号 | 服务名称 | 健康检查端点 | 预期响应 | 状态 | 检查时间 | 检查人 |
|------|---------|------------|---------|------|---------|-------|
| 1 | edams-gateway | /actuator/health | UP | ☐ | | |
| 2 | edams-auth | /api/auth/health | UP | ☐ | | |
| 3 | edams-user | /api/users/health | UP | ☐ | | |
| 4 | edams-asset | /api/assets/health | UP | ☐ | | |
| 5 | lineage-service | /api/lineage/health | UP | ☐ | | |
| 6 | governance-engine | /api/governance/health | UP | ☐ | | |
| 7 | quality-service | /api/quality/health | UP | ☐ | | |
| 8 | ethics-service | /api/ethics/health | UP | ☐ | | |
| 9 | classification-service | /api/classification/health | UP | ☐ | | |
| 10 | masking-service | /api/masking/health | UP | ☐ | | |
| 11 | iot-edge-service | /api/iot/health | UP | ☐ | | |
| 12 | edams-llm | /api/llm/health | UP | ☐ | | |
| 13 | edams-notification | /api/notification/health | UP | ☐ | | |

### 4.2 功能验证检查

| 序号 | 功能模块 | 测试场景 | 测试方法 | 状态 | 说明 |
|------|---------|---------|---------|------|------|
| 1 | 用户认证 | 用户登录 | POST /api/auth/login | ☐ | |
| 2 | 用户认证 | 获取Token | 检查JWT | ☐ | |
| 3 | 用户管理 | 创建用户 | POST /api/users | ☐ | |
| 4 | 资产管理 | 创建资产 | POST /api/assets | ☐ | |
| 5 | 资产管理 | 查询资产 | GET /api/assets/{id} | ☐ | |
| 6 | 资产管理 | 搜索资产 | GET /api/assets/search | ☐ | |
| 7 | 血缘关系 | 创建血缘 | POST /api/lineage | ☐ | |
| 8 | 血缘关系 | 查询血缘图 | GET /api/lineage/graph | ☐ | |
| 9 | 数据质量 | 执行检查 | POST /api/quality/check | ☐ | |
| 10 | 数据质量 | 查询结果 | GET /api/quality/results | ☐ | |
| 11 | 伦理评估 | 创建评估 | POST /api/ethics/assessments | ☐ | |
| 12 | 分类分级 | 自动分类 | POST /api/classification/classify | ☐ | |
| 13 | 脱敏管理 | 脱敏数据 | POST /api/masking/mask | ☐ | |
| 14 | 边缘设备 | 注册设备 | POST /api/iot/devices | ☐ | |
| 15 | LLM集成 | 调用GPT | POST /api/llm/chat | ☐ | |
| 16 | 通知服务 | 发送通知 | POST /api/notification/send | ☐ | |

---

## 五、监控和日志检查

### 5.1 监控检查

| 序号 | 监控项 | 检查方法 | 预期结果 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | Prometheus | 访问UI | 正常显示 | ☐ | |
| 2 | Grafana | 访问UI | 大盘正常 | ☐ | |
| 3 | 告警规则 | 触发测试告警 | 收到告警 | ☐ | |
| 4 | SkyWalking | 访问UI | 链路追踪正常 | ☐ | |
| 5 | 应用指标 | Prometheus查询 | 数据正常 | ☐ | |
| 6 | JVM指标 | 查看内存/GC | 数据正常 | ☐ | |
| 7 | 中间件指标 | 查看QPS/延迟 | 数据正常 | ☐ | |

### 5.2 日志检查

| 序号 | 日志项 | 检查方法 | 预期结果 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | 应用日志 | ELK查询 | 正常写入 | ☐ | |
| 2 | 错误日志 | 搜索ERROR级别 | 无异常 | ☐ | |
| 3 | 访问日志 | 查看nginx日志 | 正常记录 | ☐ | |
| 4 | 审计日志 | 查询操作记录 | 正常记录 | ☐ | |
| 5 | 日志保留 | 检查保留策略 | 7天保留 | ☐ | |

---

## 六、安全检查

### 6.1 身份认证检查

| 序号 | 检查项 | 检查方法 | 预期结果 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | HTTPS访问 | 访问域名 | 跳转HTTPS | ☐ | |
| 2 | Token有效性 | 使用过期Token | 返回401 | ☐ | |
| 3 | 密码强度 | 注册弱密码 | 拒绝注册 | ☐ | |
| 4 | 会话超时 | 静置30分钟 | 会话失效 | ☐ | |
| 5 | 并发登录 | 多设备登录 | 限制会话数 | ☐ | |

### 6.2 授权检查

| 序号 | 检查项 | 检查方法 | 预期结果 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | 角色权限 | 普通用户访问管理接口 | 返回403 | ☐ | |
| 2 | 数据权限 | A用户访问B用户数据 | 返回403 | ☐ | |
| 3 | API权限 | 未授权API调用 | 返回401/403 | ☐ | |
| 4 | 敏感操作 | 修改关键配置 | 需要授权 | ☐ | |

### 6.3 数据安全检查

| 序号 | 检查项 | 检查方法 | 预期结果 | 状态 | 说明 |
|------|--------|---------|---------|------|------|
| 1 | 敏感数据脱敏 | 查询用户信息 | 手机号脱敏 | ☐ | |
| 2 | 数据库加密 | 查看数据库 | 密码已加密 | ☐ | |
| 3 | 传输加密 | 抓包分析 | 数据加密 | ☐ | |
| 4 | 备份加密 | 检查备份文件 | 已加密 | ☐ | |

---

## 七、性能检查

### 7.1 基础性能检查

| 序号 | 检查项 | 检查方法 | 目标值 | 实际值 | 状态 | 说明 |
|------|--------|---------|--------|--------|------|------|
| 1 | API响应时间P50 | APM/日志分析 | ≤100ms | | ☐ | |
| 2 | API响应时间P95 | APM/日志分析 | ≤500ms | | ☐ | |
| 3 | API响应时间P99 | APM/日志分析 | ≤1000ms | | ☐ | |
| 4 | 搜索响应时间 | 实际测试 | ≤200ms | | ☐ | |
| 5 | 血缘查询时间 | 实际测试 | ≤500ms | | ☐ | |
| 6 | 并发用户支持 | 压测验证 | ≥1000 | | ☐ | |
| 7 | QPS | 压测验证 | ≥5000 | | ☐ | |

### 7.2 资源使用检查

| 序号 | 资源项 | 检查方法 | 目标值 | 实际值 | 状态 | 说明 |
|------|--------|---------|--------|--------|------|------|
| 1 | CPU使用率 | kubectl top pods | ≤70% | | ☐ | |
| 2 | 内存使用率 | kubectl top pods | ≤80% | | ☐ | |
| 3 | JVM堆内存 | Prometheus查询 | ≤85% | | ☐ | |
| 4 | 数据库连接池 | Prometheus查询 | ≤80% | | ☐ | |
| 5 | Kafka消费延迟 | Prometheus查询 | ≤10000 | | ☐ | |
| 6 | 磁盘使用率 | df -h | ≤70% | | ☐ | |

---

## 八、灾备和恢复检查

### 8.1 备份检查

| 序号 | 备份项 | 检查方法 | 状态 | 说明 |
|------|--------|---------|------|------|
| 1 | 数据库备份 | 检查备份文件 | ☐ | 每日全量+增量 |
| 2 | 配置文件备份 | 检查备份文件 | ☐ | |
| 3 | 镜像备份 | 检查镜像列表 | ☐ | |
| 4 | 密钥备份 | 检查密钥导出 | ☐ | |

### 8.2 恢复演练

| 序号 | 演练项 | 演练方法 | 状态 | 恢复时间 | 说明 |
|------|--------|---------|------|---------|------|
| 1 | 数据库恢复 | 从备份恢复 | ☐ | | |
| 2 | 服务恢复 | 重新部署Pod | ☐ | | |
| 3 | 全量恢复 | 完整灾难恢复 | ☐ | | |

---

## 九、运维文档检查

| 序号 | 文档项 | 检查方法 | 状态 | 说明 |
|------|--------|---------|------|------|
| 1 | 部署文档 | 文档完整性 | ☐ | |
| 2 | 运维手册 | 运维流程文档 | ☐ | |
| 3 | 应急响应 | 故障处理流程 | ☐ | |
| 4 | 联系清单 | 技术支持联系 | ☐ | |
| 5 | 架构文档 | 系统架构图 | ☐ | |

---

## 十、审批和签字

### 10.1 检查确认

| 角色 | 姓名 | 部门 | 签字 | 日期 | 意见 |
|------|------|------|------|------|------|
| 项目经理 | | | | | |
| 技术负责人 | | | | | |
| 运维负责人 | | | | | |
| 安全负责人 | | | | | |
| 测试负责人 | | | | | |
| 业务负责人 | | | | | |

### 10.2 审批意见

**项目经理审批意见**：

```空白
在此处填写审批意见...
```

**技术负责人审批意见**：

```空白
在此处填写审批意见...
```

### 10.3 最终批准

| 项目 | 批准人 | 签字 | 日期 |
|------|--------|------|------|
| 允许部署生产环境 | | | |

---

## 检查清单使用说明

1. **执行顺序**：按照检查清单从上到下顺序执行
2. **检查方法**：使用提供的检查方法进行验证
3. **状态标记**：每项检查完成后在"状态"列标记
   - ☐ 待检查
   - ✅ 通过
   - ❌ 未通过
   - ⚠️ 部分通过（需说明）
4. **问题记录**：未通过项记录到问题跟踪系统
5. **检查签字**：每项检查由对应负责人签字确认
6. **审批流程**：全部检查通过后由项目经理审批

---

**文档版本**：v1.0  
**创建日期**：2026-05-11  
**最后更新**：2026-05-11  
**文档维护人**：EDAMS项目组  
