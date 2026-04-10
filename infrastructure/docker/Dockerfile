# =====================================================
# EDAMS - Dockerfile (Multi-stage Build)
# 企业数据资产管理系统 - 多阶段构建
# =====================================================

# 阶段1: 构建阶段 (Builder)
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# 环境变量
ENV APP_HOME=/app
WORKDIR $APP_HOME

# 设置镜像代理 (国内使用)
# RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list \
#     && sed -i 's/security.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list

# 安装基础工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# 复制POM文件
COPY pom.xml .
COPY edams-common/pom.xml edams-common/
COPY edams-gateway/pom.xml edams-gateway/
COPY edams-auth/pom.xml edams-auth/
COPY edams-asset/pom.xml edams-asset/
COPY edams-lineage/pom.xml edams-lineage/
COPY edams-quality/pom.xml edams-quality/
COPY edams-security/pom.xml edams-security/
COPY edams-catalog/pom.xml edams-catalog/
COPY edams-search/pom.xml edams-search/
COPY edams-knowledge/pom.xml edams-knowledge/
COPY edams-insight/pom.xml edams-insight/
COPY edams-fabric/pom.xml edams-fabric/
COPY edams-monitor/pom.xml edams-monitor/
COPY edams-scheduler/pom.xml edams-scheduler/

# 下载依赖 (利用Docker缓存)
RUN mvn dependency:go-offline -B -DskipTests

# 复制源代码
COPY . .

# 编译打包
ARG BUILD_PROFILE=prod
RUN mvn clean package -DskipTests -B -P${BUILD_PROFILE}

# 阶段2: JRE运行环境
FROM eclipse-temurin:17-jre-alpine AS runtime

# 安全配置: 创建非root用户
RUN addgroup -S edams && adduser -S edams -G edams
USER edams

# 工作目录
WORKDIR /app

# 复制可执行JAR
COPY --from=builder --chown=edams:edams /app/target/*.jar app.jar

# 创建配置目录
RUN mkdir -p config \
    && touch config/application.yml

# 健康检查配置
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM调优参数
ENV JAVA_OPTS="\
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -XX:+ExplicitGCInvokesConcurrent \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8"

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# =====================================================
# Dockerfile 标签
# =====================================================
LABEL maintainer="devops@enterprise.com"
LABEL version="1.0.0"
LABEL description="Enterprise Data Asset Management System"
LABEL org.opencontainers.image.source="https://github.com/enterprise/edams"