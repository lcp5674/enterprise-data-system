# Keycloak/SSO部署配置指南

## 1. 环境要求

### 硬件要求
- CPU: 4 cores+
- Memory: 8GB+
- Disk: 50GB+

### 软件要求
- Docker & Docker Compose
- PostgreSQL 15+
- Keycloak 23.0+

## 2. 快速部署

### 2.1 配置环境变量

创建 `.env` 文件：

```bash
# Keycloak Admin
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=your_secure_password

# Database
KEYCLOAK_DB_PASSWORD=your_db_password

# Client Secrets
KEYCLOAK_CLIENT_SECRET=your_client_secret
KEYCLOAK_BACKEND_SECRET=your_backend_secret

# Host Configuration
KEYCLOAK_HOSTNAME_STRICT=false
KEYCLOAK_HOSTNAME_STRICT_HTTPS=false
KEYCLOAK_HTTP_ENABLED=true

# SMTP (Optional)
MAIL_HOST=smtp.company.com
MAIL_PORT=587
MAIL_USERNAME=noreply@company.com
MAIL_PASSWORD=your_mail_password
MAIL_FROM=EDAMS <noreply@company.com>
```

### 2.2 启动Keycloak

```bash
cd infrastructure/keycloak
docker-compose -f docker-compose-keycloak.yml up -d

# 查看日志
docker-compose -f docker-compose-keycloak.yml logs -f

# 检查健康状态
curl http://localhost:8180/health/ready
```

### 2.3 导入Realm配置

Realm配置文件位于 `infrastructure/keycloak/realms/edams-realm.json`

启动时会自动导入，也可手动导入：

```bash
# 进入Keycloak容器
docker exec -it edams-keycloak bash

# 使用kcadm导入
/opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin_secret

# 导入Realm
/opt/keycloak/bin/kcadm.sh create realms \
  -f /opt/keycloak/data/import/edams-realm.json \
  -s enabled=true
```

## 3. 配置说明

### 3.1 Realm配置

EDAMS使用名为 `edams` 的Realm，包含：

**角色 (Realm Roles):**
- `edams-admin`: 系统管理员
- `edams-user`: 普通用户
- `edams-data-steward`: 数据治理员
- `edams-auditor`: 审计员

**用户组 (Groups):**
- `/edams-admins`: 管理员组
- `/edams-users`: 普通用户组
- `/edams-stewards`: 治理员组
- `/edams-auditors`: 审计员组

### 3.2 客户端配置

**edams-client (前端SPA应用):**
- Client Protocol: openid-connect
- Access Type: public/confidential
- Standard Flow: 启用 (用于Web应用)
- Direct Access Grants: 禁用
- Service Accounts: 启用

**edams-backend (后端微服务):**
- Access Type: confidential
- Service Accounts: 启用
- Direct Access Grants: 启用

### 3.3 回调URL配置

根据部署环境更新 `edams-realm.json` 中的 `redirectUris`:

```json
"redirectUris": [
  "http://localhost:8000/login/oauth2/code/keycloak",
  "http://localhost:3000/login/oauth2/code/keycloak",
  "https://edams.company.com/*"
]
```

## 4. EDAMS服务配置

### 4.1 环境变量配置

在部署EDAMS服务时，添加以下环境变量：

```bash
# Keycloak配置
KEYCLOAK_ENABLED=true
KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/edams
KEYCLOAK_CLIENT_ID=edams-client
KEYCLOAK_CLIENT_SECRET=your_client_secret
KEYCLOAK_JWK_SET_URI=http://localhost:8180/realms/edams/protocol/openid-connect/certs

# 生产环境建议使用HTTPS
# KEYCLOAK_ISSUER_URI=https://keycloak.company.com/realms/edams
# KEYCLOAK_JWK_SET_URI=https://keycloak.company.com/realms/edams/protocol/openid-connect/certs
```

### 4.2 Nacos配置

在Nacos配置中心添加Keycloak配置：

```yaml
keycloak:
  enabled: ${KEYCLOAK_ENABLED:true}
  realm: edams
  jwt:
    issuer-uri: ${KEYCLOAK_ISSUER_URI}
    jwk-set-uri: ${KEYCLOAK_JWK_SET_URI}
  client:
    id: ${KEYCLOAK_CLIENT_ID}
    secret: ${KEYCLOAK_CLIENT_SECRET}
```

## 5. 前端集成

### 5.1 路由配置

在 `config/routes.ts` 中添加SSO路由：

```typescript
{
  path: '/sso',
  component: './sso',
  routes: [
    { path: '/sso/login', component: './sso/SSOLogin' },
    { path: '/sso/callback', component: './sso/SSOCallback' },
  ],
}
```

### 5.2 登录页面集成

在登录页面添加SSO登录按钮：

```typescript
import { initiateSSOLogin } from '@/services/sso';

const handleSSOLogin = async () => {
  const response = await initiateSSOLogin(window.location.origin);
  if (response.authorizationUri) {
    window.location.href = response.authorizationUri;
  }
};
```

## 6. 故障排查

### 6.1 常见问题

**1. 回调URL不匹配**
```
Error: Invalid redirect_uri
```
解决：确保回调URL与Keycloak客户端配置中的 `redirectUris` 匹配

**2. CORS问题**
```
Error: CORS origin not allowed
```
解决：在Keycloak客户端配置中添加前端域名到 `webOrigins`

**3. Token验证失败**
```
Error: Token verification failed
```
解决：检查 `KEYCLOAK_JWK_SET_URI` 是否可访问

### 6.2 日志查看

```bash
# Keycloak日志
docker-compose logs keycloak

# 应用日志（EDAMS Auth服务）
docker-compose logs edams-auth
```

## 7. 安全建议

### 7.1 生产环境

1. **使用HTTPS**
   - 配置SSL证书
   - 设置 `KEYCLOAK_HOSTNAME_STRICT_HTTPS=true`

2. **强密码策略**
   - 配置密码策略
   - 启用双因素认证

3. **限制回调URL**
   - 生产环境使用精确URL而非通配符
   - 定期审查允许的回调URL

4. **密钥轮换**
   - 定期轮换客户端密钥
   - 更新EDAMS配置

### 7.2 监控

启用Keycloak指标：

```yaml
KEYCLOAK_METRICS_ENABLED=true
```

可通过Prometheus采集指标数据。

## 8. 参考链接

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
