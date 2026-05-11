#!/bin/bash

# EDAMS 一键部署脚本
# 用于在目标服务器上一键部署EDAMS系统

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step() { echo -e "${BLUE}[STEP]${NC} $1"; }

# 检查是否为root用户
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_warn "建议使用root用户运行此脚本以获得最佳体验"
    fi
}

# 检查Docker是否安装
check_docker() {
    log_step "检查Docker环境..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装"
        log_info "请运行以下命令安装Docker:"
        echo "  curl -fsSL https://get.docker.com | bash"
        exit 1
    fi
    
    log_info "Docker 已安装: $(docker --version)"
    
    if ! docker info &> /dev/null; then
        log_error "Docker 守护进程未运行"
        log_info "请运行: sudo systemctl start docker"
        exit 1
    fi
    
    log_info "Docker 守护进程运行正常"
}

# 检查Docker Compose是否安装
check_docker_compose() {
    log_step "检查Docker Compose..."
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装"
        log_info "请运行以下命令安装Docker Compose:"
        echo "  sudo curl -L https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-\$(uname -s)-\$(uname -m) -o /usr/local/bin/docker-compose"
        echo "  sudo chmod +x /usr/local/bin/docker-compose"
        exit 1
    fi
    
    if command -v docker-compose &> /dev/null; then
        log_info "Docker Compose 已安装: $(docker-compose --version)"
        DOCKER_COMPOSE="docker-compose"
    else
        log_info "Docker Compose 已安装: $(docker compose version)"
        DOCKER_COMPOSE="docker compose"
    fi
}

# 检查端口占用
check_ports() {
    log_step "检查必要端口是否被占用..."
    
    local ports=(3306 6379 8848 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091 8092 8093 8094 8095)
    local conflicts=""
    
    for port in "${ports[@]}"; do
        if lsof -i:$port &> /dev/null 2>&1; then
            conflicts="$conflicts $port"
        fi
    done
    
    if [[ -n "$conflicts" ]]; then
        log_warn "以下端口已被占用:$conflicts"
        log_info "请停止占用端口的服务或修改docker-compose.yml中的端口映射"
        read -p "是否继续部署? (y/N): " confirm
        if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
            exit 1
        fi
    else
        log_info "所有必要端口均可用"
    fi
}

# 配置环境变量
setup_env() {
    log_step "配置环境变量..."
    
    if [[ ! -f ".env" ]]; then
        log_info "创建默认环境配置文件..."
        cat > .env << 'EOF'
# EDAMS 环境配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=edams
MYSQL_USER=edams
MYSQL_PASSWORD=edams123

REDIS_PASSWORD=redis123

NACOS_MODE=standalone
NACOS_JVM_XMS=512m
NACOS_JVM_XMX=512m

NEO4J_USER=neo4j
NEO4J_PASSWORD=neo4j123

MONGO_USER=admin
MONGO_PASSWORD=mongo123

GRAFANA_PASSWORD=admin

SPRING_PROFILES=docker

JVM_XMS=512m
JVM_XMX=1024m
EOF
        log_info "环境配置文件已创建: .env"
    else
        log_info "环境配置文件已存在"
    fi
}

# 创建必要目录
setup_directories() {
    log_step "创建必要目录..."
    
    mkdir -p logs data init-scripts
    chmod -R 777 logs data
    
    log_info "目录创建完成"
}

# 拉取基础镜像
pull_base_images() {
    log_step "拉取基础镜像..."
    
    local images=(
        "mysql:8.0"
        "redis:7-alpine"
        "nacos/nacos-server:v2.2.3"
        "bladex/sentinel-dashboard:1.8.6"
        "elasticsearch:8.11.0"
        "kibana:8.11.0"
        "neo4j:5.14-community"
        "mongo:7.0"
        "bitnami/kafka:3.6"
        "prom/prometheus:v2.47.0"
        "grafana/grafana:10.2.0"
        "jaegertracing/all-in-one:1.50"
        "provectuslabs/kafka-ui:latest"
        "maven:3.9.5-eclipse-temurin-17"
        "eclipse-temurin:17-jre-alpine"
    )
    
    for image in "${images[@]}"; do
        log_info "拉取镜像: $image"
        docker pull "$image" || log_warn "镜像拉取失败: $image"
    done
}

