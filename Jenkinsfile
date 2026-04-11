# =====================================================
# EDAMS - Jenkins Declarative Pipeline
# 企业数据资产管理系统 Jenkins持续集成流水线
# =====================================================

@Library('shared-pipeline-library@main') _

pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            label 'docker'
            args '-v $HOME/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    options {
        buildDiscarder logRotator(numToKeepStr: '30', daysToKeepStr: '30')
        timestamps()
        timeout(time: 2, unit: 'HOURS')
        disableConcurrentBuilds()
        preserveStashes()
    }

    environment {
        // Docker配置
        DOCKER_REGISTRY = credentials('docker-registry-url')
        DOCKER_IMAGE_TAG = "${env.BRANCH_NAME}-${env.GIT_COMMIT[0..7]}"

        // Maven配置
        MAVEN_OPTS = "-Dmaven.repo.local=/root/.m2/repository -Xms512m -Xmx1024m"

        // Sonar配置
        SONAR_HOST_URL = credentials('sonar-host-url')
        SONAR_TOKEN = credentials('sonar-token')

        // Slack通知
        SLACK_CHANNEL = '#devops'
    }

    stages {
        // =====================================================
        // Stage 1: 环境准备
        // =====================================================
        stage('Prepare') {
            steps {
                script {
                    echo "============================================"
                    echo "EDAMS CI/CD Pipeline Started"
                    echo "============================================"
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT[0..7]}"
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "============================================"

                    // 打印Java和Maven版本
                    sh 'java -version'
                    sh 'mvn -version'
                }
            }
        }

        // =====================================================
        // Stage 2: 代码检出
        // =====================================================
        stage('Checkout') {
            steps {
                checkout scm
                stash name: 'source', includes: '**/*'
            }
        }

        // =====================================================
        // Stage 3: 构建后端 edams-parent (21个微服务)
        // =====================================================
        stage('Build: edams-parent') {
            when {
                anyOf {
                    changeset 'microservices/edams-parent/**'
                    changeset '**/pom.xml'
                }
            }
            stages {
                // 3.1 构建公共模块
                stage('Build: edams-common') {
                    steps {
                        dir('microservices/edams-parent') {
                            sh '''
                                mvn clean package -DskipTests -B \
                                    -pl edams-common -am \
                                    -Dmaven.javadoc.skip=true \
                                    -Dmaven.source.skip=true
                            '''
                        }
                        stash includes: 'microservices/edams-parent/edams-common/target/*.jar', name: 'edams-common'
                    }
                }

                // 3.2 构建核心网关服务
                stage('Build: Gateway & Auth') {
                    steps {
                        dir('microservices/edams-parent') {
                            sh '''
                                mvn clean package -DskipTests -B \
                                    -pl edams-gateway,edams-auth \
                                    -Dmaven.javadoc.skip=true \
                                    -Dmaven.source.skip=true
                            '''
                        }
                    }
                }

                // 3.3 构建基础业务服务
                stage('Build: Core Services') {
                    parallel {
                        stage('Build: User & Permission') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-user,edams-permission \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                        stage('Build: Notification') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-notification \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                    }
                }

                // 3.4 构建知识智能域服务
                stage('Build: Knowledge Domain') {
                    parallel {
                        stage('Build: Knowledge & LLM') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-knowledge,edams-llm \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                        stage('Build: Chatbot') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-chatbot \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                    }
                }

                // 3.5 构建高级功能域服务
                stage('Build: Advanced Services') {
                    parallel {
                        stage('Build: IoT & Value') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edge-iot-service,value-service \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                        stage('Build: SLA & Incentive') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl sla-service,incentive-service \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                        stage('Build: Watermark & Sandbox') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl watermark-service,sandbox-service \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                    }
                }

                // 3.6 构建运营支撑域服务
                stage('Build: Operation Services') {
                    parallel {
                        stage('Build: Workflow & Lifecycle') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-workflow,edams-lifecycle \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                        stage('Build: Version & Collaboration') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-version,edams-collaboration \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                        stage('Build: AIOps & Asset') {
                            steps {
                                dir('microservices/edams-parent') {
                                    sh '''
                                        mvn clean package -DskipTests -B \
                                            -pl edams-aiops,edams-asset \
                                            -Dmaven.javadoc.skip=true \
                                            -Dmaven.source.skip=true
                                    '''
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================
        // Stage 4: 构建services (8个独立微服务)
        // =====================================================
        stage('Build: Services') {
            when {
                anyOf {
                    changeset 'services/**'
                    changeset '**/pom.xml'
                }
            }
            parallel {
                stage('Build: Core Services') {
                    steps {
                        dir('services') {
                            sh '''
                                mvn clean package -DskipTests -B \
                                    -pl lineage-service,quality-service,standard-service,governance-engine \
                                    -Dmaven.javadoc.skip=true \
                                    -Dmaven.source.skip=true
                            '''
                        }
                    }
                }
                stage('Build: Support Services') {
                    steps {
                        dir('services') {
                            sh '''
                                mvn clean package -DskipTests -B \
                                    -pl portal-service,admin-service,metadata-service,index-service \
                                    -Dmaven.javadoc.skip=true \
                                    -Dmaven.source.skip=true
                            '''
                        }
                    }
                }
            }
        }

        // =====================================================
        // Stage 5: 构建前端
        // =====================================================
        stage('Build: Frontend') {
            when {
                anyOf {
                    changeset 'edams-web/**'
                    environment name: 'FORCE_BUILD_FRONTEND', value: 'true'
                }
            }
            steps {
                script {
                    def frontendImage = docker.build("edams-frontend:${env.DOCKER_IMAGE_TAG}", """
                        -f edams-web/Dockerfile.frontend
                        --build-arg NODE_ENV=production
                        edams-web/
                    """)
                }
            }
        }

        // =====================================================
        // Stage 6: 构建移动端
        // =====================================================
        stage('Build: Mobile') {
            when {
                anyOf {
                    changeset 'edams-mobile/**'
                    environment name: 'FORCE_BUILD_MOBILE', value: 'true'
                }
            }
            parallel {
                stage('Build: Android APK') {
                    steps {
                        script {
                            def flutterHome = tool 'flutter'
                            sh """
                                export PATH=\$PATH:${flutterHome}/bin
                                cd edams-mobile
                                flutter pub get
                                flutter build apk --release
                            """
                        }
                        archiveArtifacts artifacts: 'edams-mobile/build/app/outputs/flutter-apk/*.apk', fingerprint: true
                    }
                }
                stage('Build: iOS') {
                    when {
                        expression { BRANCH_NAME ==~ /main|release.*/ }
                    }
                    steps {
                        script {
                            def flutterHome = tool 'flutter'
                            sh """
                                export PATH=\$PATH:${flutterHome}/bin
                                cd edams-mobile
                                flutter pub get
                                flutter build ios --release --no-codesign
                            """
                        }
                        archiveArtifacts artifacts: 'edams-mobile/build/ios/iphoneos/*.app', fingerprint: true
                    }
                }
            }
        }

        // =====================================================
        // Stage 7: 代码质量检测
        // =====================================================
        stage('Code Quality') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            parallel {
                stage('SonarQube Analysis') {
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            dir('microservices/edams-parent') {
                                sh '''
                                    mvn sonar:sonar -B \
                                        -Dsonar.projectKey=edams-parent \
                                        -Dsonar.java.binaries=**/target/classes \
                                        -Dsonar.coverage.jacoco.xmlReportsPaths=**/jacoco.xml
                                '''
                            }
                            dir('services') {
                                sh '''
                                    mvn sonar:sonar -B \
                                        -Dsonar.projectKey=edams-services \
                                        -Dsonar.java.binaries=**/target/classes \
                                        -Dsonar.coverage.jacoco.xmlReportsPaths=**/jacoco.xml
                                '''
                            }
                        }
                    }
                }
                stage('SpotBugs') {
                    steps {
                        dir('microservices/edams-parent') {
                            sh 'mvn spotbugs:check -B || true'
                        }
                    }
                }
            }
        }

        // =====================================================
        // Stage 8: 测试阶段
        // =====================================================
        stage('Test') {
            stages {
                stage('Backend Unit Tests') {
                    steps {
                        dir('microservices/edams-parent') {
                            sh '''
                                mvn test -B \
                                    -DforkCount=2 \
                                    -DreuseForks=true \
                                    -DfailIfNoTests=false \
                                    -Dsurefire.useFile=false
                            '''
                        }
                        dir('services') {
                            sh '''
                                mvn test -B \
                                    -DforkCount=2 \
                                    -DreuseForks=true \
                                    -DfailIfNoTests=false \
                                    -Dsurefire.useFile=false
                            '''
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                            jacoco execPattern: '**/target/jacoco.exec'
                        }
                    }
                }

                stage('Frontend Tests') {
                    steps {
                        dir('edams-web') {
                            sh 'npm ci'
                            sh 'npm run test -- --coverage --passWithNoTests'
                        }
                    }
                    post {
                        always {
                            junit 'edams-web/junit.xml'
                            publishHTML([
                                reportDir: 'edams-web/coverage',
                                reportFiles: 'lcov-report/index.html',
                                reportName: 'Frontend Coverage'
                            ])
                        }
                    }
                }

                stage('E2E Tests') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                        }
                    }
                    steps {
                        script {
                            def e2eImage = docker.image('mcr.microsoft.com/playwright:v1.40.0')
                            e2eImage.pull()
                            e2eImage.inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                                sh '''
                                    cd e2e-tests
                                    npm ci
                                    npx playwright install --with-deps chromium
                                    npm run test:headed || npm run test
                                '''
                            }
                        }
                    }
                    post {
                        always {
                            publishHTML([
                                reportDir: 'e2e-tests/playwright-report',
                                reportFiles: 'index.html',
                                reportName: 'E2E Test Report'
                            ])
                            archiveArtifacts artifacts: 'e2e-tests/screenshots/**/*.png', allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        // =====================================================
        // Stage 9: 安全扫描
        // =====================================================
        stage('Security Scan') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            parallel {
                stage('OWASP Dependency Check') {
                    steps {
                        dir('microservices/edams-parent') {
                            sh '''
                                mvn org.owasp:dependency-check-maven:check -B \
                                    -DskipTests \
                                    -DconnectionString= \
                                    -DfailBuildOnCVSS=7
                            '''
                        }
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: '**/dependency-check-report.html', fingerprint: true, allowEmptyArchive: true
                        }
                    }
                }

                stage('Trivy Container Scan') {
                    steps {
                        sh '''
                            trivy image --severity HIGH,CRITICAL \
                                --exit-code 1 \
                                --ignore-unfixed \
                                ${DOCKER_REGISTRY}/edams/edams-gateway:${DOCKER_IMAGE_TAG} || true
                        '''
                    }
                }
            }
        }

        // =====================================================
        // Stage 10: Docker镜像构建与推送
        // =====================================================
        stage('Docker Build & Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            stages {
                stage('Build Backend Images') {
                    steps {
                        script {
                            // 构建edams-parent服务镜像
                            def parentServices = [
                                'edams-gateway', 'edams-auth', 'edams-user', 'edams-permission',
                                'edams-notification', 'edams-knowledge', 'edams-llm', 'edams-chatbot',
                                'edams-workflow', 'edams-lifecycle', 'edams-version', 'edams-collaboration',
                                'edams-aiops', 'edams-asset', 'edge-iot-service', 'value-service',
                                'sla-service', 'incentive-service', 'watermark-service', 'sandbox-service'
                            ]

                            parentServices.each { service ->
                                def image = docker.build(
                                    "${DOCKER_REGISTRY}/edams/${service}:${DOCKER_IMAGE_TAG}",
                                    "-f microservices/edams-parent/${service}/Dockerfile microservices/edams-parent/"
                                )
                                image.push()
                                if (env.BRANCH_NAME == 'main') {
                                    image.push('latest')
                                }
                            }

                            // 构建services服务镜像
                            def services = [
                                'lineage-service', 'quality-service', 'standard-service', 'governance-engine',
                                'portal-service', 'admin-service', 'metadata-service', 'index-service'
                            ]

                            services.each { service ->
                                def image = docker.build(
                                    "${DOCKER_REGISTRY}/edams/${service}:${DOCKER_IMAGE_TAG}",
                                    "-f services/${service}/Dockerfile services/"
                                )
                                image.push()
                                if (env.BRANCH_NAME == 'main') {
                                    image.push('latest')
                                }
                            }
                        }
                    }
                }

                stage('Push Frontend Image') {
                    steps {
                        script {
                            def frontendImage = docker.build(
                                "${DOCKER_REGISTRY}/edams/frontend:${DOCKER_IMAGE_TAG}",
                                "-f edams-web/Dockerfile.frontend edams-web/"
                            )
                            frontendImage.push()
                            if (env.BRANCH_NAME == 'main') {
                                frontendImage.push('latest')
                            }
                        }
                    }
                }
            }
        }

        // =====================================================
        // Stage 11: 部署阶段
        // =====================================================
        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            stages {
                // 11.1 部署测试环境
                stage('Deploy: Test Environment') {
                    when {
                        anyOf {
                            branch 'develop'
                            environment name: 'DEPLOY_TEST', value: 'true'
                        }
                    }
                    steps {
                        script {
                            kubernetesDeploy(
                                configs: 'k8s/test/*.yaml',
                                kubeconfigId: 'test-kubeconfig',
                                enableContextSubstitution: true
                            )

                            sleep 30

                            // 验证部署
                            def testPods = sh(
                                script: "kubectl get pods -n edams-test -l app=edams --no-headers | wc -l",
                                returnStdout: true
                            ).trim()

                            echo "Test environment pods running: ${testPods}"
                        }
                    }
                }

                // 11.2 部署预发布环境
                stage('Deploy: Staging Environment') {
                    when {
                        branch 'main'
                    }
                    steps {
                        input message: 'Deploy to Staging?',
                              submitter: 'devops-team',
                              ok: 'Deploy'

                        script {
                            kubernetesDeploy(
                                configs: 'k8s/staging/*.yaml',
                                kubeconfigId: 'staging-kubeconfig',
                                enableContextSubstitution: true
                            )

                            sleep 60

                            // 健康检查
                            def stagingHealth = sh(
                                script: "curl -sf http://edams-staging.example.com/actuator/health || echo 'UNHEALTHY'",
                                returnStdout: true
                            ).trim()

                            if (stagingHealth != 'UP') {
                                error "Staging health check failed: ${stagingHealth}"
                            }

                            echo "Staging deployment successful!"
                        }
                    }
                }

                // 11.3 蓝绿部署生产环境
                stage('Deploy: Production (Blue-Green)') {
                    when {
                        branch 'main'
                        environment name: 'DEPLOY_PROD', value: 'true'
                    }
                    steps {
                        script {
                            // 准备工作空间
                            sh '''
                                kubectl get namespaces | grep edams-prod || kubectl create namespace edams-prod
                            '''

                            // 蓝绿部署
                            def greenVersion = "v${env.BUILD_NUMBER}"

                            kubernetesDeploy(
                                configs: 'k8s/prod/green-*.yaml',
                                kubeconfigId: 'prod-kubeconfig',
                                enableContextSubstitution: true,
                                substitutions: [
                                    'GREEN_VERSION': greenVersion,
                                    'DOCKER_TAG': env.DOCKER_IMAGE_TAG
                                ]
                            )

                            sleep 90

                            // 验证Green环境
                            def greenPods = sh(
                                script: "kubectl get pods -n edams-prod -l version=green --no-headers | wc -l",
                                returnStdout: true
                            ).trim()

                            if (greenPods.toInteger() < 3) {
                                error "Not enough green pods running: ${greenPods}"
                            }

                            // 执行烟雾测试
                            def smokeResult = sh(
                                script: """
                                    kubectl run smoke-test-${env.BUILD_NUMBER} \
                                        --image=curlimages/curl:latest \
                                        --rm -i --restart=Never \
                                        -- curl -sf http://edams-gateway:8080/actuator/health \
                                        || exit 1
                                """,
                                returnStatus: true
                            )

                            if (smokeResult != 0) {
                                error "Smoke test failed, rolling back..."
                            }

                            // 切换流量到Green
                            sh '''
                                kubectl patch service edams-gateway \
                                    -n edams-prod \
                                    -p '{"spec":{"selector":{"version":"green"}}}'
                            '''

                            sleep 30

                            // 清理Blue环境
                            sh '''
                                kubectl delete deployment -n edams-prod -l version=blue --wait=false
                            '''

                            // 更新Blue环境为新版本
                            kubernetesDeploy(
                                configs: 'k8s/prod/blue-*.yaml',
                                kubeconfigId: 'prod-kubeconfig',
                                enableContextSubstitution: true,
                                substitutions: [
                                    'BLUE_VERSION': greenVersion,
                                    'DOCKER_TAG': env.DOCKER_IMAGE_TAG
                                ]
                            )
                        }
                    }
                }
            }
        }
    }

    // =====================================================
    // Post Actions
    // =====================================================
    post {
        always {
            script {
                // 清理工作空间
                echo "Cleaning workspace..."
                cleanWs()
            }
        }

        success {
            script {
                // 发送成功通知
                if (env.BRANCH_NAME == 'main') {
                    slackSend(
                        channel: env.SLACK_CHANNEL,
                        color: 'good',
                        message: """
                            :white_check_mark: EDAMS Build #${env.BUILD_NUMBER} Successful!
                            *Branch:* ${env.BRANCH_NAME}
                            *Commit:* ${env.GIT_COMMIT[0..7]}
                            *Pipeline:* ${env.BUILD_URL}
                        """
                    )

                    // 发送钉钉通知
                    dingtalk(
                        robot: 'edams-ci',
                        type: 'markdown',
                        title: 'EDAMS CI/CD Pipeline',
                        message: """
                            ## 构建成功 ✅

                            - **项目**: EDAMS
                            - **分支**: ${env.BRANCH_NAME}
                            - **提交**: ${env.GIT_COMMIT[0..7]}
                            - **Build**: #${env.BUILD_NUMBER}
                            - **Pipeline**: [查看流水线](${env.BUILD_URL})
                        """
                    )
                }
            }
        }

        failure {
            script {
                // 发送失败通知
                slackSend(
                    channel: env.SLACK_CHANNEL,
                    color: 'danger',
                    message: """
                        :x: EDAMS Build #${env.BUILD_NUMBER} Failed!
                        *Branch:* ${env.BRANCH_NAME}
                        *Commit:* ${env.GIT_COMMIT[0..7]}
                        *Pipeline:* ${env.BUILD_URL}
                    """
                )

                dingtalk(
                    robot: 'edams-ci',
                    type: 'markdown',
                    title: 'EDAMS CI/CD Pipeline Failed',
                    message: """
                        ## 构建失败 ❌

                        - **项目**: EDAMS
                        - **分支**: ${env.BRANCH_NAME}
                        - **提交**: ${env.GIT_COMMIT[0..7]}
                        - **Build**: #${env.BUILD_NUMBER}
                        - **Pipeline**: [查看流水线](${env.BUILD_URL})
                    """
                )
            }
        }

        unstable {
            echo "Build marked as unstable."
        }

        cleanup {
            echo "Pipeline cleanup completed."
        }
    }
}
