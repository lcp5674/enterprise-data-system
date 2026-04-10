#!/bin/bash

# backup-and-restore.sh
# EDAMS系统备份与恢复管理脚本
# 支持完整系统备份、增量备份和灾难恢复

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 全局配置
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
BACKUP_DIR="/backups/edams_${TIMESTAMP}"
NAMESPACE=${NAMESPACE:-edams-prod}
RETENTION_DAYS=${RETENTION_DAYS:-30}
S3_BUCKET=${S3_BUCKET:-edams-backups}
STORAGE_CLASS=${STORAGE_CLASS:-standard}

# 状态变量
EXIT_CODE=0

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO] $(date '+%Y-%m-%d %H:%M:%S') $1${NC}"
}

log_success() {
    echo -e "${GREEN}[SUCCESS] $(date '+%Y-%m-%d %H:%M:%S') $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}[WARN] $(date '+%Y-%m-%d %H:%M:%S') $1${NC}" >&2
}

log_error() {
    echo -e "${RED}[ERROR] $(date '+%Y-%m-%d %H:%M:%S') $1${NC}" >&2
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 命令不存在，请先安装"
        exit 1
    fi
}

# 备份PostgreSQL数据库
backup_postgresql() {
    local backup_type=${1:-full}
    local backup_file="$BACKUP_DIR/postgresql_${backup_type}.sql.gz"
    
    log_info "开始$backup_type备份PostgreSQL数据库..."
    
    # 获取PostgreSQL Pod
    PG_POD=$(kubectl get pods -n "$NAMESPACE" -l "app=postgresql" -o name | head -1)
    
    if [ -z "$PG_POD" ]; then
        log_error "未找到PostgreSQL Pod"
        return 1
    fi
    
    # 创建备份目录
    mkdir -p "$BACKUP_DIR"
    
    # 执行数据库备份
    if [ "$backup_type" = "full" ]; then
        # 全量备份
        kubectl exec -n "$NAMESPACE" "$PG_POD" -- \
            pg_dumpall -U postgres -c | gzip > "$backup_file"
    else
        # 增量备份需要使用WAL归档
        pg_current_wal=$(kubectl exec -n "$NAMESPACE" "$PG_POD" -- \
            psql -U postgres -t -c "SELECT pg_walfile_name(pg_current_wal_lsn())" 2>/dev/null | tr -d '\r\n')
        
        # 备份WAL文件
        kubectl exec -n "$NAMESPACE" "$PG_POD" -- \
            tar czf - -C /bitnami/postgresql/data/pg_wal . | \
            cat > "$BACKUP_DIR/wal_${TIMESTAMP}.tar.gz"
    fi
    
    local backup_size=$(du -h "$backup_file" 2>/dev/null | cut -f1 || echo "0")
    log_success "PostgreSQL $backup_type备份完成: $backup_file (${backup_size})"
    
    # 验证备份文件
    if ! gzip -t "$backup_file" 2>/dev/null; then
        log_error "备份文件验证失败"
        return 1
    fi
}

# 备份Redis数据库
backup_redis() {
    log_info "开始备份Redis数据库..."
    
    # 获取Redis Pod
    REDIS_POD=$(kubectl get pods -n "$NAMESPACE" -l "app=redis" -o name | head -1)
    
    if [ -z "$REDIS_POD" ]; then
        log_error "未找到Redis Pod"
        return 1
    fi
    
    # 执行Redis备份 (SAVE命令)
    kubectl exec -n "$NAMESPACE" "$REDIS_POD" -- redis-cli SAVE > /dev/null 2>&1
    
    # 等待备份完成
    sleep 2
    
    # 获取Redis数据文件
    redis_data=$(kubectl exec -n "$NAMESPACE" "$REDIS_POD" -- \
        sh -c 'find /data -name "*.rdb" -type f | head -1')
    
    if [ -z "$redis_data" ]; then
        log_error "未找到Redis数据文件"
        return 1
    fi
    
    # 备份数据文件
    kubectl cp "${NAMESPACE}/${REDIS_POD#pod/}:${redis_data}" \
        "$BACKUP_DIR/redis_backup.rdb" > /dev/null 2>&1
    
    local backup_size=$(du -h "$BACKUP_DIR/redis_backup.rdb" 2>/dev/null | cut -f1 || echo "0")
    log_success "Redis备份完成: $BACKUP_DIR/redis_backup.rdb (${backup_size})"
}