# 构建应用镜像
build_images() {
    log_step "构建应用镜像..."
    
    log_info "构建时间较长，请耐心等待..."
    $DOCKER_COMPOSE build --no-cache
    
    log_info "镜像构建完成"
}

# 启动服务
start_services() {
    log_step "启动服务..."
    
    log_info "正在启动所有服务..."
    $DOCKER_COMPOSE up -d
    
    log_info "服务启动命令已执行"
}

# 等待服务健康
wait_for_healthy() {
    log_step "等待服务就绪..."
    
    log_info "这可能需要5-10分钟，请耐心等待..."
    
    local services=("mysql" "redis" "nacos")
    local max_attempts=120
    local attempt=1
    
    for service in "${services[@]}"; do
        log_info "等待 $service 就绪..."
        attempt=1
        while [ $attempt -le $max_attempts ]; do
            if $DOCKER_COMPOSE exec -T "$service" echo "ok" &> /dev/null; then
                log_info "$service 就绪!"
                break
            fi
            echo -ne "\r  尝试 $attempt/$max_attempts..."
            sleep 5
            attempt=$((attempt + 1))
        done
        
        if [ $attempt -gt $max_attempts ]; then
            log_warn "$service 启动超时，请检查日志"
        fi
    done
}

# 运行健康检查
health_check() {
    log_step "运行健康检查..."
    
    if [[ -x "./health-check.sh" ]]; then
        ./health-check.sh
    else
        log_info "健康检查脚本不存在，跳过..."
    fi
}

# 显示服务状态
show_status() {
    log_step "显示服务状态..."
    
    echo ""
    echo "========================================"
    echo "         EDAMS 服务状态"
    echo "========================================"
    $DOCKER_COMPOSE ps
    
    echo ""
    echo "========================================"
    echo "         服务访问地址"
    echo "========================================"
    echo "API Gateway:     http://localhost:8080"
    echo "Nacos:          http://localhost:8848/nacos"
    echo "Kibana:         http://localhost:5601"
    echo "Neo4j:          http://localhost:7474"
    echo "Kafka UI:       http://localhost:8090"
    echo "Prometheus:     http://localhost:9090"
    echo "Grafana:        http://localhost:3000"
    echo "Jaeger:         http://localhost:16686"
    echo ""
}

# 显示帮助
show_help() {
    echo "EDAMS 部署脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  deploy      完整部署 (默认)"
    echo "  build       仅构建镜像"
    echo "  start       启动服务"
    echo "  stop        停止服务"
    echo "  restart     重启服务"
    echo "  logs        查看日志"
    echo "  status      查看状态"
    echo "  health      健康检查"
    echo "  clean       清理资源"
    echo "  help        显示帮助"
    echo ""
}

# 完整部署
do_deploy() {
    log_info "========================================"
    log_info "    EDAMS 完整部署流程"
    log_info "========================================"
    
    check_root
    check_docker
    check_docker_compose
    check_ports
    setup_env
    setup_directories
    pull_base_images
    build_images
    start_services
    wait_for_healthy
    health_check
    show_status
    
    log_info "========================================"
    log_info "    部署完成!"
    log_info "========================================"
    log_info "查看日志: $0 logs"
    log_info "查看状态: $0 status"
}

# 主函数
main() {
    local command="${1:-deploy}"
    
    case "$command" in
        deploy)
            do_deploy
            ;;
        build)
            check_docker
            check_docker_compose
            build_images
            ;;
        start)
            check_docker
            $DOCKER_COMPOSE up -d
            ;;
        stop)
            $DOCKER_COMPOSE down
            ;;
        restart)
            $DOCKER_COMPOSE restart
            ;;
        logs)
            $DOCKER_COMPOSE logs -f "${2:-}"
            ;;
        status)
            $DOCKER_COMPOSE ps
            ;;
        health)
            health_check
            ;;
        clean)
            log_warn "清理所有Docker资源..."
            $DOCKER_COMPOSE down -v --rmi all
            rm -rf logs data
            log_info "清理完成"
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
