# =====================================================
# EDAMS - Makefile for Development & Deployment
# 企业数据资产管理系统 Makefile
# =====================================================

.PHONY: help build build-all test clean docker-build docker-run docker-stop \
        deploy deploy-test deploy-staging deploy-prod init clean-all

# 默认目标
.DEFAULT_GOAL := help

# 颜色定义
BOLD := $(shell tput bold)
GREEN := $(shell tput setaf 2)
YELLOW := $(shell tput setaf 3)
BLUE := $(shell tput setaf 4)
RESET := $(shell tput sgr0)

# 打印函数
info = $(info $(BOLD)$(BLUE)[INFO]$(RESET) $(1))
success = $(info $(GREEN)$(1)$(RESET))
warning = $(info $(YELLOW)$(1)$(RESET))
error = $(info $(RED)$(1)$(RESET))

# Docker配置
DOCKER_REGISTRY := registry.example.com
DOCKER_IMAGE_TAG := latest

# Maven配置
MAVEN_OPTS := -Xmx1024m -XX:+UseG1GC
BUILD_PROFILE := dev

# 帮助信息
help:
	@echo "$(BOLD)$(BLUE)EDAMS - 企业数据资产管理系统$(RESET)"
	@echo ""
	@echo "$(BOLD)使用方法:$(RESET)"
	@echo "  make $(GREEN)<target>$(RESET)"
	@echo ""
	@echo "$(BOLD)开发命令:$(RESET)"
	@echo "  $(GREEN)init$(RESET)              - 初始化项目（安装依赖）"
	@echo "  $(GREEN)build$(RESET)              - 构建所有模块"
	@echo "  $(GREEN)build-all$(RESET)          - 完整构建（包含测试）"
	@echo "  $(GREEN)test$(RESET)               - 运行所有测试"
	@echo ""
	@echo "$(BOLD)Docker命令:$(RESET)"
	@echo "  $(GREEN)docker-build$(RESET)        - 构建所有Docker镜像"
	@echo "  $(GREEN)docker-run$(RESET)          - 启动Docker Compose环境"
	@echo "  $(GREEN)docker-stop$(RESET)         - 停止Docker Compose环境"
	@echo "  $(GREEN)docker-clean$(RESET)        - 清理Docker资源"
	@echo ""
	@echo "$(BOLD)部署命令:$(RESET)"
	@echo "  $(GREEN)deploy$(RESET)              - 部署到当前环境"
	@echo "  $(GREEN)deploy-test$(RESET)         - 部署到测试环境"
	@echo "  $(GREEN)deploy-staging$(RESET)      - 部署到预发布环境"
	@echo "  $(GREEN)deploy-prod$(RESET)         - 部署到生产环境"
	@echo ""
	@echo "$(BOLD)清理命令:$(RESET)"
	@echo "  $(GREEN)clean$(RESET)               - 清理构建产物"
	@echo "  $(GREEN)clean-all$(RESET)           - 清理所有（包括Docker）"
	@echo ""
	@echo "$(BOLD)其他命令:$(RESET)"
	@echo "  $(GREEN)lint$(RESET)               - 代码检查"
	@echo "  $(GREEN)security-scan$(RESET)       - 安全扫描"

# =====================================================
# 开发命令
# =====================================================

# 初始化项目
init:
	@echo "$(BOLD)=== 初始化项目 ===$(RESET)"
	@$(call success, "安装后端依赖...")
	cd microservices/edams-parent && mvn dependency:go-offline -B || true
	@$(call success, "安装services依赖...")
	cd services && mvn dependency:go-offline -B || true
	@$(call success, "安装前端依赖...")
	cd edams-web && npm install
	@$(call success, "初始化完成!")

# 构建所有模块
build:
	@echo "$(BOLD)=== 构建项目 ===$(RESET)"
	@$(call info, "构建edams-parent...")
	cd microservices/edams-parent && mvn clean package -DskipTests -B -P${BUILD_PROFILE}
	@$(call info, "构建services...")
	cd services && mvn clean package -DskipTests -B -P${BUILD_PROFILE}
	@$(call success, "后端构建完成!")

