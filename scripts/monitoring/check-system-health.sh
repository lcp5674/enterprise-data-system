#!/bin/bash

# check-system-health.sh
# EDAMS系统健康检查脚本
# 检查所有组件状态，生成健康报告

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 全局变量
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
REPORT_FILE="/tmp/edams_health_report_${TIMESTAMP}.md"
NAMESPACE=${NAMESPACE:-edams-prod}
SLACK_WEBHOOK_URL=${SLACK_WEBHOOK_URL:-}
EMAIL_RECIPIENTS=${EMAIL_RECIPIENTS:-devops@enterprise.com}

# 状态变量
STATUS_PASS=0
STATUS_WARN=1
STATUS_FAIL=2

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}[WARN] $1${NC}"
}

log_error() {
    echo -e "${RED}[ERROR] $1${NC}"
}

# 检查组件状态
check_k8s() {
    local component=$1
    local namespace=${2:-$NAMESPACE}
    local health_status=$STATUS_PASS
    
    log_info "检查 $component 状态..."
    
    if ! kubectl get "$component" -n "$namespace" --no-headers > /dev/null 2>&1; then
        health_status=$STATUS_FAIL
        log_error "$component 检查失败"
        return $health_status
    fi
    
    # 检查每个Pod的状态
    kubectl get pods -n "$namespace" -l "app.kubernetes.io/component=$component" -o json | \
        jq -r '.items[] | "\(.metadata.name): \(.status.phase)"' | while read -r pod; do
            pod_name=$(echo $pod | cut -d':' -f1)
            status=$(echo $pod | cut -d':' -f2- | xargs)
            
            case $status in
                "Running")
                    echo "✅ $pod_name: $status"
                    ;;
                "Pending"|"ContainerCreating")
                    health_status=$STATUS_WARN
                    echo "⚠️  $pod_name: $status"
                    ;;
                "Failed"|"Error"|"CrashLoopBackOff")
                    health_status=$STATUS_FAIL
                    echo "❌ $pod_name: $status"
                    
                    # 获取失败原因
                    kubectl describe pod "$pod_name" -n "$namespace" | \
                        grep -A5 "Events:" || true
                    ;;
                *)
                    health_status=$STATUS_WARN
                    echo "❓ $pod_name: $status"
                    ;;
            esac
    done
    
    if [ $health_status -eq $STATUS_PASS ]; then
        all_pods=$(kubectl get pods -n "$namespace" -l "app.kubernetes.io/component=$component" --no-headers | wc -l)
        ready_pods=$(kubectl get pods -n "$namespace" -l "app.kubernetes.io/component=$component" -o json | \
            jq '.items[] | select(.status.conditions[] | select(.type=="Ready" and .status=="True")) | .metadata.name' | wc -l)
        
        echo "📊 Pod统计: $ready_pods/$all_pods 已就绪"
    fi
    
    return $health_status
}

# 检查数据库连接
check_database() {
    log_info "检查数据库连接..."
    
    local health_status=$STATUS_PASS
    
    # 检查PostgreSQL
    if kubectl get pods -n "$NAMESPACE" -l "app=postgresql" --no-headers > /dev/null 2>&1; then
        db_pod=$(kubectl get pods -n "$NAMESPACE" -l "app=postgresql" -o name | head -1)
        
        # 测试连接
        if kubectl exec -n "$NAMESPACE" "$db_pod" -- pg_isready -U postgres > /dev/null 2>&1; then
            echo "✅ PostgreSQL: 连接正常"
        else
            health_status=$STATUS_FAIL
            echo "❌ PostgreSQL: 连接失败"
        fi
        
        # 检查磁盘空间
        df_output=$(kubectl exec -n "$NAMESPACE" "$db_pod" -- df -h | grep "/bitnami/postgresql" || true)
        if [ -n "$df_output" ]; then
            echo "💾 PostgreSQL磁盘: $df_output"
        fi
    else
        echo "⚠️  PostgreSQL: 未找到Pod"
        health_status=$STATUS_WARN
    fi
    
    # 检查Redis
    if kubectl get pods -n "$NAMESPACE" -l "app=redis" --no-headers > /dev/null 2>&1; then
        redis_pod=$(kubectl get pods -n "$NAMESPACE" -l "app=redis" -o name | head -1)
        
        # 测试连接
        if kubectl exec -n "$NAMESPACE" "$redis_pod" -- redis-cli ping | grep -q "PONG"; then
            echo "✅ Redis: 连接正常"
            
            # 检查内存使用情况
            redis_info=$(kubectl exec -n "$NAMESPACE" "$redis_pod" -- redis-cli info memory 2>/dev/null || true)
            memory_used=$(echo "$redis_info" | grep "used_memory_human" | cut -d: -f2)
            memory_max=$(echo "$redis_info" | grep "maxmemory_human" | cut -d: -f2)
            
            echo "🧠 Redis内存: 已用$memory_used / 最大$memory_max"
        else
            health_status=$STATUS_FAIL
            echo "❌ Redis: 连接失败"
        fi
    else
        echo "⚠️  Redis: 未找到Pod"
        health_status=$STATUS_WARN
    fi
    
    return $health_status
}