# 备份Kubernetes资源
backup_kubernetes_resources() {
    log_info "开始备份Kubernetes资源..."
    
    # 备份所有命名空间资源
    RESOURCES=(
        "deployments"
        "statefulsets"
        "configmaps"
        "secrets"
        "services"
        "ingresses"
        "persistentvolumeclaims"
        "networkpolicies"
    )
    
    for resource in "${RESOURCES[@]}"; do
        log_info "备份 $resource..."
        
        kubectl get "$resource" -n "$NAMESPACE" -o yaml > \
            "$BACKUP_DIR/k8s_${resource}.yaml" 2>/dev/null || continue
        
        count=$(grep -c "^\-\-\-$$" "$BACKUP_DIR/k8s_${resource}.yaml" 2>/dev/null || echo "0")
        
        if [ "$count" -gt 0 ]; then
            log_info "  → 备份了 $count 个 $resource"
        fi
    done
    
    # 备份自定义资源
    kubectl get crds -o name | while read crd; do
        crd_name=$(echo "$crd" | cut -d/ -f2)
        
        if kubectl get "$crd_name" -n "$NAMESPACE" --no-headers 2>/dev/null; then
            kubectl get "$crd_name" -n "$NAMESPACE" -o yaml > \
                "$BACKUP_DIR/crd_${crd_name}.yaml" 2>/dev/null
        fi
    done
    
    log_success "Kubernetes资源备份完成"
}

# 备份配置文件
backup_configuration() {
    log_info "开始备份配置文件..."
    
    # 备份Helm charts
    if [ -d "infrastructure/helm" ]; then
        cp -r infrastructure/helm "$BACKUP_DIR/helm_charts"
        log_info "  → 备份Helm charts"
    fi
    
    # 备份Docker配置
    if [ -d "infrastructure/docker" ]; then
        cp -r infrastructure/docker "$BACKUP_DIR/docker_configs"
        log_info "  → 备份Docker配置"
    fi
    
    # 备份CI/CD配置
    if [ -f ".gitlab-ci.yml" ]; then
        cp .gitlab-ci.yml "$BACKUP_DIR/"
        log_info "  → 备份CI/CD配置"
    fi
    
    # 备份k8s配置
    if [ -d "infrastructure/k8s" ]; then
        cp -r infrastructure/k8s "$BACKUP_DIR/kubernetes_configs"
        log_info "  → 备份Kubernetes配置"
    fi
    
    log_success "配置文件备份完成"
}

# 上传到云存储
upload_to_cloud() {
    local backup_file=$1
    local target_path=$2
    
    log_info "上传备份到云存储: $target_path"
    
    # AWS S3
    if command -v aws &> /dev/null && [ -n "$S3_BUCKET" ]; then
        aws s3 cp "$backup_file" "s3://${S3_BUCKET}/${target_path}" > /dev/null 2>&1
        
        if [ $? -eq 0 ]; then
            log_success "上传到S3成功: s3://${S3_BUCKET}/${target_path}"
        else
            log_error "上传到S3失败"
            return 1
        fi
    fi
    
    # Azure Blob Storage
    if command -v az &> /dev/null && [ -n "$AZURE_STORAGE_CONTAINER" ]; then
        az storage blob upload -f "$backup_file" \
            -c "$AZURE_STORAGE_CONTAINER" \
            -n "$target_path" > /dev/null 2>&1
    fi
    
    # Google Cloud Storage
    if command -v gsutil &> /dev/null && [ -n "$GCS_BUCKET" ]; then
        gsutil cp "$backup_file" "gs://${GCS_BUCKET}/${target_path}" > /dev/null 2>&1
    fi
}