# 完整构建（包含测试）
build-all:
	@echo "$(BOLD)=== 完整构建 ===$(RESET)"
	@$(call info, "构建edams-parent...")
	cd microservices/edams-parent && mvn clean package -B -P${BUILD_PROFILE}
	@$(call info, "构建services...")
	cd services && mvn clean package -B -P${BUILD_PROFILE}
	@$(call info, "构建前端...")
	cd edams-web && npm install && npm run build
	@$(call success, "完整构建完成!")

# 运行测试
test:
	@echo "$(BOLD)=== 运行测试 ===$(RESET)"
	@$(call info, "运行后端测试...")
	cd microservices/edams-parent && mvn test -B
	cd services && mvn test -B
	@$(call info, "运行前端测试...")
	cd edams-web && npm test -- --coverage --passWithNoTests || true
	@$(call success, "测试完成!")

# 代码检查
lint:
	@echo "$(BOLD)=== 代码检查 ===$(RESET)"
	@$(call info, "后端代码检查...")
	cd microservices/edams-parent && mvn checkstyle:check -B || true
	cd services && mvn checkstyle:check -B || true
	@$(call info, "前端代码检查...")
	cd edams-web && npm run lint || true
	@$(call success, "代码检查完成!")

# 安全扫描
security-scan:
	@echo "$(BOLD)=== 安全扫描 ===$(RESET)"
	@$(call info, "运行OWASP依赖检查...")
	cd microservices/edams-parent && mvn org.owasp:dependency-check-maven:check -B || true
	cd services && mvn org.owasp:dependency-check-maven:check -B || true
	@$(call success, "安全扫描完成!")

# =====================================================
# Docker命令
# =====================================================

# 构建所有Docker镜像
docker-build:
	@echo "$(BOLD)=== 构建Docker镜像 ===$(RESET)"
	@$(call info, "构建后端服务镜像...")
	@for service in gateway auth user permission notification knowledge llm chatbot workflow lifecycle version collaboration aiops asset edge-iot-service value-service sla-service incentive-service watermark-service sandbox-service; do \
		echo "Building edams-$$service..."; \
		docker build -t ${DOCKER_REGISTRY}/edams-$$service:${DOCKER_IMAGE_TAG} \
			-f docker/Dockerfile.$$service microservices/edams-parent/ || true; \
	done
	@$(call info, "构建services镜像...")
	@for service in lineage-service quality-service standard-service governance-engine portal-service admin-service metadata-service index-service; do \
		echo "Building $$service..."; \
		docker build -t ${DOCKER_REGISTRY}/$$service:${DOCKER_IMAGE_TAG} \
			-f services/Dockerfile.$$service services/ || true; \
	done
	@$(call info, "构建前端镜像...")
	docker build -t ${DOCKER_REGISTRY}/edams-frontend:${DOCKER_IMAGE_TAG} -f edams-web/Dockerfile.frontend edams-web/
	@$(call success, "Docker镜像构建完成!")

# 启动Docker Compose环境
docker-run:
	@echo "$(BOLD)=== 启动Docker环境 ===$(RESET)"
	cd docker && docker-compose -f docker-compose.yml up -d
	@$(call success, "Docker环境启动完成!")
	@$(call info, "查看服务状态:")
	@cd docker && docker-compose ps

# 停止Docker Compose环境
docker-stop:
	@echo "$(BOLD)=== 停止Docker环境 ===$(RESET)"
	cd docker && docker-compose -f docker-compose.yml down
	@$(call success, "Docker环境已停止!")

# 清理Docker资源
docker-clean:
	@echo "$(BOLD)=== 清理Docker资源 ===$(RESET)"
	@$(call warning, "清理未使用的镜像...")
	docker image prune -af
	@$(call warning, "清理未使用的卷...")
	docker volume prune -f
	@$(call warning, "清理未使用的网络...")
	docker network prune -f
	@$(call success, "Docker清理完成!")

