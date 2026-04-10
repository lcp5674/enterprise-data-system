# =====================================================
# EDAMS - 优化Docker多阶段构建
# 企业数据资产管理系统生产镜像构建
# =====================================================

# 阶段1: 依赖下载层 (最大化Docker缓存)
FROM maven:3.9.4-eclipse-temurin-17 AS deps

WORKDIR /build

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

# 使用国内镜像加速 (可选)
ARG USE_CHINA_MIRROR=true
RUN if [ "$USE_CHINA_MIRROR" = "true" ]; then \
    sed -i 's/https:\/\/repo.maven.apache.org\/maven2/https:\/\/maven.aliyun.com\/repository\/central/g' /usr/share/maven/conf/settings.xml && \
    sed -i 's/https:\/\/repo.maven.apache.org\/maven2/https:\/\/maven.aliyun.com\/repository\/central/g' /root/.m2/settings.xml; \
    fi

# 下载依赖并缓存到本地 (不进行编译)
RUN mvn dependency:go-offline -B \
    -Denforcer.skip=true \
    -Dcheckstyle.skip=true \
    -DskipTests \
    -Dmaven.test.skip=true

# 阶段2: 编译打包
FROM deps AS builder

# 复制所有源代码
COPY . .

# 构建参数
ARG BUILD_PROFILE=prod
ARG SKIP_TESTS=true
ARG MAVEN_OPTS="-DskipTests"

# 编译打包
RUN mvn clean package \
    -B \
    -P${BUILD_PROFILE} \
    -DskipTests=${SKIP_TESTS} \
    -Denforcer.skip=true \
    -Dcheckstyle.skip=true \
    -Dspotbugs.skip=true \
    -Dpmd.skip=true \
    -Djacoco.skip=true \
    -Dmaven.javadoc.skip=true \
    -Dmaven.source.skip=true \
    -Dmaven.test.skip=true

# 阶段3: 瘦身JRE运行时
FROM eclipse-temurin:17-jre-alpine AS jre-slim

# 安装必要的工具 (最小化)
RUN apk add --no-cache \
    tini \
    curl \
    ca-certificates \
    tzdata \
    && update-ca-certificates \
    && rm -rf /var/cache/apk/*

# 设置时区
ENV TZ=Asia/Shanghai

# 阶段4: 服务镜像
FROM jre-slim AS service

# 安全配置: 创建非root用户和组
RUN addgroup -S edams -g 1000 && \
    adduser -S edams -u 1000 -G edams -h /home/edams -s /bin/sh && \
    mkdir -p /app && \
    chown -R edams:edams /app

WORKDIR /app
USER edams

# 从构建阶段复制JAR文件
COPY --from=builder --chown=edams:edams /build/target/*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/config \
    /app/logs \
    /app/tmp \
    && chmod 755 /app/config /app/logs /app/tmp

# 环境变量
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS=""
ENV JAVA_OPTS=""

# JVM调优参数 (生产环境优化)
ENV JVM_OPTS="\
    -Xms512m \
    -Xmx1024m \
    -Xmn384m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:G1HeapRegionSize=8m \
    -XX:InitiatingHeapOccupancyPercent=45 \
    -XX:G1ReservePercent=10 \
    -XX:ParallelGCThreads=4 \
    -XX:ConcGCThreads=2 \
    -XX:+ParallelRefProcEnabled \
    -XX:+ExplicitGCInvokesConcurrent \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseStringDeduplication \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -XX:ErrorFile=/app/logs/hs_err_pid%p.log \
    -XX:+CrashOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Djava.awt.headless=true \
    -Djava.net.preferIPv4Stack=true \
    -Djava.io.tmpdir=/app/tmp \
    -Dlogging.file.path=/app/logs \
    -Dspring.application.admin.enabled=false \
    -Dmanagement.endpoint.shutdown.enabled=false \
    -Dmanagement.endpoints.jmx.exposure.exclude=* \
    -Dmanagement.endpoints.web.exposure.include=health,info,prometheus,metrics \
    -Dmanagement.metrics.export.prometheus.enabled=true \
    -Dmanagement.health.defaults.enabled=true"

# 健康检查 (针对Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# 就绪检查
LABEL ready-probe="curl -f http://localhost:8080/actuator/health/readiness"

# 暴露端口
EXPOSE 8080

# 使用tini作为entrypoint避免僵尸进程
ENTRYPOINT ["/sbin/tini", "--"]

# 启动命令
CMD ["sh", "-c", "java ${JVM_OPTS} ${JAVA_OPTS} ${JAVA_TOOL_OPTIONS} -jar app.jar"]

# =====================================================
# Docker镜像标签
# =====================================================
LABEL maintainer="devops-team@enterprise.com"
LABEL org.opencontainers.image.title="Enterprise Data Asset Management System"
LABEL org.opencontainers.image.description="Production-ready microservice for EDAMS"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.authors="Enterprise DevOps Team"
LABEL org.opencontainers.image.source="https://github.com/enterprise/edams"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.url="https://edams.enterprise.com"
LABEL org.opencontainers.image.vendor="Enterprise Technology Co., Ltd."
LABEL org.opencontainers.image.created="2026-04-10T00:00:00Z"

# 安全标签
LABEL security.scan.enabled="true"
LABEL security.scan.type="trivy,snyk"
LABEL security.policy="non-root,no-suid,readonly-fs"