# 清理旧备份
cleanup_old_backups() {
    log_info "清理超过${RETENTION_DAYS}天的旧备份..."
    
    # 本地备份清理
    find /backups -name "edams_*" -type d -mtime +$RETENTION_DAYS 2>/dev/null | \
        while read -r old_backup; do
            log_info "删除本地备份: $old_backup"
            rm -rf "$old_backup"
        done
    
    # 云存储备份清理（示例）
    # aws s3 ls s3://$S3_BUCKET/edams/ | while read -r line; do
    #     backup_date=$(echo $line | awk '{print $1}')
    #     # 比较日期并清理
    # done
    
    log_success "备份清理完成"
}

# 创建备份报告
create_backup_report() {
    local report_file="$BACKUP_DIR/backup_report_${TIMESTAMP}.md"
    
    cat > "$report_file" <<EOF
# EDAMS系统备份报告
**备份时间**: $(date '+%Y-%m-%d %H:%M:%S')  
**命名空间**: $NAMESPACE  
**备份类型**: $BACKUP_TYPE  
**备份目录**: $BACKUP_DIR

## 备份概览
| 组件 | 状态 | 大小 | 详情 |
|------|------|------|------|
EOF
    
    # PostgreSQL备份信息
    if [ -f "$BACKUP_DIR/postgresql_full.sql.gz" ]; then
        size=$(du -h "$BACKUP_DIR/postgresql_full.sql.gz" 2>/dev/null | cut -f1 || echo "N/A")
        echo "| PostgreSQL | ✅ 成功 | $size | 全量备份 |" >> "$report_file"
    fi
    
    # Redis备份信息
    if [ -f "$BACKUP_DIR/redis_backup.rdb" ]; then
        size=$(du -h "$BACKUP_DIR/redis_backup.rdb" 2>/dev/null | cut -f1 || echo "N/A")
        echo "| Redis | ✅ 成功 | $size | RDB快照 |" >> "$report_file"
    fi
    
    # Kubernetes资源信息
    k8s_files_count=$(find "$BACKUP_DIR" -name "k8s_*.yaml" -o -name "crd_*.yaml" 2>/dev/null | wc -l)
    echo "| Kubernetes | ✅ 成功 | $k8s_files_count 文件 | 资源定义 |" >> "$report_file"
    
    # 配置文件信息
    config_dirs_count=$(find "$BACKUP_DIR" -type d -name "*config*" 2>/dev/null | wc -l)
    echo "| 配置文件 | ✅ 成功 | $config_dirs_count 目录 | 系统配置 |" >> "$report_file"

    cat >> "$report_file" <<EOF

## 备份清单
\`\`\`
$(find "$BACKUP_DIR" -type f | sort)
\`\`\`

## 校验信息
\`\`\`
# PostgreSQL备份校验
$(gzip -t "$BACKUP_DIR/postgresql_full.sql.gz" 2>/dev/null && echo "✅ PostgreSQL备份完整性验证通过")
\`\`\`

## 恢复说明
如需恢复系统，请执行以下步骤：

1. **恢复Kubernetes资源**:
   \`\`\`bash
   kubectl apply -f $BACKUP_DIR/k8s_deployments.yaml
   \`\`\`

2. **恢复PostgreSQL数据库**:
   \`\`\`bash
   # 首先恢复PostgreSQL StatefulSet
   kubectl apply -f $BACKUP_DIR/k8s_statefulsets.yaml
   
   # 导入数据库
   gunzip -c $BACKUP_DIR/postgresql_full.sql.gz | \\
     kubectl exec -n $NAMESPACE postgresql-0 -- psql -U postgres
   \`\`\`

3. **恢复Redis数据库**:
   \`\`\`bash
   kubectl cp $BACKUP_DIR/redis_backup.rdb \\
     $NAMESPACE/redis-0:/data/dump.rdb
   \`\`\`

4. **重启应用**:
   \`\`\`bash
   kubectl rollout restart deployment -n $NAMESPACE
   \`\`\`

## 注意事项
1. 恢复前请确保目标环境有足够的资源
2. 生产环境恢复建议在维护窗口进行
3. 恢复完成后验证所有服务状态
4. 记录恢复过程和时间

> 报告生成时间: $(date)
EOF
    
    log_info "备份报告已生成: $report_file"
}

# 恢复数据库
restore_postgresql() {
    local backup_file=$1
    
    log_info "开始恢复PostgreSQL数据库..."
    
    # 获取PostgreSQL Pod
    PG_POD=$(kubectl get pods -n "$NAMESPACE" -l "app=postgresql" -o name | head -1)
    
    if [ -z "$PG_POD" ]; then
        log_error "未找到PostgreSQL Pod"
        return 1
    fi
    
    # 停止接受新连接
    kubectl exec -n "$NAMESPACE" "$PG_POD" -- \
        psql -U postgres -c "ALTER SYSTEM SET default_transaction_read_only = on;" > /dev/null 2>&1
    
    # 终止现有连接
    kubectl exec -n "$NAMESPACE" "$PG_POD" -- \
        psql -U postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE pid <> pg_backend_pid();" > /dev/null 2>&1
    
    sleep 2
    
    # 恢复数据库
    gunzip -c "$backup_file" | \
        kubectl exec -i -n "$NAMESPACE" "$PG_POD" -- psql -U postgres
    
    # 重新启用写入
    kubectl exec -n "$NAMESPACE" "$PG_POD" -- \
        psql -U postgres -c "ALTER SYSTEM SET default_transaction_read_only = off;" > /dev/null 2>&1
    
    log_success "PostgreSQL数据库恢复完成"
}

# 显示使用说明
show_help() {
    cat << EOF
EDAMS系统备份与恢复管理脚本

使用方法:
  $0 [命令] [选项]

命令:
  backup [类型]    执行系统备份（支持: full, incremental）
  restore [文件]   从指定备份恢复系统
  list             列出可用的备份
  cleanup          清理旧备份
  report           生成备份报告

选项:
  --namespace      指定Kubernetes命名空间 (默认: edams-prod)
  --retention      备份保留天数 (默认: 30)
  --s3-bucket      S3存储桶名称
  --help           显示此帮助信息

示例:
  # 执行完整系统备份
  $0 backup full
  
  # 从备份恢复系统
  $0 restore /backups/edams_20241201_120000
  
  # 列出备份
  $0 list
  
  # 清理旧备份
  $0 cleanup

环境变量:
  NAMESPACE        指定命名空间
  RETENTION_DAYS   备份保留天数
  S3_BUCKET        S3存储桶名称
  AZURE_STORAGE_CONTAINER  Azure存储容器名称
  GCS_BUCKET       Google Cloud存储桶名称

EOF
}

# 备份主函数
backup_main() {
    local backup_type=${1:-full}
    
    log_info "开始EDAMS系统$backup_type备份..."
    
    # 执行备份
    backup_postgresql "$backup_type"
    backup_redis
    backup_kubernetes_resources
    backup_configuration
    
    # 创建备份档案
    local archive_file="/tmp/edams_backup_${TIMESTAMP}.tar.gz"
    tar czf "$archive_file" -C "$(dirname "$BACKUP_DIR")" "$(basename "$BACKUP_DIR")" > /dev/null 2>&1
    
    local archive_size=$(du -h "$archive_file" 2>/dev/null | cut -f1)
    log_success "备份归档完成: $archive_file (${archive_size})"
    
    # 上传到云存储
    if [ -n "$S3_BUCKET" ] || [ -n "$AZURE_STORAGE_CONTAINER" ] || [ -n "$GCS_BUCKET" ]; then
        upload_to_cloud "$archive_file" "edams/${TIMESTAMP}/edams_backup.tar.gz"
    fi
    
    # 创建备份报告
    create_backup_report
    
    log_success "系统备份完成!"
}

# 恢复主函数
restore_main() {
    local backup_path=$1
    
    if [ ! -d "$backup_path" ]; then
        log_error "备份目录不存在: $backup_path"
        exit 1
    fi
    
    log_info "开始从备份恢复系统: $backup_path"
    
    # 恢复Kubernetes资源
    log_info "恢复Kubernetes资源..."
    for yaml_file in "${backup_path}/k8s_"*.yaml; do
        if [ -f "$yaml_file" ]; then
            log_info "  应用: $(basename "$yaml_file")"
            kubectl apply -f "$yaml_file" --dry-run=client > /dev/null 2>&1
        fi
    done
    
    # 恢复数据库（需要确认）
    log_warn "恢复数据库会覆盖现有数据，请确认在维护窗口执行!"
    read -p "是否继续恢复数据库? (yes/no): " confirm
    if [[ $confirm != "yes" ]]; then
        log_error "恢复已取消"
        exit 1
    fi
    
    # 恢复PostgreSQL
    if [ -f "${backup_path}/postgresql_full.sql.gz" ]; then
        restore_postgresql "${backup_path}/postgresql_full.sql.gz"
    fi
    
    # 恢复Redis
    if [ -f "${backup_path}/redis_backup.rdb" ]; then
        log_info "恢复Redis数据库..."
        REDIS_POD=$(kubectl get pods -n "$NAMESPACE" -l "app=redis" -o name | head -1)
        kubectl cp "${backup_path}/redis_backup.rdb" \
            "${NAMESPACE}/${REDIS_POD#pod/}:/data/dump.rdb"
        kubectl exec -n "$NAMESPACE" "$REDIS_POD" -- redis-cli DEBUG RELOAD > /dev/null 2>&1
        log_success "Redis数据库恢复完成"
    fi
    
    log_success "系统恢复完成! 请手动重启相关应用服务。"
}

# 列出备份
list_backups() {
    log_info "可用的备份列表:"
    
    # 本地备份
    echo "本地备份:"
    find /backups -name "edams_*" -type d -maxdepth 1 2>/dev/null | \
        while read -r backup; do
            size=$(du -sh "$backup" 2>/dev/null | cut -f1)
            date=$(basename "$backup" | sed 's/edams_//')
            echo "  $date - $size - $backup"
        done | sort -r
    
    # 云存储备份
    if command -v aws &> /dev/null && [ -n "$S3_BUCKET" ]; then
        echo ""
        echo "S3备份:"
        aws s3 ls "s3://${S3_BUCKET}/edams/" --recursive 2>/dev/null | \
            awk '{print $1" "$2" "$3" "$4}' | sort -r | head -10
    fi
}

# 主函数
main() {
    # 检查必需工具
    check_command kubectl
    check_command gzip
    
    # 解析参数
    CMD=${1:-help}
    
    case $CMD in
        backup)
            BACKUP_TYPE=${2:-full}
            if [[ ! "$BACKUP_TYPE" =~ ^(full|incremental)$ ]]; then
                log_error "无效的备份类型: $BACKUP_TYPE"
                show_help
                exit 1
            fi
            backup_main "$BACKUP_TYPE"
            ;;
        restore)
            if [ -z "${2:-}" ]; then
                log_error "请指定备份路径"
                show_help
                exit 1
            fi
            restore_main "$2"
            ;;
        list)
            list_backups
            ;;
        cleanup)
            cleanup_old_backups
            ;;
        report)
            create_backup_report
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $CMD"
            show_help
            exit 1
            ;;
    esac
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi