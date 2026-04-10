# Makefile for EDAMS Docker Image Build
# 企业数据资产管理系统 - Docker镜像构建管理

.DEFAULT_GOAL := help

# =====================================================
# 配置参数
# =====================================================

# 镜像仓库地址
REGISTRY ?= registry.enterprise.com
NAMESPACE ?= edams

# 镜像标签
VERSION ?= $(shell git describe --tags --abbrev=0 2>/dev/null || echo "1.0.0")
BUILD_NUMBER ?= $(shell date +%Y%m%d%H%M%S)
IMAGE_TAG ?= $(VERSION)-$(BUILD_NUMBER)
LATEST_TAG = latest

# 服务名称 (可选)
SERVICES := gateway auth asset lineage quality security catalog search knowledge insight

# 构建参数
MAVEN_PROFILE ?= prod
USE_CHINA_MIRROR ?= true
SKIP_TESTS ?= true
BUILD_ARGS ?= --build-arg BUILD_PROFILE=$(MAVEN_PROFILE) \
              --build-arg USE_CHINA_MIRROR=$(USE_CHINA_MIRROR) \
              --build-arg SKIP_TESTS=$(SKIP_TESTS)

# Docker构建器配置
DOCKER_BUILDKIT ?= 1
DOCKER_BUILD_CMD = DOCKER_BUILDKIT=$(DOCKER_BUILDKIT) docker build
DOCKER_PUSH_CMD = docker push

# =====================================================
# 帮助信息
# =====================================================

.PHONY: help
help: ## 显示帮助信息
	@echo "企业数据资产管理系统 - Docker镜像构建管理"
	@echo ""
	@echo "可用命令:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "配置变量:"
	@echo "  REGISTRY=$(REGISTRY)"
	@echo "  NAMESPACE=$(NAMESPACE)"
	@echo "  VERSION=$(VERSION)"
	@echo "  IMAGE_TAG=$(IMAGE_TAG)"
	@echo "  MAVEN_PROFILE=$(MAVEN_PROFILE)"
	@echo "  USE_CHINA_MIRROR=$(USE_CHINA_MIRROR)"
	@echo "  SKIP_TESTS=$(SKIP_TESTS)"
	@echo ""
	@echo "示例:"
	@echo "  make build-service SERVICE=asset           # 构建资产管理服务镜像"
	@echo "  make push-all                             # 构建并推送所有服务"
	@echo "  make scan-security                        # 安全扫描所有镜像"

# =====================================================
# 基础构建命令
# =====================================================

.PHONY: prepare
prepare: ## 准备构建环境
	@echo "准备构建环境..."
	@mkdir -p logs
	@echo "DOCKER_BUILDKIT=$(DOCKER_BUILDKIT)"
	@echo "构建参数: $(BUILD_ARGS)"

.PHONY: build-base
build-base: prepare ## 构建基础镜像
	@echo "构建基础镜像..."
	$(DOCKER_BUILD_CMD) \
		$(BUILD_ARGS) \
		-t $(REGISTRY)/$(NAMESPACE)/base-jre:$(IMAGE_TAG) \
		-t $(REGISTRY)/$(NAMESPACE)/base-jre:$(LATEST_TAG) \
		-f Dockerfile.base \
		. 2>&1 | tee logs/build-base-$(BUILD_NUMBER).log

# =====================================================
# 服务构建命令
# =====================================================

.PHONY: build-service
build-service: prepare ## 构建单个服务镜像 (需指定SERVICE=xxx)
ifndef SERVICE
	$(error SERVICE变量未定义，请指定要构建的服务，如: make build-service SERVICE=asset)
endif
	@echo "构建服务: $(SERVICE)"
	$(DOCKER_BUILD_CMD) \
		$(BUILD_ARGS) \
		--target service \
		-t $(REGISTRY)/$(NAMESPACE)/edams-$(SERVICE):$(IMAGE_TAG) \
		-t $(REGISTRY)/$(NAMESPACE)/edams-$(SERVICE):$(LATEST_TAG) \
		--build-arg SERVICE_NAME=$(SERVICE) \
		-f Dockerfile \
		../.. 2>&1 | tee logs/build-$(SERVICE)-$(BUILD_NUMBER).log

.PHONY: build-all
build-all: prepare ## 构建所有服务镜像
	@echo "构建所有服务..."
	@for service in $(SERVICES); do \
		echo "构建服务: $$service"; \
		$(DOCKER_BUILD_CMD) \
			$(BUILD_ARGS) \
			--target service \
			-t $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG) \
			-t $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(LATEST_TAG) \
			--build-arg SERVICE_NAME=$$service \
			-f Dockerfile \
			../.. 2>&1 | tee logs/build-$$service-$(BUILD_NUMBER).log; \
		if [ $$? -ne 0 ]; then \
			echo "构建服务 $$service 失败"; \
			exit 1; \
		fi; \
	done
	@echo "所有服务构建完成"

# =====================================================
# 镜像推送命令
# =====================================================

.PHONY: push-service
push-service: build-service ## 构建并推送单个服务镜像
	@echo "推送服务: $(SERVICE)"
	$(DOCKER_PUSH_CMD) $(REGISTRY)/$(NAMESPACE)/edams-$(SERVICE):$(IMAGE_TAG)
	$(DOCKER_PUSH_CMD) $(REGISTRY)/$(NAMESPACE)/edams-$(SERVICE):$(LATEST_TAG)

