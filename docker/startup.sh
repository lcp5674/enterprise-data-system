#!/bin/bash

# EDAMS 统一启动脚本
# 管理所有微服务的启动、停止、重启

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "========================================"
echo "EDAMS - Docker 服务管理"
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
        log_error "Docker 守护进程未运行，请先启动 Docker"
        exit 1
    fi
    log_info "Docker 运行中"
}

# 启动服务
start_services() {
    log_info "启动所有服务..."

    cd "$SCRIPT_DIR"

    # 创建必要目录
    mkdir -p logs data

    # 使用docker compose启动
    if command -v docker-compose &> /dev/null; then
        docker-compose up -d
    else
        docker compose up -d
    fi

    log_info "服务启动命令已执行，等待服务健康检查..."
    wait_for_healthy
}

# 等待服务健康
wait_for_healthy() {
    log_info "等待服务就绪..."

    local services=("mysql" "redis")
    local max_attempts=60
    local attempt=1

    for service in "${services[@]}"; do
        log_info "等待 $service 就绪..."
        attempt=1
        while [ $attempt -le $max_attempts ]; do
            if check_service_healthy "$service"; then
                log_info "$service 就绪!"
                break
            fi
            echo -ne "\r  尝试 $attempt/$max_attempts..."
            sleep 2
            attempt=$((attempt + 1))
        done
    done
}

# 检查单个服务健康状态
check_service_healthy() {
    local service=$1
    case "$service" in
        mysql)
            docker exec edams-mysql mysqladmin ping -h localhost -uroot -proot123456 &> /dev/null
            ;;
        redis)
            docker exec edams-redis redis-cli ping &> /dev/null
            ;;
        *)
            return 0
            ;;
    esac
}

# 显示服务状态
show_status() {
    echo ""
    echo "========================================"
    echo "服务状态"
    echo "========================================"

    cd "$SCRIPT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose ps
    else
        docker compose ps
    fi

    echo ""
    echo "服务访问地址:"
    echo -e "  ${CYAN}API Gateway:${NC}          http://localhost:8080"
    echo -e "  ${CYAN}认证服务:${NC}             http://localhost:8081"
    echo -e "  ${CYAN}资产服务:${NC}             http://localhost:8082"
    echo -e "  ${CYAN}权限服务:${NC}             http://localhost:8083"
    echo -e "  ${CYAN}分析服务:${NC}             http://localhost:8084"
    echo -e "  ${CYAN}知识图谱:${NC}             http://localhost:8085"
    echo -e "  ${CYAN}LLM服务:${NC}              http://localhost:8086"
    echo -e "  ${CYAN}通知服务:${NC}             http://localhost:8087"
    echo -e "  ${CYAN}工作流服务:${NC}           http://localhost:8088"
    echo -e "  ${CYAN}生命周期服务:${NC}         http://localhost:8089"
    echo -e "  ${CYAN}AI运维服务:${NC}           http://localhost:8090"
    echo -e "  ${CYAN}智能助手:${NC}             http://localhost:8091"
    echo -e "  ${CYAN}智能分析:${NC}             http://localhost:8092"
    echo -e "  ${CYAN}用户服务:${NC}             http://localhost:8093"
    echo -e "  ${CYAN}协作服务:${NC}             http://localhost:8094"
    echo ""
    echo "中间件管理界面:"
    echo -e "  ${CYAN}Nacos:${NC}                http://localhost:8848/nacos"
    echo -e "  ${CYAN}Sentinel:${NC}             http://localhost:8858"
    echo -e "  ${CYAN}Kibana:${NC}               http://localhost:5601"
    echo -e "  ${CYAN}Neo4j Browser:${NC}        http://localhost:7474"
    echo -e "  ${CYAN}Kafka UI:${NC}             http://localhost:8090"
    echo -e "  ${CYAN}Prometheus:${NC}           http://localhost:9090"
    echo -e "  ${CYAN}Grafana:${NC}              http://localhost:3000"
    echo -e "  ${CYAN}Jaeger:${NC}               http://localhost:16686"
    echo ""
}

# 停止服务
stop_services() {
    log_info "停止所有服务..."

    cd "$SCRIPT_DIR"

    if command -v docker-compose &> /dev/null; then
        docker-compose down
    else
        docker compose down
    fi

    log_info "所有服务已停止"
}

# 重启服务
restart_services() {
    log_info "重启所有服务..."
    stop_services
    sleep 2
    start_services
    show_status
}

# 查看日志
show_logs() {
    local service="${1:-}"

    cd "$SCRIPT_DIR"

    if [ -n "$service" ]; then
        log_info "查看 $service 服务日志..."
        if command -v docker-compose &> /dev/null; then
            docker-compose logs -f --tail=100 "$service"
        else
            docker compose logs -f --tail=100 "$service"
        fi
    else
        log_info "查看所有服务日志..."
        if command -v docker-compose &> /dev/null; then
            docker-compose logs -f --tail=50
        else
            docker compose logs -f --tail=50
        fi
    fi
}

# 显示帮助
show_help() {
    echo "用法: $0 [命令] [参数]"
    echo ""
    echo "命令:"
    echo "  start         启动所有服务 (默认)"
    echo "  stop          停止所有服务"
    echo "  restart       重启所有服务"
    echo "  status        查看服务状态"
    echo "  logs [服务名] 查看服务日志"
    echo "  rebuild       重新构建并启动"
    echo "  help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start                    # 启动所有服务"
    echo "  $0 stop                     # 停止所有服务"
    echo "  $0 logs gateway             # 查看gateway服务日志"
    echo "  $0 rebuild                  # 重新构建并启动"
    echo ""
}

# 主函数
main() {
    local command="${1:-start}"
    shift || true

    case "$command" in
        start)
            check_docker
            start_services
            show_status
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$@"
            ;;
        rebuild)
            check_docker
            cd "$SCRIPT_DIR"
            if command -v docker-compose &> /dev/null; then
                docker-compose build --no-cache
                docker-compose up -d
            else
                docker compose build --no-cache
                docker compose up -d
            fi
            wait_for_healthy
            show_status
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
