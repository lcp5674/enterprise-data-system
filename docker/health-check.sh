#!/bin/bash

# EDAMS 服务健康检查脚本
# 用于验证所有服务是否正常运行

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查服务健康
check_service() {
    local name=$1
    local url=$2
    local expected_code=${3:-200}
    
    echo -n "检查 $name... "
    
    if curl -sf --max-time 5 "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 健康${NC}"
        return 0
    else
        echo -e "${RED}✗ 失败${NC}"
        return 1
    fi
}

# 检查容器状态
check_container() {
    local container=$1
    
    if docker ps --filter "name=$container" --filter "status=running" | grep -q "$container"; then
        return 0
    else
        return 1
    fi
}

echo "========================================"
echo "      EDAMS 健康检查"
echo "========================================"
echo ""

# 检查Docker
echo "1. Docker 环境检查"
echo "----------------------------------------"
if docker info &> /dev/null; then
    echo -e "Docker: ${GREEN}✓ 运行中${NC}"
else
    echo -e "Docker: ${RED}✗ 未运行${NC}"
    exit 1
fi
echo ""

# 检查容器状态
echo "2. 容器状态检查"
echo "----------------------------------------"

containers=(
    "edams-mysql:MySQL"
    "edams-redis:Redis"
    "edams-nacos:Nacos"
    "edams-elasticsearch:Elasticsearch"
    "edams-neo4j:Neo4j"
    "edams-kafka:Kafka"
)

all_healthy=true
for item in "${containers[@]}"; do
    IFS=':' read -r container name <<< "$item"
    if check_container "$container"; then
        echo -e "  $name: ${GREEN}✓ 运行中${NC}"
    else
        echo -e "  $name: ${RED}✗ 未运行${NC}"
        all_healthy=false
    fi
done

if ! $all_healthy; then
    echo ""
    echo -e "${YELLOW}部分容器未运行，请检查日志: docker-compose logs${NC}"
fi
echo ""

# 检查API端点
echo "3. API健康检查"
echo "----------------------------------------"

endpoints=(
    "Nacos:http://localhost:8848/nacos/"
    "Sentinel:http://localhost:8858/"
    "Kafka UI:http://localhost:8090/"
    "Elasticsearch:http://localhost:9200/"
    "Kibana:http://localhost:5601/api/status"
    "Neo4j:http://localhost:7474/"
    "Prometheus:http://localhost:9090/-/healthy"
    "Grafana:http://localhost:3000/api/health"
)

for endpoint in "${endpoints[@]}"; do
    IFS=':' read -r name url <<< "$endpoint"
    check_service "$name" "$url"
done
echo ""

# 检查微服务
echo "4. 微服务健康检查"
echo "----------------------------------------"

services=(
    "Gateway:http://localhost:8080/actuator/health"
    "Auth:http://localhost:8081/actuator/health"
    "Asset:http://localhost:8082/actuator/health"
    "Permission:http://localhost:8083/actuator/health"
    "Analytics:http://localhost:8084/actuator/health"
    "Knowledge:http://localhost:8085/actuator/health"
    "LLM:http://localhost:8086/actuator/health"
    "Notification:http://localhost:8087/actuator/health"
    "Workflow:http://localhost:8088/actuator/health"
    "Lifecycle:http://localhost:8089/actuator/health"
    "AIOps:http://localhost:8090/actuator/health"
    "Chatbot:http://localhost:8091/actuator/health"
    "Intelligent-Analysis:http://localhost:8092/actuator/health"
    "User:http://localhost:8093/actuator/health"
    "Collaboration:http://localhost:8094/actuator/health"
    "Report:http://localhost:8095/actuator/health"
)

for service in "${services[@]}"; do
    IFS=':' read -r name url <<< "$service"
    check_service "$name" "$url"
done
echo ""

# 资源使用情况
echo "5. Docker 资源使用"
echo "----------------------------------------"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" | head -20

echo ""
echo "========================================"
echo "      健康检查完成"
echo "========================================"
