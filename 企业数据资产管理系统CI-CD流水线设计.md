# 企业数据资产管理系统CI/CD流水线设计

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| V1.0 | 2026-04-10 | 架构师团队 | 初始版本 |

---

## 目录

1. [流水线架构设计](#1-流水线架构设计)
2. [质量门禁配置](#2-质量门禁配置)
3. [自动化测试策略](#3-自动化测试策略)
4. [部署策略](#4-部署策略)
5. [回滚策略](#5-回滚策略)
6. [运维脚本](#6-运维脚本)

---

## 1. 流水线架构设计

### 1.1 整体流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CI/CD 流水线架构                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Code Commit    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐│
│  ─────────────► │ Checkout│───►│  Build │───►│  Test  │───►│ Analyze ││
│                 └─────────┘    └─────────┘    └─────────┘    └─────────┘│
│                                                                        │
│   PR/MR触发      ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐│
│  ─────────────► │ Checkout│───►│  Build │───►│  Test  │───►│ Deploy  ││
│                 └─────────┘    └─────────┘    └─────────┘    └─────────┘│
│                        │          │            │              │        │
│                        ▼          ▼            ▼              ▼        │
│                   [Maven/Gradle] [单元测试] [SonarQube]  [Docker Hub] │
│                                                                         │
│   自动触发        ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐│
│  ─────────────► │ Staging │───►│ Smoke  │───►│ E2E     │───►│ Staging ││
│                 └─────────┘    └─────────┘    └─────────┘    └─────────┘│
│                                                                        │
│   手动触发        ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐│
│  ─────────────► │ Pre-    │───►│ Deploy │───►│ Health  │───►│ Prod    ││
│                 │ Approval│    │ Prod   │    │ Check   │    │ Ready   ││
│                 └─────────┘    └─────────┘    └─────────┘    └─────────┘│
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 GitLab CI/CD配置

```yaml
# .gitlab-ci.yml
stages:
  - checkout
  - build
  - test
  - analyze
  - deploy
  - verify

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: ""
  MAVEN_OPTS: "-Dmaven.repo.local=/root/.m2/repository"
  DOCKER_REGISTRY: registry.example.com
  DOCKER_IMAGE_TAG: $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA

# ============ 默认配置 ============
.default:
  image: maven:3.9-eclipse-temurin-17
  retry:
    max: 2
    when:
      - runner_system_failure
      - stuck_or_timeout_failure

# ============ 缓存配置 ============
maven_cache:
  cache:
    key: ${CI_COMMIT_REF_SLUG}
    paths:
      - .m2/repository
      - target/

node_cache:
  cache:
    key: ${CI_COMMIT_REF_SLUG}
    paths:
      - node_modules/
      - .npm/

# ============ 阶段：代码检出 ============
checkout:
  stage: checkout
  extends: .default
  script:
    - echo "Checking out code..."
    - git lfs pull || true
  artifacts:
    paths:
      - ./
    expire_in: 1 hour
  only:
    - merge_requests
    - main
    - develop
    - /^feature\/.*$/
    - /^release\/.*$/

# ============ 阶段：编译构建 ============
compile:
  stage: build
  extends: .default
  cache:
    - maven_cache
  script:
    - mvn clean compile -DskipTests
  artifacts:
    paths:
      - target/**/classes
    expire_in: 1 day
  only:
    - merge_requests
    - main
    - develop
    - /^feature\/.*$/

maven_package:
  stage: build
  extends: .default
  cache:
    - maven_cache
  script:
    - mvn package -DskipTests -B
    - echo "Build completed successfully"
  artifacts:
    paths:
      - "**/target/*.jar"
    expire_in: 7 days
  only:
    - merge_requests
    - main
    - develop

docker_build:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  cache: []
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $DOCKER_REGISTRY
    - docker build -t $DOCKER_IMAGE_TAG .
    - docker build -t $CI_REGISTRY_IMAGE:latest .
    - docker push $DOCKER_IMAGE_TAG
    - docker push $CI_REGISTRY_IMAGE:latest
  only:
    - main
    - develop
  when: manual

# ============ 阶段：测试 ============
unit_test:
  stage: test
  extends: .default
  cache:
    - maven_cache
  script:
    - mvn test -B
  coverage: '/Total:.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
      coverage_report:
        coverage_format: jacoco
        path: target/site/jacoco/jacoco.xml
    paths:
      - target/surefire-reports/
    expire_in: 7 days
  only:
    - merge_requests
    - main
    - develop
    - /^feature\/.*$/
  except:
    - schedules

integration_test:
  stage: test
  extends: .default
  services:
    - postgres:15
    - redis:7
  cache:
    - maven_cache
  script:
    - mvn verify -Dspring.profiles.active=test -B
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
    paths:
      - target/surefire-reports/
    expire_in: 7 days
  only:
    - merge_requests
    - main
    - develop
  when: manual

# ============ 阶段：代码分析 ============
sonar_check:
  stage: analyze
  image: sonarsource/sonar-scanner-cli:latest
  cache: []
  script:
    - sonar-scanner
  artifacts:
    reports:
      sonar: sonar-report.json
    expire_in: 7 days
  only:
    - merge_requests
    - main
    - develop
  except:
    - schedules

# ============ 阶段：部署 ============
deploy_staging:
  stage: deploy
  image: bitnami/kubectl:latest
  services:
    - docker:dind
  environment:
    name: staging
    url: https://staging.edams.example.com
    on_stop: stop_staging
  script:
    - kubectl set image deployment/edams-gateway gateway=$DOCKER_IMAGE_TAG -n edams-staging
    - kubectl set image deployment/edams-asset asset=$DOCKER_IMAGE_TAG -n edams-staging
    - kubectl rollout status deployment/edams-gateway -n edams-staging --timeout=300s
    - kubectl rollout status deployment/edams-asset -n edams-staging --timeout=300s
    - kubectl exec -n edams-staging deployment/edams-gateway -- sh -c "curl -f http://localhost:8888/actuator/health"
  only:
    - develop
    - /^release\/.*$/

deploy_production:
  stage: deploy
  image: bitnami/kubectl:latest
  services:
    - docker:dind
  environment:
    name: production
    url: https://edams.example.com
  before_script:
    - echo "Production deployment requires manual approval"
  script:
    - kubectl set image deployment/edams-gateway gateway=$DOCKER_IMAGE_TAG -n edams-prod
    - kubectl set image deployment/edams-asset asset=$DOCKER_IMAGE_TAG -n edams-prod
    - kubectl rollout status deployment/edams-gateway -n edams-prod --timeout=300s
    - kubectl rollout status deployment/edams-asset -n edams-prod --timeout=300s
  manual: true
  only:
    - main
    - /^release\/.*$/
  when: manual

# ============ 阶段：验证 ============
smoke_test:
  stage: verify
  image: curlimages/curl:latest
  script:
    - curl -f https://staging.edams.example.com/actuator/health || exit 1
    - curl -f https://staging.edams.example.com/api/v1/assets/health || exit 1
  only:
    - develop
    - /^release\/.*$/
  after_script:
    - echo "Smoke test completed"

e2e_test:
  stage: verify
  image: cypress/browsers:node18-chrome109
  cache:
    - node_cache
  services:
    - docker:dind
  script:
    - npm ci
    - npm run e2e:ci
  artifacts:
    paths:
      - cypress/videos/
      - cypress/screenshots/
    expire_in: 7 days
  only:
    - develop
    - /^release\/.*$/
  when: manual

# ============ 清理阶段 ============
stop_staging:
  stage: deploy
  image: bitnami/kubectl:latest
  environment:
    name: staging
    action: stop
  script:
    - kubectl delete deployment edams-gateway edams-asset -n edams-staging || true
  when: manual
  only:
    - develop
```

### 1.3 SonarQube质量配置

```properties
# sonar-project.properties
sonar.projectKey=edams
sonar.projectName=Enterprise Data Asset Management System
sonar.projectVersion=1.0.0

sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.sourceEncoding=UTF-8

# 覆盖率
sonar.coverage.jacoco.xmlReportsPaths=target/site/jacoco/jacoco.xml

# 测试报告
sonar.surefire.reportsPath=target/surefire-reports

# 代码检查规则
sonar.java.codeAnalysis.Profiles=edams-profile

# 问题阈值
sonar.qualitygate.wait=true
sonar.qualitygate.conditions=5

# 排除规则
sonar.exclusions=**/generated/**,**/target/**,**/*.sql
sonar.test.exclusions=**/target/**
```

```xml
<!-- pom.xml SonarQube配置 -->
<properties>
    <sonar.host.url>http://sonar:9000</sonar.host.url>
    <sonar.login>${SONAR_TOKEN}</sonar.login>
    <sonar.projectKey>edams-${project.artifactId}</sonar.projectKey>
    <sonar.coverage.jacoco.xmlReportsPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportsPaths>
</properties>

<build>
    <plugins>
        <!-- JaCoCo覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <key>instruction</key>
                                        <value>COVEREDRATIO</value>
                                        <threshold>0.80</threshold>
                                    </limit>
                                    <limit>
                                        <key>branch</key>
                                        <value>COVEREDRATIO</value>
                                        <threshold>0.75</threshold>
                                    </limit>
                                    <limit>
                                        <key>class</key>
                                        <value>COVEREDRATIO</value>
                                        <threshold>0.85</threshold>
                                    </limit>
                                    <limit>
                                        <key>method</key>
                                        <value>COVEREDRATIO</value>
                                        <threshold>0.80</threshold>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- SonarQube Scanner -->
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
            <version>3.10.0.2594</version>
        </plugin>
    </plugins>
</build>
```

---

## 2. 质量门禁配置

### 2.1 SonarQube质量门

```json
{
  "name": "EDAMS Quality Gate",
  "description": "企业数据资产管理系统质量门",
  "metricPredicates": [
    {
      "metric": "sqale_rating",
      "op": "LT",
      "value": "2.0"
    },
    {
      "metric": "reliability_rating",
      "op": "LT",
      "value": "2.0"
    },
    {
      "metric": "security_rating",
      "op": "LT",
      "value": "2.0"
    },
    {
      "metric": "coverage",
      "op": "LT",
      "value": 80.0
    },
    {
      "metric": "new_coverage",
      "op": "LT",
      "value": 80.0
    },
    {
      "metric": "duplicated_lines_density",
      "op": "GT",
      "value": 3.0
    },
    {
      "metric": "new_duplicated_lines_density",
      "op": "GT",
      "value": 3.0
    },
    {
      "metric": "blocker_violations",
      "op": "GT",
      "value": 0
    },
    {
      "metric": "critical_violations",
      "op": "GT",
      "value": 0
    },
    {
      "metric": "major_violations",
      "op": "GT",
      "value": 10
    }
  ],
  "onBranches": [
    {
      "type": "INCLUDE",
      "value": ["main", "develop", "feature/*", "release/*"]
    }
  ]
}
```

### 2.2 代码检查规则

```xml
<!-- pom.xml PMD/Checkstyle配置 -->
<build>
    <plugins>
        <!-- PMD 代码检查 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>3.21.0</version>
            <configuration>
                <rulesets>
                    <ruleset>/rulesets/edams-pmd-ruleset.xml</ruleset>
                </rulesets>
                <printFailingErrors>true</printFailingErrors>
                <failOnViolation>true</failOnViolation>
                <enforcerRules>
                    <key>CheckResult</key>
                    <value>FAIL</value>
                </enforcerRules>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- Checkstyle 代码格式 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
                <includeResources>true</includeResources>
                <includeTestSourceDirectory>true</includeTestSourceDirectory>
            </configuration>
            <executions>
                <execution>
                    <id>checkstyle-check</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- SpotBugs 静态分析 -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>4.8.1.0</version>
            <configuration>
                <effort>Max</effort>
                <threshold>Medium</threshold>
                <failOnError>true</failOnError>
                <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 2.3 安全扫描规则

```yaml
# 安全扫描配置
security_checks:
  dependency_check:
    enabled: true
    severity: HIGH
    fail_on_cvss: 7.0
    
  secrets_detection:
    enabled: true
    patterns:
      - "password="
      - "secret="
      - "api_key="
      - "aws_access_key"
      - "PRIVATE KEY"
      
  sast_scan:
    enabled: true
    rules:
      - id: java:S3649
        title: "SQL注入"
        severity: CRITICAL
      - id: java:S5131
        title: "XSS攻击"
        severity: HIGH
      - id: java:S2091
        title: "权限绕过"
        severity: HIGH
```

---

## 3. 自动化测试策略

### 3.1 测试金字塔

```
                    ▲
                   ╱ ╲
                  ╱   ╲
                 ╱ E2E╲          少量、关键场景
                ╱──────╲
               ╱集成测试╲         核心业务流程
              ╱──────────╲
             ╱  单元测试  ╲        大量、快速反馈
            ╱──────────────╲
```

### 3.2 单元测试规范

```java
package com.enterprise.edams.asset.service;

import com.enterprise.edams.asset.dto.CreateAssetRequest;
import com.enterprise.edams.asset.entity.DataAsset;
import com.enterprise.edams.asset.exception.AssetException;
import com.enterprise.edams.asset.repository.DataAssetRepository;
import com.enterprise.edams.asset.service.impl.DataAssetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DataAssetService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据资产管理服务测试")
class DataAssetServiceTest {

    @Mock
    private DataAssetRepository repository;

    @InjectMocks
    private DataAssetServiceImpl service;

    private DataAsset sampleAsset;
    private CreateAssetRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleAsset = DataAsset.builder()
                .id(1L)
                .assetName("测试资产")
                .assetType(AssetType.TABLE)
                .description("测试描述")
                .ownerId(1L)
                .sensitivity(DataSensitivity.INTERNAL)
                .status(AssetStatus.DRAFT)
                .qualityScore(BigDecimal.ZERO)
                .createdBy("system")
                .createdTime(LocalDateTime.now())
                .updatedBy("system")
                .updatedTime(LocalDateTime.now())
                .deleted(0)
                .version(0)
                .build();

        createRequest = CreateAssetRequest.builder()
                .assetName("测试资产")
                .assetType(AssetType.TABLE)
                .description("测试描述")
                .ownerId(1L)
                .sensitivity(DataSensitivity.INTERNAL)
                .build();
    }

    @Nested
    @DisplayName("创建资产测试")
    class CreateAssetTests {

        @Test
        @DisplayName("成功创建资产")
        void shouldCreateAssetSuccessfully() {
            // Given
            when(repository.existsByAssetName(createRequest.getAssetName())).thenReturn(false);
            when(repository.save(any(DataAsset.class))).thenReturn(sampleAsset);

            // When
            DataAsset result = service.create(createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAssetName()).isEqualTo(createRequest.getAssetName());
            assertThat(result.getAssetType()).isEqualTo(createRequest.getAssetType());

            // 验证保存被调用
            ArgumentCaptor<DataAsset> captor = ArgumentCaptor.forClass(DataAsset.class);
            verify(repository).save(captor.capture());
            
            DataAsset saved = captor.getValue();
            assertThat(saved.getAssetName()).isEqualTo("测试资产");
        }

        @Test
        @DisplayName("资产名称重复时抛出异常")
        void shouldThrowExceptionWhenNameDuplicate() {
            // Given
            when(repository.existsByAssetName(createRequest.getAssetName())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.create(createRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("资产名称已存在");
        }

        @Test
        @DisplayName("资产名称为空时抛出异常")
        void shouldThrowExceptionWhenNameEmpty() {
            // Given
            createRequest.setAssetName("");

            // When & Then
            assertThatThrownBy(() -> service.create(createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("资产名称不能为空");
        }

        @Test
        @DisplayName("资产名称超长时抛出异常")
        void shouldThrowExceptionWhenNameTooLong() {
            // Given
            createRequest.setAssetName("a".repeat(201));

            // When & Then
            assertThatThrownBy(() -> service.create(createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("资产名称不能超过200个字符");
        }
    }

    @Nested
    @DisplayName("查询资产测试")
    class QueryAssetTests {

        @Test
        @DisplayName("根据ID查询资产成功")
        void shouldFindAssetById() {
            // Given
            when(repository.findById(1L)).thenReturn(Optional.of(sampleAsset));

            // When
            DataAsset result = service.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getAssetName()).isEqualTo("测试资产");
        }

        @Test
        @DisplayName("根据ID查询不存在时抛出异常")
        void shouldThrowExceptionWhenAssetNotFound() {
            // Given
            when(repository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.getById(999L))
                    .isInstanceOf(AssetException.class);
        }
    }

    @Nested
    @DisplayName("删除资产测试")
    class DeleteAssetTests {

        @Test
        @DisplayName("成功删除资产")
        void shouldDeleteAssetSuccessfully() {
            // Given
            when(repository.findById(1L)).thenReturn(Optional.of(sampleAsset));
            doNothing().when(repository).removeById(1L);

            // When
            service.deleteById(1L);

            // Then
            verify(repository).removeById(1L);
        }

        @Test
        @DisplayName("删除受保护资产时抛出异常")
        void shouldThrowExceptionWhenDeleteProtectedAsset() {
            // Given
            sampleAsset.setProtected(true);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleAsset));

            // When & Then
            assertThatThrownBy(() -> service.deleteById(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("受保护资产不允许删除");
        }
    }

    @Nested
    @DisplayName("发布资产测试")
    class PublishAssetTests {

        @Test
        @DisplayName("成功发布草稿状态的资产")
        void shouldPublishDraftAsset() {
            // Given
            when(repository.findById(1L)).thenReturn(Optional.of(sampleAsset));
            when(repository.save(any(DataAsset.class))).thenAnswer(invocation -> {
                DataAsset asset = invocation.getArgument(0);
                asset.setStatus(AssetStatus.PUBLISHED);
                return asset;
            });

            // When
            DataAsset result = service.publish(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo(AssetStatus.PUBLISHED);
            verify(repository).save(any(DataAsset.class));
        }
    }
}
```

### 3.3 集成测试配置

```java
package com.enterprise.edams.asset;

import com.enterprise.edams.asset.entity.DataAsset;
import com.enterprise.edams.asset.repository.DataAssetRepository;
import com.enterprise.edams.common.testcontainers.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据资产Repository集成测试
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainersConfiguration.class)
class DataAssetRepositoryIntegrationTest {

    @Autowired
    private DataAssetRepository repository;

    @Test
    void shouldSaveAndFindAsset() {
        // Given
        DataAsset asset = DataAsset.builder()
                .assetName("Test Asset")
                .assetType(AssetType.TABLE)
                .createdBy("test")
                .build();

        // When
        DataAsset saved = repository.save(asset);
        Optional<DataAsset> found = repository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getAssetName()).isEqualTo("Test Asset");
    }
}
```

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:tc:postgresql:15://localhost:5432/edams_test
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: false

  redis:
    host: localhost
    port: 6379
    docker:
      image: redis:7-testcontainer

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
```

### 3.4 覆盖率要求

| 模块类型 | 最低覆盖率 | 说明 |
|----------|------------|------|
| Controller层 | 70% | 重点验证参数校验 |
| Service层 | 80% | 核心业务逻辑 |
| Repository层 | 60% | 查询方法验证 |
| 工具类 | 90% | 通用组件 |
| 异常类 | 80% | 错误处理 |
| DTO/Convert | 70% | 数据转换 |
| **整体** | **80%** | 全局目标 |

---

## 4. 部署策略

### 4.1 蓝绿部署

```yaml
# Kubernetes部署配置 - 蓝绿部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: edams-asset-blue
  namespace: edams-prod
  labels:
    app: edams-asset
    slot: blue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: edams-asset
      slot: blue
  template:
    metadata:
      labels:
        app: edams-asset
        slot: blue
    spec:
      containers:
      - name: edams-asset
        image: registry.example.com/edams-asset:v1.2.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          successThreshold: 1
          failureThreshold: 3
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 15
          failureThreshold: 5

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: edams-asset-green
  namespace: edams-prod
  labels:
    app: edams-asset
    slot: green
spec:
  replicas: 3
  selector:
    matchLabels:
      app: edams-asset
      slot: green
  template:
    metadata:
      labels:
        app: edams-asset
        slot: green
    spec:
      containers:
      - name: edams-asset
        image: registry.example.com/edams-asset:v1.3.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"

---
apiVersion: v1
kind: Service
metadata:
  name: edams-asset
  namespace: edams-prod
spec:
  selector:
    app: edams-asset
    slot: blue        # 切换时修改为green
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
```

### 4.2 金丝雀发布

```yaml
# Canary部署策略
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: edams-asset
  namespace: edams-prod
spec:
  replicas: 10
  strategy:
    canary:
      # 初始流量分配
      canaryMetadata:
        labels:
          version: canary
      stableMetadata:
        labels:
          version: stable
      # 流量权重
      trafficRouting:
        istio:
          virtualService:
            name: edams-asset-vsvc
            routes:
              - primary
      # 分阶段发布
      steps:
        - setWeight: 5      # 5% 流量到新版本
        - pause: {duration: 10m}  # 观察10分钟
        - setWeight: 20     # 20% 流量
        - pause: {duration: 10m}
        - setWeight: 50     # 50% 流量
        - pause: {duration: 10m}
        - setWeight: 100    # 100% 流量
      # 自动回滚条件
      analysis:
        templates:
          - templateName: success-rate
        startingStep: 1
        args:
          - name: service-name
            value: edams-asset-canary
---
# 分析模板
apiVersion: argoproj.io/v1alpha1
kind: AnalysisTemplate
metadata:
  name: success-rate
spec:
  args:
    - name: service-name
  metrics:
    - name: success-rate
      interval: 5m
      successCondition: result[0] >= 0.95
      failureLimit: 3
      provider:
        prometheus:
          address: http://prometheus:9090
          query: |
            sum(rate(http_server_requests_seconds_count{
              job="{{args.service-name}}",
              status=~"2.."
            }[5m])) / 
            sum(rate(http_server_requests_seconds_count{
              job="{{args.service-name}}"
            }[5m]))
```

### 4.3 滚动更新

```yaml
# 滚动更新策略
apiVersion: apps/v1
kind: Deployment
metadata:
  name: edams-asset
  namespace: edams-prod
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%        # 最多超出25%的Pod
      maxUnavailable: 0   # 不可用Pod为0，保证服务可用
  minReadySeconds: 60      # 新Pod就绪后等待60秒
  progressDeadlineSeconds: 600  # 超时时间600秒
```

---

## 5. 回滚策略

### 5.1 自动回滚

```yaml
# 部署回滚配置
apiVersion: batch/v1
kind: Job
metadata:
  name: rollback-edams-asset
  namespace: edams-prod
spec:
  template:
    spec:
      containers:
      - name: kubectl
        image: bitnami/kubectl:latest
        command:
        - /bin/sh
        - -c
        - |
          # 回滚到上一个版本
          kubectl rollout undo deployment/edams-asset -n edams-prod
          
          # 等待回滚完成
          kubectl rollout status deployment/edams-asset -n edams-prod --timeout=300s
          
          # 验证健康状态
          kubectl exec -n edams-prod deployment/edams-asset -- curl -f http://localhost:8080/actuator/health
      restartPolicy: OnFailure
```

### 5.2 回滚脚本

```bash
#!/bin/bash
# scripts/rollback.sh

set -e

NAMESPACE=${1:-edams-prod}
SERVICE=${2:-edams-asset}
REVISION=${3:-}

echo "=========================================="
echo "开始回滚服务"
echo "namespace: $NAMESPACE"
echo "service: $SERVICE"
echo "revision: ${REVISION:-上一版本}"
echo "=========================================="

# 获取当前版本
CURRENT_REVISION=$(kubectl rollout history deployment/$SERVICE -n $NAMESPACE | grep -E "^[0-9]+" | tail -1 | awk '{print $1}')
echo "当前版本: $CURRENT_REVISION"

if [ -n "$REVISION" ]; then
    echo "回滚到版本: $REVISION"
    kubectl rollout undo deployment/$SERVICE -n $NAMESPACE --to-revision=$REVISION
else
    echo "回滚到上一版本"
    kubectl rollout undo deployment/$SERVICE -n $NAMESPACE
fi

# 等待回滚完成
echo "等待回滚完成..."
kubectl rollout status deployment/$SERVICE -n $NAMESPACE --timeout=600s

# 验证健康状态
echo "验证健康状态..."
POD=$(kubectl get pods -n $NAMESPACE -l app=$SERVICE -o jsonpath='{.items[0].metadata.name}')
HEALTH=$(kubectl exec -n $NAMESPACE $POD -- curl -s http://localhost:8080/actuator/health)

if echo "$HEALTH" | grep -q '"status":"UP"'; then
    echo "✅ 回滚成功，服务健康"
else
    echo "❌ 回滚后服务不健康，需要人工介入"
    exit 1
fi

echo "=========================================="
echo "回滚完成"
echo "=========================================="
```

### 5.3 数据库回滚

```sql
-- 数据库回滚脚本
-- V1.0.1__add_column_phone.sql 回滚

-- 备份当前数据
CREATE TABLE IF NOT EXISTS data_asset_backup AS 
SELECT * FROM data_asset WHERE 1=0;

INSERT INTO data_asset_backup SELECT * FROM data_asset;

-- 删除新增的列
ALTER TABLE data_asset DROP COLUMN IF EXISTS phone;
ALTER TABLE data_asset DROP COLUMN IF EXISTS phone_country_code;

-- 记录回滚日志
INSERT INTO migration_log (version, action, executed_at, status)
VALUES ('V1.0.1', 'ROLLBACK', NOW(), 'SUCCESS');
```

---

## 6. 运维脚本

### 6.1 健康检查脚本

```bash
#!/bin/bash
# scripts/health-check.sh

set -e

SERVICE_NAME=${1:-"edams-gateway"}
SERVICE_URL=${2:-"http://localhost:8888"}
THRESHOLD=${3:-3}
MAX_RESPONSE_TIME=5000

echo "=========================================="
echo "健康检查脚本"
echo "服务: $SERVICE_NAME"
echo "URL: $SERVICE_URL"
echo "阈值: $THRESHOLD次失败"
echo "=========================================="

FAIL_COUNT=0
CHECK_COUNT=0

while [ $CHECK_COUNT -lt $THRESHOLD ]; do
    CHECK_COUNT=$((CHECK_COUNT + 1))
    
    echo -n "[$CHECK_COUNT] 检查健康状态... "
    
    START_TIME=$(date +%s%3N)
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $SERVICE_URL/actuator/health)
    END_TIME=$(date +%s%3N)
    RESPONSE_TIME=$((END_TIME - START_TIME))
    
    if [ "$HTTP_CODE" == "200" ] && [ $RESPONSE_TIME -lt $MAX_RESPONSE_TIME ]; then
        echo "✅ 正常 (HTTP: $HTTP_CODE, 耗时: ${RESPONSE_TIME}ms)"
        FAIL_COUNT=0
    else
        echo "❌ 异常 (HTTP: $HTTP_CODE, 耗时: ${RESPONSE_TIME}ms)"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        
        if [ $FAIL_COUNT -ge $THRESHOLD ]; then
            echo ""
            echo "=========================================="
            echo "❌ 健康检查失败，达到阈值"
            echo "=========================================="
            exit 1
        fi
    fi
    
    sleep 5
done

echo ""
echo "=========================================="
echo "✅ 健康检查通过"
echo "=========================================="
exit 0
```

### 6.2 日志收集脚本

```bash
#!/bin/bash
# scripts/log-collector.sh

set -e

NAMESPACE=${1:-edams-prod}
LABEL=${2:-app=edams-asset}
OUTPUT_DIR=${3:-./logs}

mkdir -p $OUTPUT_DIR

echo "=========================================="
echo "日志收集脚本"
echo "namespace: $NAMESPACE"
echo "label: $LABEL"
echo "output: $OUTPUT_DIR"
echo "=========================================="

# 收集Pod列表
PODS=$(kubectl get pods -n $NAMESPACE -l $LABEL -o jsonpath='{.items[*].metadata.name}')

for POD in $PODS; do
    echo "收集 Pod: $POD"
    
    # 应用日志
    kubectl logs $POD -n $NAMESPACE > $OUTPUT_DIR/${POD}-app.log 2>&1
    
    # 前一个实例的日志（如果存在）
    kubectl logs $POD -n $NAMESPACE --previous > $OUTPUT_DIR/${POD}-previous.log 2>&1 || true
    
    echo "  ✅ 完成"
done

# 收集事件
kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' > $OUTPUT_DIR/events.log

echo ""
echo "=========================================="
echo "日志收集完成"
echo "文件保存在: $OUTPUT_DIR"
echo "=========================================="

# 打包
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
tar -czf logs_${TIMESTAMP}.tar.gz -C $OUTPUT_DIR .
echo "已打包: logs_${TIMESTAMP}.tar.gz"
```

### 6.3 性能基准测试脚本

```bash
#!/bin/bash
# scripts/benchmark.sh

set -e

API_URL=${1:-"http://localhost:8888/api/v1/assets"}
CONCURRENT=${2:-100}
DURATION=${3:-60}

echo "=========================================="
echo "性能基准测试"
echo "URL: $API_URL"
echo "并发: $CONCURRENT"
echo "时长: ${DURATION}秒"
echo "=========================================="

# 预热
echo "预热..."
for i in {1..10}; do
    curl -s $API_URL > /dev/null
done

# 性能测试
echo "开始测试..."
RESULTS=$(ab -n 10000 -c $CONCURRENT -t $DURATION -g results.tsv $API_URL)

echo "$RESULTS"

# 提取关键指标
REQUESTS_PER_SEC=$(echo "$RESULTS" | grep "Requests per second" | awk '{print $4}')
TIME_PER_REQUEST=$(echo "$RESULTS" | grep "Time per request" | head -1 | awk '{print $4}')
FAILED=$(echo "$RESULTS" | grep "Failed requests" | awk '{print $3}')

echo ""
echo "=========================================="
echo "性能测试结果"
echo "=========================================="
echo "QPS: $REQUESTS_PER_SEC"
echo "平均响应时间: ${TIME_PER_REQUEST}ms"
echo "失败请求: $FAILED"
echo "=========================================="

# 结果判定
if (( $(echo "$REQUESTS_PER_SEC > 500" | bc -l) )); then
    echo "✅ 性能达标"
else
    echo "❌ 性能不达标"
fi
```

---

## 附录

### A. 流水线状态徽章

```markdown
[![Build Status](https://gitlab.example.com/enterprise/edams/badges/main/pipeline.svg)](https://gitlab.example.com/enterprise/edams/-/pipelines)
[![Coverage](https://gitlab.example.com/enterprise/edams/badges/main/coverage.svg)](https://gitlab.example.com/enterprise/edams/-/pipelines)
[![Quality Gate Status](https://sonar.example.com/api/project_badges/measure?project=edams&metric=alert_status)](https://sonar.example.com/dashboard?id=edams)
```

### B. 环境配置矩阵

| 环境 | 触发方式 | 审批 | 数据 |
|------|----------|------|------|
| dev | 自动 | 无 | 测试数据 |
| staging | 自动/手动 | 无 | 脱敏生产数据 |
| production | 手动 | 必须 | 生产数据 |
| hotfix | 手动 | 必须 | 生产数据 |

### C. 发布检查清单

- [ ] 代码Review已通过
- [ ] 所有测试已通过
- [ ] SonarQube质量门通过
- [ ] 安全扫描无高危漏洞
- [ ] 数据库迁移脚本已测试
- [ ] 回滚方案已准备
- [ ] 监控告警已配置
- [ ] 值班人员已通知