# 检查网络连接
check_network() {
    log_info "检查网络连接..."
    
    local health_status=$STATUS_PASS
    
    # 检查服务端点
    kubectl get svc -n "$NAMESPACE" -o json | \
        jq -r '.items[] | select(.spec.type=="ClusterIP" or .spec.type=="LoadBalancer") | "\(.metadata.name): \(.spec.ports[].port)"' | \
        while read -r service; do
            svc_name=$(echo $service | cut -d':' -f1)
            svc_port=$(echo $service | cut -d':' -f2 | xargs)
            
            # 使用临时Pod测试连接
            test_pod="network-test-$(date +%s)"
            kubectl run "$test_pod" -n "$NAMESPACE" --image=busybox --restart=Never -- sleep 3600 > /dev/null 2>&1
            
            # 等待Pod就绪
            sleep 2
            
            if kubectl exec -n "$NAMESPACE" "$test_pod" -- nc -z -w3 "$svc_name" "$svc_port" > /dev/null 2>&1; then
                echo "✅ $svc_name:$svc_port: 连接正常"
            else
                health_status=$STATUS_WARN
                echo "⚠️  $svc_name:$svc_port: 连接失败"
            fi
            
            # 清理测试Pod
            kubectl delete pod "$test_pod" -n "$NAMESPACE" > /dev/null 2>&1
    done
    
    # 检查Ingress
    if kubectl get ingress -n "$NAMESPACE" --no-headers > /dev/null 2>&1; then
        ingress_info=$(kubectl get ingress -n "$NAMESPACE" -o json)
        ingress_count=$(echo "$ingress_info" | jq '.items | length')
        echo "🌐 Ingress: 已配置$ingress_count个"
    fi
    
    return $health_status
}

# 检查监控系统
check_monitoring() {
    log_info "检查监控系统..."
    
    local health_status=$STATUS_PASS
    
    # 检查Prometheus
    if kubectl get pods -n monitoring -l "app=prometheus" --no-headers > /dev/null 2>&1; then
        echo "✅ Prometheus: 运行正常"
    else
        health_status=$STATUS_WARN
        echo "⚠️  Prometheus: 未运行或未找到"
    fi
    
    # 检查Grafana
    if kubectl get pods -n monitoring -l "app=grafana" --no-headers > /dev/null 2>&1; then
        echo "✅ Grafana: 运行正常"
    else
        health_status=$STATUS_WARN
        echo "⚠️  Grafana: 未运行或未找到"
    fi
    
    # 检查AlertManager
    if kubectl get pods -n monitoring -l "app=alertmanager" --no-headers > /dev/null 2>&1; then
        echo "✅ AlertManager: 运行正常"
    else
        health_status=$STATUS_WARN
        echo "⚠️  AlertManager: 未运行或未找到"
    fi
    
    return $health_status
}

# 检查资源使用情况
check_resources() {
    log_info "检查资源使用情况..."
    
    echo "📈 节点资源:"
    kubectl get nodes -o wide | awk 'NR==1 || $3 !~ /master/' | while read -r line; do
        if [ -n "$line" ]; then
            echo "  $line"
        fi
    done
    
    echo ""
    echo "📊 Pod资源请求/限制:"
    kubectl get pods -n "$NAMESPACE" -o json | \
        jq -r '.items[] | "\(.metadata.name): \(.spec.containers[].resources.requests.cpu)/\(.spec.containers[].resources.limits.cpu) CPU, \(.spec.containers[].resources.requests.memory)/\(.spec.containers[].resources.limits.memory) Memory"' | \
        head -10
    
    echo ""
    echo "🧮 集群总资源使用情况:"
    kubectl top nodes 2>/dev/null || echo "  (需要安装metrics-server)"
}

