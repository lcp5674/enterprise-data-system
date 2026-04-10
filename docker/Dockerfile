# =====================================================
# Dockerfile - 多阶段构建
# =====================================================

# 阶段1：构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 设置Maven镜像
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 复制pom.xml
COPY edams-parent/pom.xml edams-parent/
COPY edams-parent/edams-common/pom.xml edams-parent/edams-common/
COPY edams-parent/edams-asset/pom.xml edams-parent/edams-asset/
COPY edams-parent/edams-auth/pom.xml edams-parent/edams-auth/
COPY edams-parent/edams-gateway/pom.xml edams-parent/edams-gateway/

# 下载依赖（利用缓存）
RUN mvn dependency:go-offline -B -f edams-parent/pom.xml || true

# 复制源码
COPY edams-parent edams-parent

# 构建
RUN mvn clean package -DskipTests -B -f edams-parent/pom.xml

# 阶段2：运行
FROM eclipse-temurin:17-jre-alpine

# 安全配置
RUN addgroup -S edams && adduser -S edams -G edams
USER edams

WORKDIR /app

# 复制jar包
COPY --from=builder /build/edams-parent/edams-asset/target/*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1

# JVM优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