# =====================================================
# 部署命令
# =====================================================

# 部署到当前环境
deploy:
	@echo "$(BOLD)=== 部署到当前环境 ===$(RESET)"
	@$(call info, "部署基础设施...")
	cd docker && docker-compose -f docker-compose.yml up -d
	@$(call info, "部署微服务...")
	cd docker && docker-compose -f docker-compose.dev.yml up -d
	@$(call success, "部署完成!")

# 部署到测试环境
deploy-test:
	@echo "$(BOLD)=== 部署到测试环境 ===$(RESET)"
	@$(call info, "使用Helm部署到测试环境...")
	helm upgrade --install edams ./charts/edams \
		--namespace edams-test \
		--create-namespace \
		--values ./charts/edams/values-test.yaml \
		--set image.tag=${DOCKER_IMAGE_TAG} \
		--wait --timeout 10m
	@$(call success, "测试环境部署完成!")

# 部署到预发布环境
deploy-staging:
	@echo "$(BOLD)=== 部署到预发布环境 ===$(RESET)"
	@$(call info, "使用Helm部署到预发布环境...")
	helm upgrade --install edams ./charts/edams \
		--namespace edams-staging \
		--create-namespace \
		--values ./charts/edams/values-staging.yaml \
		--set image.tag=${DOCKER_IMAGE_TAG} \
		--wait --timeout 15m
	@$(call success, "预发布环境部署完成!")

# 部署到生产环境
deploy-prod:
	@echo "$(BOLD)=== 部署到生产环境 ===$(RESET)"
	@$(call info, "使用Helm部署到生产环境...")
	helm upgrade --install edams ./charts/edams \
		--namespace edams-prod \
		--create-namespace \
		--values ./charts/edams/values-prod.yaml \
		--set image.tag=${DOCKER_IMAGE_TAG} \
		--wait --timeout 20m
	@$(call success, "生产环境部署完成!")

# 回滚
rollback:
	@read -p "输入环境 (test/staging/prod): " env; \
	helm rollback edams -n edams-$$env

# =====================================================
# 清理命令
# =====================================================

# 清理构建产物
clean:
	@echo "$(BOLD)=== 清理构建产物 ===$(RESET)"
	@$(call info, "清理后端构建产物...")
	cd microservices/edams-parent && mvn clean -B || true
	cd services && mvn clean -B || true
	@$(call info, "清理前端构建产物...")
	cd edams-web && rm -rf dist node_modules/.cache
	@$(call success, "清理完成!")

# 清理所有
clean-all: docker-stop
	@echo "$(BOLD)=== 清理所有 ===$(RESET)"
	@$(call info, "清理构建产物...")
	$(MAKE) clean
	@$(call info, "清理Docker镜像...")
	docker system prune -af --volumes
	@$(call success, "全部清理完成!")

# =====================================================
# 辅助命令
# =====================================================

# 查看日志
logs:
	@read -p "输入服务名: " service; \
	cd docker && docker-compose -f docker-compose.yml logs -f $$service

# 查看所有服务状态
status:
	@echo "$(BOLD)=== 服务状态 ===$(RESET)"
	@cd docker && docker-compose -f docker-compose.yml ps

# 重新构建并启动
rebuild:
	@$(MAKE) docker-stop
	@$(MAKE) docker-build
	@$(MAKE) docker-run

# 重新启动服务
restart:
	@read -p "输入服务名: " service; \
	cd docker && docker-compose -f docker-compose.yml restart $$service

# 进入容器
exec:
	@read -p "输入服务名: " service; \
	cd docker && docker-compose -f docker-compose.yml exec $$service /bin/sh

# 查看健康状态
health:
	@echo "$(BOLD)=== 健康检查 ===$(RESET)"
	@for port in 8080 8081 8082 8848 9200 7474; do \
		status=$$(curl -sf http://localhost:$$port/actuator/health 2>/dev/null && echo "OK" || echo "FAIL"); \
		echo "Port $$port: $$status"; \
	done || true