.PHONY: push-all
push-all: build-all ## 构建并推送所有服务镜像
	@echo "推送所有服务..."
	@for service in $(SERVICES); do \
		echo "推送服务: $$service"; \
		$(DOCKER_PUSH_CMD) $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG); \
		$(DOCKER_PUSH_CMD) $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(LATEST_TAG); \
	done
	@echo "所有服务推送完成"

# =====================================================
# 镜像管理命令
# =====================================================

.PHONY: list-images
list-images: ## 列出所有已构建的镜像
	@echo "已构建的镜像:"
	@for service in $(SERVICES); do \
		echo "  - $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG)"; \
		echo "  - $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(LATEST_TAG)"; \
	done

.PHONY: clean-images
clean-images: ## 清理本地构建的镜像
	@echo "清理本地镜像..."
	@for service in $(SERVICES); do \
		docker rmi -f $$(docker images -q "$(REGISTRY)/$(NAMESPACE)/edams-$$service" 2>/dev/null) 2>/dev/null || true; \
	done
	@echo "清理完成"

# =====================================================
# 安全检查命令
# =====================================================

.PHONY: scan-security
scan-security: ## 安全扫描所有镜像
	@echo "执行安全扫描..."
	@which trivy >/dev/null 2>&1 || (echo "请先安装 trivy: https://aquasecurity.github.io/trivy/" && exit 1)
	@for service in $(SERVICES); do \
		echo "扫描服务: $$service"; \
		trivy image --severity HIGH,CRITICAL \
			$(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG) 2>&1 | tee logs/scan-$$service-$(BUILD_NUMBER).log; \
	done

.PHONY: scan-vulnerability
scan-vulnerability: ## 漏洞扫描 (使用Snyk)
	@echo "执行漏洞扫描 (需要Snyk令牌)..."
	@which snyk >/dev/null 2>&1 || (echo "请先安装 snyk: npm install -g snyk" && exit 1)
	@test -n "$(SNYK_TOKEN)" || (echo "请设置 SNYK_TOKEN 环境变量" && exit 1)
	@for service in $(SERVICES); do \
		echo "扫描服务: $$service"; \
		snyk container test $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG) \
			--json 2>&1 | tee logs/snyk-$$service-$(BUILD_NUMBER).log | jq '.vulnerabilities[] | {id:.id, severity:.severity, title:.title}' || true; \
	done

# =====================================================
# 镜像大小分析
# =====================================================

.PHONY: analyze-size
analyze-size: ## 分析镜像大小
	@echo "镜像大小分析:"
	@for service in $(SERVICES); do \
		size=$$(docker images --format "{{.Repository}}:{{.Tag}} {{.Size}}" "$(REGISTRY)/$(NAMESPACE)/edams-$$service" 2>/dev/null | grep "$(IMAGE_TAG)" | awk '{print $$2}') || size="N/A"; \
		echo "  - edams-$$service: $(IMAGE_TAG) - $$size"; \
	done

.PHONY: optimize-size
optimize-size: ## 优化镜像大小
	@echo "执行镜像大小优化..."
	@for service in $(SERVICES); do \
		echo "优化服务: $$service"; \
		dive $(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG) || true; \
	done

# =====================================================
# 构建报告
# =====================================================

.PHONY: generate-report
generate-report: build-all scan-security analyze-size ## 生成构建报告
	@echo "生成构建报告..."
	@echo "构建报告 - $(shell date)" > logs/build-report-$(BUILD_NUMBER).md
	@echo "========================" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "## 镜像信息" >> logs/build-report-$(BUILD_NUMBER).md
	@for service in $(SERVICES); do \
		echo "- \`$(REGISTRY)/$(NAMESPACE)/edams-$$service:$(IMAGE_TAG)\`" >> logs/build-report-$(BUILD_NUMBER).md; \
	done
	@echo "" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "## 安全扫描摘要" >> logs/build-report-$(BUILD_NUMBER).md
	@grep -h "CRITICAL\|HIGH" logs/scan-*.log | sort | uniq >> logs/build-report-$(BUILD_NUMBER).md
	@echo "" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "## 构建日志" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "查看详细日志: logs/build-*-$(BUILD_NUMBER).log" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "" >> logs/build-report-$(BUILD_NUMBER).md
	@echo "报告已保存到: logs/build-report-$(BUILD_NUMBER).md"

# =====================================================
# 清理命令
# =====================================================

.PHONY: clean
clean: clean-images ## 清理构建产物和日志
	@echo "清理构建产物..."
	@rm -rf logs/
	@rm -f *.log
	@echo "清理完成"

# =====================================================
# CI/CD集成命令
# =====================================================

.PHONY: ci-build
ci-build: prepare build-all scan-security ## CI环境构建流程
	@echo "CI构建流程完成"

.PHONY: cd-deploy
cd-deploy: push-all generate-report ## CD环境部署流程
	@echo "CD部署流程完成"
	@echo "部署完成，查看报告: logs/build-report-$(BUILD_NUMBER).md"

.PHONY: full-pipeline
full-pipeline: ci-build cd-deploy ## 完整CI/CD流水线
	@echo "完整CI/CD流水线执行完成"