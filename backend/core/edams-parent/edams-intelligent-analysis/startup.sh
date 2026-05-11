#!/bin/bash

# EDAMS Intelligent Analysis Docker Startup Script
# 启动脚本，用于启动智能分析服务及其依赖

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR" && pwd)"

echo "========================================"
echo "EDAMS Intelligent Analysis - Startup"
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

# 检查Docker是否运行
check_docker() {
    if ! docker info &> /dev/null; then
        log_error "Docker daemon is not running. Please start Docker first."
        exit 1
    fi
}

# 启动服务
start_services() {
    log_info "Starting services..."

    cd "$PROJECT_DIR"

    # 创建必要的目录
    mkdir -p logs data

    # 使用docker compose启动
    if command -v docker-compose &> /dev/null; then
        docker-compose up -d
    else
        docker compose up -d
    fi
}

# 等待服务健康
wait_for_healthy() {
    log_info "Waiting for services to be healthy..."

    local max_attempts=60
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -sf http://localhost:8088/analysis/actuator/health &> /dev/null; then
            log_info "Service is healthy!"
            return 0
        fi
        echo -ne "\r  Attempt $attempt/$max_attempts..."
        sleep 2
        attempt=$((attempt + 1))
    done

    log_error "Service failed to become healthy after $max_attempts attempts"
    return 1
}

# 显示服务状态
show_status() {
    echo ""
    echo "========================================"
    echo "Services Status"
    echo "========================================"

    cd "$PROJECT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose ps
    else
        docker compose ps
    fi

    echo ""
    echo "Service URLs:"
    echo -e "  ${CYAN}Intelligent Analysis API:${NC} http://localhost:8088/analysis"
    echo -e "  ${CYAN}Health Check:${NC} http://localhost:8088/analysis/actuator/health"
    echo ""
}

# 查看日志
show_logs() {
    echo ""
    echo "Recent logs:"
    echo "========================================"

    cd "$PROJECT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose logs --tail=50 intelligent-analysis
    else
        docker compose logs --tail=50 intelligent-analysis
    fi
}

# 停止服务
stop_services() {
    log_info "Stopping services..."

    cd "$PROJECT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose down
    else
        docker compose down
    fi

    log_info "Services stopped."
}

# 显示帮助
show_help() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  start     Start all services (default)"
    echo "  stop      Stop all services"
    echo "  restart   Restart all services"
    echo "  logs      Show service logs"
    echo "  status    Show service status"
    echo "  rebuild   Rebuild and start services"
    echo "  help      Show this help message"
    echo ""
}

# 主函数
main() {
    local command="${1:-start}"

    case "$command" in
        start)
            check_docker
            start_services
            wait_for_healthy
            show_status
            ;;
        stop)
            stop_services
            ;;
        restart)
            stop_services
            sleep 2
            start_services
            wait_for_healthy
            show_status
            ;;
        logs)
            show_logs
            ;;
        status)
            show_status
            ;;
        rebuild)
            check_docker
            cd "$PROJECT_DIR"
            if command -v docker-compose &> /dev/null; then
                docker-compose build --no-cache intelligent-analysis
                docker-compose up -d
            else
                docker compose build --no-cache intelligent-analysis
                docker compose up -d
            fi
            wait_for_healthy
            show_status
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
