#!/bin/bash

# EDAMS Intelligent Analysis Docker Build Script
# 构建脚本，用于构建智能分析服务的Docker镜像

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR" && pwd)"

echo "========================================"
echo "EDAMS Intelligent Analysis - Docker Build"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    log_info "Docker is installed: $(docker --version)"
}

# 检查Docker Compose是否安装
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    if command -v docker-compose &> /dev/null; then
        log_info "Docker Compose is installed: $(docker-compose --version)"
    else
        log_info "Docker Compose is installed (as docker compose plugin): $(docker compose version)"
    fi
}

# 构建镜像
build_image() {
    log_info "Building Docker image..."

    cd "$PROJECT_DIR"

    # 使用docker compose构建
    if command -v docker-compose &> /dev/null; then
        docker-compose build --no-cache intelligent-analysis
    else
        docker compose build --no-cache intelligent-analysis
    fi

    log_info "Docker image built successfully!"
}

# 拉取基础镜像
pull_base_images() {
    log_info "Pulling base images..."
    docker pull maven:3.9.5-eclipse-temurin-17
    docker pull eclipse-temurin:17-jre-alpine
}

# 主函数
main() {
    log_info "Starting build process..."

    check_docker
    check_docker_compose
    pull_base_images
    build_image

    log_info "========================================"
    log_info "Build completed successfully!"
    log_info "========================================"
    log_info ""
    log_info "To start the service, run:"
    log_info "  ./startup.sh"
    log_info ""
    log_info "Or use docker-compose directly:"
    log_info "  docker-compose up -d"
}

main "$@"
