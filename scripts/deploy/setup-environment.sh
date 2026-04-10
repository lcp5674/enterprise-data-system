#!/bin/bash

# setup-environment.sh
# EDAMS生产环境初始化脚本
# 此脚本负责初始化生产环境的所有基础设施组件

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]$(date '+%Y-%m-%d %H:%M:%S') $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}[WARN]$(date '+%Y-%m-%d %H:%M:%S') $1${NC}"
}

log_error() {
    echo -e "${RED}[ERROR]$(date '+%Y-%m-%d %H:%M:%S') $1${NC}" >&2
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 命令不存在，请先安装"
        exit 1
    fi
}

# 加载环境变量
load_env() {
    ENV_FILE="${1:-.env}"
    
    if [ -f "$ENV_FILE" ]; then
        log_info "从 $ENV_FILE 加载环境变量"
        set -a
        source "$ENV_FILE"
        set +a
    else
        log_warn "环境变量文件 $ENV_FILE 不存在"
    fi
}

# 检查集群连接
check_cluster() {
    log_info "检查Kubernetes集群连接..."
    
    if ! kubectl cluster-info > /dev/null 2>&1; then
        log_error "无法连接到Kubernetes集群"
        exit 1
    fi
    
    CLUSTER=$(kubectl config current-context)
    log_info "当前集群: $CLUSTER"
    
    # 获取集群信息
    kubectl get nodes -o wide
}

# 创建命名空间
create_namespace() {
    local namespace=$1
    
    log_info "创建命名空间: $namespace"
    
    if kubectl get namespace "$namespace" > /dev/null 2>&1; then
        log_warn "命名空间 $namespace 已存在"
        return
    fi
    
    kubectl create namespace "$namespace"
    
    # 添加标签
    kubectl label namespace "$namespace" environment=${ENVIRONMENT:-prod}
    kubectl label namespace "$namespace" team=edams-devops
}

# 创建角色和权限
create_rbac() {
    local namespace=$1
    
    log_info "为 $namespace 创建RBAC配置"
    
    # 创建ServiceAccount
    kubectl apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: edms-operator
  namespace: $namespace
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: edms-operator-role
  namespace: $namespace
rules:
- apiGroups: [""]
  resources: ["pods", "services", "endpoints", "persistentvolumeclaims", "secrets", "configmaps"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
- apiGroups: ["apps"]
  resources: ["deployments", "statefulsets", "replicasets", "daemonsets"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: edms-operator-binding
  namespace: $namespace
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: edms-operator-role
subjects:
- kind: ServiceAccount
  name: edms-operator
  namespace: $namespace
EOF
}

# 创建TLS证书
create_tls_certificates() {
    local namespace=$1
    local domain=$2
    
    log_info "为 $domain 创建TLS证书"
    
    if kubectl get -n cert-manager > /dev/null 2>&1; then
        # 使用cert-manager创建证书
        kubectl apply -f - <<EOF
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: letsencrypt-prod
  namespace: $namespace
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: devops@enterprise.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
---
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: tls-certificate
  namespace: $namespace
spec:
  secretName: tls-secret
  duration: 2160h # 90 days
  renewBefore: 360h # 15 days
  issuerRef:
    name: letsencrypt-prod
    kind: Issuer
  dnsNames:
  - $domain
  - "*.$domain"
EOF
    else
        log_warn "cert-manager未安装，生成自签名证书"
        
        # 生成自签名证书
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout /tmp/tls.key -out /tmp/tls.crt \
            -subj "/CN=$domain/O=Enterprise" > /dev/null 2>&1
            
        kubectl create secret tls tls-secret \
            --namespace $namespace \
            --key /tmp/tls.key \
            --cert /tmp/tls.crt
    fi
}

# 创建存储类
create_storage_classes() {
    log_info "创建存储类"
    
    # SSD快速存储类
    kubectl apply -f - <<EOF
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: ssd-fast
provisioner: kubernetes.io/aws-ebs # 根据实际云提供商修改
parameters:
  type: gp3
  fsType: ext4
reclaimPolicy: Retain
allowVolumeExpansion: true
mountOptions:
  - defaults
  - noatime
  - nodiratime
---
# HDD标准存储类
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: hdd-standard
provisioner: kubernetes.io/aws-ebs
parameters:
  type: sc1
reclaimPolicy: Retain
volumeBindingMode: WaitForFirstConsumer
EOF
}

# 创建网络策略
create_network_policies() {
    local namespace=$1
    
    log_info "为 $namespace 创建网络策略"
    
    # 默认拒绝所有流量
    kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: $namespace
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
---
# 允许监控相关流量
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-monitoring
  namespace: $namespace
spec:
  podSelector:
    matchLabels:
      monitoring: "true"
  policyTypes:
  - Ingress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          monitoring: "true"
    ports:
    - protocol: TCP
      port: 8080
    - protocol: TCP
      port: 9090
---
# 允许数据库流量
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-database
  namespace: $namespace
spec:
  podSelector:
    matchLabels:
      component: database
  policyTypes:
  - Ingress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          environment: $namespace
    ports:
    - protocol: TCP
      port: 5432
EOF
}

# 创建监控配置
create_monitoring() {
    local namespace=$1
    
    log_info "为 $namespace 创建监控配置"
    
    # 创建Prometheus资源
    kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: edms-service-monitor
  namespace: $namespace
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app.kubernetes.io/part-of: edms
  namespaceSelector:
    matchNames:
    - $namespace
  endpoints:
  - port: metrics
    interval: 30s
    scrapeTimeout: 10s
    path: /actuator/prometheus
  - port: http
    interval: 30s
    scrapeTimeout: 10s
    path: /actuator/info
---
apiVersion: monitoring.coreos.com/v1
kind: PodMonitor
metadata:
  name: edms-pod-monitor
  namespace: $namespace
  labels:
    release: prometheus
spec:
  selector:
    matchLabels:
      app.kubernetes.io/part-of: edms
  podMetricsEndpoints:
  - port: metrics
    interval: 30s
    scrapeTimeout: 10s
EOF
}

# 主函数
main() {
    # 检查必需工具
    check_command kubectl
    check_command helm
    check_command openssl
    
    # 加载环境变量
    load_env ".env"
    
    # 设置默认值
    ENVIRONMENT=${ENVIRONMENT:-prod}
    DOMAIN=${DOMAIN:-edams.enterprise.com}
    NAMESPACE="${NAMESPACE:-edams-${ENVIRONMENT}}"
    
    log_info "开始设置环境: $ENVIRONMENT"
    log_info "域名: $DOMAIN"
    log_info "命名空间: $NAMESPACE"
    
    # 执行初始化步骤
    check_cluster
    create_namespace "$NAMESPACE"
    create_rbac "$NAMESPACE"
    create_tls_certificates "$NAMESPACE" "$DOMAIN"
    create_storage_classes
    create_network_policies "$NAMESPACE"
    create_monitoring "$NAMESPACE"
    
    log_info "环境设置完成！"
    
    # 显示下一步操作
    echo ""
    echo "下一步操作："
    echo "1. 安装依赖: helm dependency update infrastructure/helm/edams"
    echo "2. 部署应用: helm upgrade --install edams infrastructure/helm/edams --namespace $NAMESPACE"
    echo "3. 验证部署: kubectl get all -n $NAMESPACE"
    echo ""
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi