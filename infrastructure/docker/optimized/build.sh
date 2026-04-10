#!/bin/bash
# EDAMS Docker镜像构建脚本
# 企业数据资产管理系统 - 生产环境镜像构建

set -euo pipefail

# =====================================================
# 配置参数
# =====================================================

# 默认值
REGISTRY=${REGISTRY:-"registry.enterprise.com"}
NAMESPACE=${NAMESPACE:-"edams"}
VERSION=${VERSION:-"1.0.0"}
BUILD_NUMBER=${BUILD_NUMBER:-$(date +%Y%m%d%H%M%S)}
IMAGE_TAG="${VERSION}-${BUILD_NUMBER}"
LATEST_TAG="latest"

# 服务列表
SERVICES=(
    "gateway"
    "auth"
    "asset"
    "lineage"
    "quality"
    "security"
    "catalog"
    "search"
    "knowledge"
    "insight"
)

# 构建参数
MAVEN_PROFILE=${MAVEN_PROFILE:-"prod"}
USE_CHINA_MIRROR=${USE_CHINA_MIRROR:-"true"}
SKIP_TESTS=${SKIP_TESTS:-"true"}

# Docker配置
DOCKER_BUILDKIT=${DOCKER_BUILDKIT:-1}
export DOCKER_BUILDKIT

# =====================================================
# 彩色输出函数
# =====================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =====================================================
# 辅助函数
# =====================================================

check_requirements() {
    log_info "检查系统依赖..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装"
        exit 1
    fi
    
    # 检查Docker守护进程
    if ! docker info &> /dev/null; then
        log_error "Docker守护进程未运行"
        exit 1
    fi
    
    # 检查Docker Buildx (可选)
    if docker buildx version &> /dev/null; then
        log_info "Docker Buildx可用"
        USE_BUILDX=true
    else
        log_warning "Docker Buildx不可用，使用标准构建"
        USE_BUILDX=false
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_warning "Maven未安装，将使用Docker内部构建"
    fi
    
    log_success "依赖检查通过"
}

create_directories() {
    log_info "创建日志目录..."
    mkdir -p logs
    mkdir -p artifacts
}

print_configuration() {
    log_info "构建配置:"
    echo "  Registry:      ${REGISTRY}"
    echo "  Namespace:     ${NAMESPACE}"
    echo "  Version:       ${VERSION}"
    echo "  Build Number:  ${BUILD_NUMBER}"
    echo "  Image Tag:     ${IMAGE_TAG}"
    echo "  Latest Tag:    ${LATEST_TAG}"
    echo "  Maven Profile: ${MAVEN_PROFILE}"
    echo "  Skip Tests:    ${SKIP_TESTS}"
    echo "  China Mirror:  ${USE_CHINA_MIRROR}"
    echo "  Docker BuildKit: ${DOCKER_BUILDKIT}"
    echo ""
    log_info "服务列表:"
    for service in "${SERVICES[@]}"; do
        echo "  - ${service}"
    done
}

# =====================================================
# Docker登录
# =====================================================

docker_login() {
    log_info "Docker登录检查..."
    
    if [[ -n "${DOCKER_USERNAME:-}" && -n "${DOCKER_PASSWORD:-}" ]]; then
        log_info "使用环境变量登录到 ${REGISTRY}"
        echo "${DOCKER_PASSWORD}" | docker login "${REGISTRY}" \
            --username "${DOCKER_USERNAME}" \
            --password-stdin
    elif [[ -f "/usr/local/etc/docker_credentials" ]]; then
        log_info "使用配置文件登录"
        source "/usr/local/etc/docker_credentials"
        echo "${DOCKER_PASSWORD}" | docker login "${REGISTRY}" \
            --username "${DOCKER_USERNAME}" \
            --password-stdin
    else
        log_warning "未找到Docker登录凭据，将只进行本地构建"
        CAN_PUSH=false
    fi
}

# =====================================================
# 镜像构建
# =====================================================

build_service() {
    local service="$1"
    local start_time=$(date +%s)
    
    log_info "构建服务: ${service}"
    
    # 构建参数
    local build_args="--build-arg BUILD_PROFILE=${MAVEN_PROFILE}"
    build_args="${build_args} --build-arg USE_CHINA_MIRROR=${USE_CHINA_MIRROR}"
    build_args="${build_args} --build-arg SKIP_TESTS=${SKIP_TESTS}"
    build_args="${build_args} --build-arg SERVICE_NAME=${service}"
    
    # 构建镜像
    if [[ "${USE_BUILDX}" == "true" ]]; then
        docker buildx build \
            --target service \
            --platform linux/amd64,linux/arm64 \
            ${build_args} \
            -t "${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}" \
            -t "${REGISTRY}/${NAMESPACE}/edams-${service}:${LATEST_TAG}" \
            -f Dockerfile \
            ../.. \
            --push
    else
        docker build \
            --target service \
            ${build_args} \
            -t "${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}" \
            -t "${REGISTRY}/${NAMESPACE}/edams-${service}:${LATEST_TAG}" \
            -f Dockerfile \
            ../.. \
            2>&1 | tee "logs/build-${service}-${BUILD_NUMBER}.log"
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    if [[ ${PIPESTATUS[0]} -eq 0 ]]; then
        log_success "服务 ${service} 构建完成 (耗时: ${duration}s)"
        echo "${service}:${IMAGE_TAG}:SUCCESS:${duration}" >> "logs/build-summary-${BUILD_NUMBER}.txt"
    else
        log_error "服务 ${service} 构建失败"
        echo "${service}:${IMAGE_TAG}:FAILED:${duration}" >> "logs/build-summary-${BUILD_NUMBER}.txt"
        return 1
    fi
}

build_all_services() {
    log_info "开始构建所有服务..."
    
    rm -f "logs/build-summary-${BUILD_NUMBER}.txt"
    local failed_services=()
    
    for service in "${SERVICES[@]}"; do
        if ! build_service "${service}"; then
            failed_services+=("${service}")
        fi
    done
    
    if [[ ${#failed_services[@]} -gt 0 ]]; then
        log_error "以下服务构建失败: ${failed_services[*]}"
        return 1
    else
        log_success "所有服务构建成功"
        return 0
    fi
}

# =====================================================
# 镜像推送
# =====================================================

push_service() {
    local service="$1"
    
    log_info "推送服务: ${service}"
    
    docker push "${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}"
    docker push "${REGISTRY}/${NAMESPACE}/edams-${service}:${LATEST_TAG}"
    
    log_success "服务 ${service} 推送完成"
}

push_all_services() {
    log_info "开始推送所有服务..."
    
    for service in "${SERVICES[@]}"; do
        push_service "${service}"
    done
    
    log_success "所有服务推送完成"
}

# =====================================================
# 安全扫描
# =====================================================

security_scan() {
    log_info "执行安全扫描..."
    
    # 检查trivy
    if ! command -v trivy &> /dev/null; then
        log_warning "Trivy未安装，跳过安全扫描"
        log_warning "安装命令: brew install trivy 或参考 https://aquasecurity.github.io/trivy/"
        return 0
    fi
    
    local vuln_found=false
    
    for service in "${SERVICES[@]}"; do
        log_info "扫描服务: ${service}"
        
        trivy image \
            --severity HIGH,CRITICAL \
            --exit-code 0 \
            --format table \
            "${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}" \
            2>&1 | tee "logs/scan-${service}-${BUILD_NUMBER}.log"
            
        if trivy image \
            --severity HIGH,CRITICAL \
            --exit-code 1 \
            --quiet \
            "${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}" \
            > /dev/null 2>&1; then
            log_warning "服务 ${service} 发现高危漏洞"
            vuln_found=true
        fi
    done
    
    if [[ "${vuln_found}" == "true" ]]; then
        log_warning "发现高危漏洞，请检查扫描报告"
    else
        log_success "安全扫描完成，未发现高危漏洞"
    fi
}

# =====================================================
# 镜像大小分析
# =====================================================

analyze_image_size() {
    log_info "分析镜像大小..."
    
    echo "服务,镜像标签,大小" > "artifacts/image-sizes-${BUILD_NUMBER}.csv"
    
    for service in "${SERVICES[@]}"; do
        local size=$(docker images \
            --format "{{.Repository}}:{{.Tag}} {{.Size}}" \
            "${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}" 2>/dev/null | \
            grep "${IMAGE_TAG}" | \
            awk '{print $2}' || echo "N/A")
        
        echo "${service},${IMAGE_TAG},${size}" >> "artifacts/image-sizes-${BUILD_NUMBER}.csv"
        
        log_info "服务 ${service}: ${size}"
    done
    
    log_success "镜像大小分析完成，结果保存到 artifacts/image-sizes-${BUILD_NUMBER}.csv"
}

# =====================================================
# 生成构建报告
# =====================================================

generate_build_report() {
    log_info "生成构建报告..."
    
    local report_file="artifacts/build-report-${BUILD_NUMBER}.md"
    
    cat > "${report_file}" << EOF
# EDAMS 构建报告

## 构建信息
- **构建时间**: $(date)
- **构建编号**: ${BUILD_NUMBER}
- **镜像标签**: ${IMAGE_TAG}
- **最新标签**: ${LATEST_TAG}
- **Maven Profile**: ${MAVEN_PROFILE}

## 服务镜像
EOF
    
    for service in "${SERVICES[@]}"; do
        echo "- \`${REGISTRY}/${NAMESPACE}/edams-${service}:${IMAGE_TAG}\`" >> "${report_file}"
    done
    
    cat >> "${report_file}" << EOF

## 构建摘要
\`\`\`
$(cat "logs/build-summary-${BUILD_NUMBER}.txt" 2>/dev/null || echo "无构建摘要")
\`\`\`

## 镜像大小
| 服务 | 大小 |
|------|------|
EOF
    
    while IFS=, read -r service tag size; do
        if [[ "${service}" != "服务" ]]; then
            echo "| ${service} | ${size} |" >> "${report_file}"
        fi
    done < "artifacts/image-sizes-${BUILD_NUMBER}.csv"
    
    cat >> "${report_file}" << EOF

## 安全扫描
安全扫描日志:
- \`logs/scan-*-${BUILD_NUMBER}.log\`

## 构建日志
详细构建日志:
- \`logs/build-*-${BUILD_NUMBER}.log\`

## 后续步骤
1. 验证镜像: \`docker run --rm ${REGISTRY}/${NAMESPACE}/edams-gateway:${IMAGE_TAG} --version\`
2. 部署到Kubernetes
3. 更新部署配置中的镜像标签

---
*本报告由EDAMS构建脚本自动生成*
EOF
    
    log_success "构建报告已生成: ${report_file}"
}

# =====================================================
# 主函数
# =====================================================

main() {
    local action=${1:-"all"}
    
    check_requirements
    create_directories
    print_configuration
    docker_login
    
    case "${action}" in
        "build")
            build_all_services
            ;;
        "push")
            build_all_services
            push_all_services
            ;;
        "scan")
            security_scan
            ;;
        "report")
            analyze_image_size
            generate_build_report
            ;;
        "all")
            build_all_services
            security_scan
            analyze_image_size
            generate_build_report
            if [[ "${CAN_PUSH:-true}" == "true" ]]; then
                push_all_services
            fi
            ;;
        "help"|"--help"|"-h")
            echo "用法: $0 [action]"
            echo ""
            echo "可用操作:"
            echo "  build    构建所有服务镜像"
            echo "  push     构建并推送所有服务镜像"
            echo "  scan     安全扫描所有镜像"
            echo "  report   生成构建报告"
            echo "  all      执行完整流程 (默认)"
            echo ""
            echo "环境变量:"
            echo "  REGISTRY        镜像仓库地址 (默认: registry.enterprise.com)"
            echo "  NAMESPACE       命名空间 (默认: edams)"
            echo "  VERSION         版本号 (默认: 1.0.0)"
            echo "  BUILD_NUMBER    构建编号 (默认: 时间戳)"
            echo "  MAVEN_PROFILE   Maven配置文件 (默认: prod)"
            echo "  SKIP_TESTS      跳过测试 (默认: true)"
            echo "  USE_CHINA_MIRROR 使用国内镜像加速 (默认: true)"
            echo "  DOCKER_USERNAME Docker登录用户名"
            echo "  DOCKER_PASSWORD Docker登录密码"
            exit 0
            ;;
        *)
            log_error "未知操作: ${action}"
            echo "使用: $0 help 查看帮助"
            exit 1
            ;;
    esac
    
    log_success "构建流程完成"
}

# =====================================================
# 脚本入口
# =====================================================

# 如果脚本被直接执行而不是被source
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi