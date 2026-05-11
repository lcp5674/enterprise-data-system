#!/bin/bash

# EDAMS 统一构建脚本
# 构建所有微服务的Docker镜像

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "========================================"
echo "EDAMS - Docker 镜像构建"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    log_info "Docker 版本: $(docker --version)"
}

# 检查Docker Compose是否安装
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    if command -v docker-compose &> /dev/null; then
        log_info "Docker Compose 版本: $(docker-compose --version)"
    else
        log_info "Docker Compose 版本: $(docker compose version)"
    fi
}

# 构建镜像
build_images() {
    log_info "开始构建 Docker 镜像..."

    cd "$SCRIPT_DIR"

    # 检查是否使用新式docker compose命令
    if command -v docker-compose &> /dev/null; then
        docker-compose build --no-cache "$@"
    else
        docker compose build --no-cache "$@"
    fi

    log_info "Docker 镜像构建完成!"
}

# 拉取基础镜像
pull_base_images() {
    log_info "拉取基础镜像..."
    docker pull maven:3.9.5-eclipse-temurin-17
    docker pull eclipse-temurin:17-jre-alpine
    docker pull mysql:8.0
    docker pull redis:7-alpine
    docker pull nacos/nacos-server:v2.2.3
    docker pull elasticsearch:8.11.0
    docker pull kibana:8.11.0
    docker pull neo4j:5.14-community
    docker pull mongo:7.0
    docker pull bitnami/kafka:3.6
    docker pull prom/prometheus:v2.47.0
    docker pull grafana/grafana:10.2.0
    docker pull jaegertracing/all-in-one:1.50
    docker pull bladex/sentinel-dashboard:1.8.6
    docker pull provectuslabs/kafka-ui:latest
}

# 显示帮助
show_help() {
    echo "用法: $0 [命令] [参数]"
    echo ""
    echo "命令:"
    echo "  build         构建所有微服务镜像 (默认)"
    echo "  pull          拉取基础镜像"
    echo "  rebuild       重新构建指定服务"
    echo "  help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 build                    # 构建所有镜像"
    echo "  $0 build gateway auth       # 只构建gateway和auth服务"
    echo "  $0 pull                     # 拉取所有基础镜像"
    echo "  $0 rebuild gateway          # 重新构建gateway服务"
    echo ""
}

# 主函数
main() {
    local command="${1:-build}"
    shift || true

    case "$command" in
        build)
            check_docker
            check_docker_compose
            build_images "$@"
            ;;
        pull)
            check_docker
            pull_base_images
            ;;
        rebuild)
            check_docker
            check_docker_compose
            build_images --no-cache "$@"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