# 生成健康报告
generate_report() {
    local overall_status=$1
    shift
    local checks=("$@")
    
    cat > "$REPORT_FILE" <<EOF
# EDAMS系统健康检查报告
**时间**: $(date '+%Y-%m-%d %H:%M:%S')  
**命名空间**: $NAMESPACE  
**环境**: ${ENVIRONMENT:-prod}

## 总体状态
EOF

    case $overall_status in
        $STATUS_PASS)
            echo "**✅ 系统正常**" >> "$REPORT_FILE"
            ;;
        $STATUS_WARN)
            echo "**⚠️  系统有警告**" >> "$REPORT_FILE"
            ;;
        $STATUS_FAIL)
            echo "**❌ 系统有错误**" >> "$REPORT_FILE"
            ;;
    esac
    
    cat >> "$REPORT_FILE" <<EOF

## 详细检查结果

### 1. Kubernetes组件状态
$(grep -A5 "检查.*状态" $REPORT_FILE.tmp 2>/dev/null || echo "无数据")

### 2. 数据库状态
$(grep -A5 "检查数据库连接" $REPORT_FILE.tmp 2>/dev/null || echo "无数据")

### 3. 网络状态
$(grep -A5 "检查网络连接" $REPORT_FILE.tmp 2>/dev/null || echo "无数据")

### 4. 监控系统状态
$(grep -A5 "检查监控系统" $REPORT_FILE.tmp 2>/dev/null || echo "无数据")

### 5. 资源使用情况
$(grep -A5 "检查资源使用情况" $REPORT_FILE.tmp 2>/dev/null || echo "无数据")

## 建议操作
$([ $overall_status -eq $STATUS_FAIL ] && echo "1. 立即排查系统错误" || echo "1. 系统运行正常，继续保持监控")

## 系统信息
- 节点数量: $(kubectl get nodes --no-headers | wc -l)
- Pod总数: $(kubectl get pods -n "$NAMESPACE" --no-headers | wc -l)
- 服务数量: $(kubectl get svc -n "$NAMESPACE" --no-headers | wc -l)
- 命名空间: $NAMESPACE

> 报告生成时间: $(date)
EOF
}

# 发送通知
send_notification() {
    local status=$1
    
    if [ -z "$SLACK_WEBHOOK_URL" ]; then
        log_warn "未配置SLACK_WEBHOOK_URL，跳过Slack通知"
        return
    fi
    
    local message
    local color
    
    case $status in
        $STATUS_PASS)
            message="✅ EDAMS系统运行正常"
            color="good"
            ;;
        $STATUS_WARN)
            message="⚠️  EDAMS系统有警告"
            color="warning"
            ;;
        $STATUS_FAIL)
            message="🚨 EDAMS系统出现错误"
            color="danger"
            ;;
    esac
    
    # 发送到Slack
    curl -X POST -H 'Content-type: application/json' --data \
        "{\"attachments\":[{\"color\":\"$color\",\"title\":\"$message\",\"text\":\"环境: $ENVIRONMENT\n时间: $(date)\n报告: $(cat $REPORT_FILE | head -3)\"}]}" \
        "$SLACK_WEBHOOK_URL" > /dev/null 2>&1
    
    log_info "已发送通知到Slack"
}

# 主函数
main() {
    log_info "开始EDAMS系统健康检查..."
    log_info "命名空间: $NAMESPACE"
    
    # 保存输出到临时文件
    exec 2>&1 | tee "$REPORT_FILE.tmp"
    
    local overall_status=$STATUS_PASS
    local check_results=()
    
    # 执行各项检查
    check_k8s "gateway" || overall_status=$(($overall_status > $? ? $overall_status : $?))
    check_k8s "auth" || overall_status=$(($overall_status > $? ? $overall_status : $?))
    check_k8s "asset" || overall_status=$(($overall_status > $? ? $overall_status : $?))
    check_database || overall_status=$(($overall_status > $? ? $overall_status : $?))
    check_network || overall_status=$(($overall_status > $? ? $overall_status : $?))
    check_monitoring || overall_status=$(($overall_status > $? ? $overall_status : $?))
    check_resources
    
    # 生成报告
    generate_report "$overall_status" "${check_results[@]}"
    
    # 显示报告
    echo ""
    echo "📋 健康检查报告已生成: $REPORT_FILE"
    echo ""
    cat "$REPORT_FILE" | head -20
    
    # 发送通知
    send_notification "$overall_status"
    
    # 退出状态
    if [ $overall_status -eq $STATUS_FAIL ]; then
        log_error "系统检查发现错误！"
        exit 1
    elif [ $overall_status -eq $STATUS_WARN ]; then
        log_warn "系统检查发现警告"
        exit 0
    else
        log_info "系统检查正常"
        exit 0
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi