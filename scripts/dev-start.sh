#!/bin/bash
# =====================================================
# 企业数据资产管理系统 - 本地开发环境启动脚本
# =====================================================

set -e

echo "======================================"
echo "企业数据资产管理系统 - 本地开发环境启动"
echo "======================================"

# 切换到项目根目录
cd "$(dirname "$0")/.."

# 启动基础服务
echo "[1/4] 启动Docker Compose基础服务..."
cd docker
docker-compose up -d mysql redis nacos sentinel
echo "等待服务启动..."
sleep 30

# 等待Nacos就绪
echo "等待Nacos就绪..."
until curl -s http://localhost:8848/nacos/ > /dev/null; do
    echo "等待Nacos..."
    sleep 5
done
echo "Nacos已就绪"

# 启动中间件
echo "[2/4] 启动中间件服务..."
docker-compose up -d elasticsearch neo4j kafka mongodb

# 等待中间件就绪
echo "等待中间件服务..."
sleep 30

# 启动微服务
echo "[3/4] 启动微服务..."
cd ..

# 启动网关
echo "启动 edams-gateway..."
cd edams-parent/edams-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
cd ../..

# 启动认证服务
echo "启动 edams-auth..."
cd edams-parent/edams-auth && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
cd ../..

# 启动资产管理服务
echo "启动 edams-asset..."
cd edams-parent/edams-asset && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
cd ../..

# 启动数据分析服务
echo "启动 edams-analytics..."
cd edams-parent/edams-analytics && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
cd ../..

# 启动报表服务
echo "启动 edams-report..."
cd edams-parent/edams-report && mvn spring-boot:run -Dspring-boot.run.profiles=dev &
cd ..

echo ""
echo "======================================"
echo "本地开发环境已启动"
echo "======================================"
echo "Nacos控制台: http://localhost:8848/nacos"
echo "API网关: http://localhost:8888"
echo "Sentinel控制台: http://localhost:8858"
echo "前端开发: http://localhost:3000"
echo "======================================"
