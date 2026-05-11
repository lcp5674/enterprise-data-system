#!/bin/bash

# EDAMS API 全流程测试脚本
# 用于验证系统各模块功能是否正常

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 测试配置
GATEWAY_URL="http://localhost:8080"
AUTH_URL="http://localhost:8081"
ASSET_URL="http://localhost:8082"
ANALYSIS_URL="http://localhost:8092"

# 全局变量
TOKEN=""
TEST_FAILED=0

# 日志函数
log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_test() { echo -e "${BLUE}[TEST]${NC} $1"; }

# 测试函数
test_endpoint() {
    local name=$1
    local method=${2:-GET}
    local url=$3
    local data=$4
    local expected_code=${5:-200}
    
    log_test "测试 $name ($method $url)"
    
    local response
    local http_code
    
    if [[ "$method" == "POST" ]]; then
        if [[ -n "$data" ]]; then
            response=$(curl -s -w "\n%{http_code}" -X POST "$url" \
                -H "Content-Type: application/json" \
                -d "$data" 2>&1)
        else
            response=$(curl -s -w "\n%{http_code}" -X POST "$url" \
                -H "Content-Type: application/json" 2>&1)
        fi
    else
        response=$(curl -s -w "\n%{http_code}" "$url" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -1)
    
    if [[ "$http_code" == "$expected_code" ]]; then
        log_info "✓ $name 测试通过 (HTTP $http_code)"
        return 0
    else
        log_error "✗ $name 测试失败 (HTTP $http_code, 期望 $expected_code)"
        echo "  响应: $(echo "$response" | head -1 | head -c 200)"
        TEST_FAILED=$((TEST_FAILED + 1))
        return 1
    fi
}

# 1. 网关健康检查
test_gateway_health() {
    echo ""
    log_info "========================================"
    log_info "1. API网关健康检查"
    log_info "========================================"
    
    test_endpoint "网关健康检查" "GET" "$GATEWAY_URL/actuator/health"
}

# 2. Nacos注册中心
test_nacos() {
    echo ""
    log_info "========================================"
    log_info "2. Nacos注册中心"
    log_info "========================================"
    
    test_endpoint "Nacos健康检查" "GET" "http://localhost:8848/nacos/" "" "302"
}

# 3. 认证服务测试
test_auth() {
    echo ""
    log_info "========================================"
    log_info "3. 认证服务测试"
    log_info "========================================"
    
    # 健康检查
    test_endpoint "认证服务健康检查" "GET" "$AUTH_URL/actuator/health"
    
    # 登录测试
    log_test "测试用户登录"
    response=$(curl -s -X POST "$AUTH_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin123"}')
    
    if echo "$response" | grep -q "token"; then
        TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        log_info "✓ 用户登录成功，获取Token"
    else
        log_warn "用户登录可能失败 (需要先初始化数据)"
    fi
}

# 4. 资产服务测试
test_asset() {
    echo ""
    log_info "========================================"
    log_info "4. 资产服务测试"
    log_info "========================================"
    
    test_endpoint "资产服务健康检查" "GET" "$ASSET_URL/actuator/health"
}

# 5. 智能分析服务测试
test_intelligent_analysis() {
    echo ""
    log_info "========================================"
    log_info "5. 智能分析服务测试"
    log_info "========================================"
    
    test_endpoint "分析服务健康检查" "GET" "$ANALYSIS_URL/analysis/actuator/health"
    
    # 获取模型配置列表
    log_test "获取模型配置列表"
    response=$(curl -s -X GET "$ANALYSIS_URL/analysis/api/model-configs")
    if echo "$response" | grep -q "data"; then
        log_info "✓ 模型配置接口正常"
    else
        log_warn "模型配置接口可能需要初始化"
    fi
    
    # 获取任务列表
    log_test "获取分析任务列表"
    response=$(curl -s -X GET "$ANALYSIS_URL/analysis/api/tasks")
    if echo "$response" | grep -q "data"; then
        log_info "✓ 任务管理接口正常"
    else
        log_warn "任务管理接口可能需要初始化"
    fi
}

# 6. 数据库连接测试
test_database() {
    echo ""
    log_info "========================================"
    log_info "6. 数据库连接测试"
    log_info "========================================"
    
    # 测试MySQL
    if docker exec edams-mysql mysqladmin ping -h localhost -uroot -proot123456 &>/dev/null; then
        log_info "✓ MySQL 连接正常"
    else
        log_error "✗ MySQL 连接失败"
        TEST_FAILED=$((TEST_FAILED + 1))
    fi
    
    # 测试Redis
    if docker exec edams-redis redis-cli -a redis123 ping &>/dev/null; then
        log_info "✓ Redis 连接正常"
    else
        log_error "✗ Redis 连接失败"
        TEST_FAILED=$((TEST_FAILED + 1))
    fi
}

# 7. 消息队列测试
test_kafka() {
    echo ""
    log_info "========================================"
    log_info "7. Kafka消息队列测试"
    log_info "========================================"
    
    if docker exec edams-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list &>/dev/null; then
        log_info "✓ Kafka 连接正常"
    else
        log_error "✗ Kafka 连接失败"
        TEST_FAILED=$((TEST_FAILED + 1))
    fi
}

# 8. 全文搜索引擎测试
test_elasticsearch() {
    echo ""
    log_info "========================================"
    log_info "8. Elasticsearch测试"
    log_info "========================================"
    
    response=$(curl -s http://localhost:9200/_cluster/health)
    if echo "$response" | grep -q "cluster_name"; then
        status=$(echo "$response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        log_info "✓ Elasticsearch 状态: $status"
    else
        log_error "✗ Elasticsearch 连接失败"
        TEST_FAILED=$((TEST_FAILED + 1))
    fi
}

# 9. 知识图谱测试
test_neo4j() {
    echo ""
    log_info "========================================"
    log_info "9. Neo4j知识图谱测试"
    log_info "========================================"
    
    response=$(curl -s http://localhost:7474)
    if echo "$response" | grep -q "neo4j"; then
        log_info "✓ Neo4j 连接正常"
    else
        log_error "✗ Neo4j 连接失败"
        TEST_FAILED=$((TEST_FAILED + 1))
    fi
}

# 10. 监控组件测试
test_monitoring() {
    echo ""
    log_info "========================================"
    log_info "10. 监控组件测试"
    log_info "========================================"
    
    test_endpoint "Prometheus" "GET" "http://localhost:9090/-/healthy"
    test_endpoint "Grafana" "GET" "http://localhost:3000/api/health"
    test_endpoint "Jaeger" "GET" "http://localhost:16686/"
}

# 生成测试报告
generate_report() {
    echo ""
    log_info "========================================"
    log_info "测试报告"
    log_info "========================================"
    
    if [[ $TEST_FAILED -eq 0 ]]; then
        echo -e "${GREEN}所有测试通过!${NC}"
        return 0
    else
        echo -e "${RED}有 $TEST_FAILED 项测试失败${NC}"
        return 1
    fi
}

# 显示帮助
show_help() {
    echo "EDAMS API 全流程测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  all         运行所有测试 (默认)"
    echo "  gateway     仅测试网关"
    echo "  auth        仅测试认证服务"
    echo "  analysis    仅测试智能分析服务"
    echo "  db          仅测试数据库"
    echo "  help        显示帮助"
    echo ""
}

# 主函数
main() {
    local test="${1:-all}"
    
    echo "========================================"
    log_info "EDAMS 全流程API测试"
    echo "========================================"
    
    case "$test" in
        all)
            test_gateway_health
            test_nacos
            test_auth
            test_asset
            test_intelligent_analysis
            test_database
            test_kafka
            test_elasticsearch
            test_neo4j
            test_monitoring
            ;;
        gateway)
            test_gateway_health
            ;;
        auth)
            test_auth
            ;;
        analysis)
            test_intelligent_analysis
            ;;
        db)
            test_database
            ;;
        help|--help|-h)
            show_help
            exit 0
            ;;
        *)
            log_error "未知测试: $test"
            show_help
            exit 1
            ;;
    esac
    
    generate_report
}

main "$@"
