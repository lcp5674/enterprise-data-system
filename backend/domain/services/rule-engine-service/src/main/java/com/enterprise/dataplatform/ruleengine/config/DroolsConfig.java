package com.enterprise.dataplatform.ruleengine.config;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Drools KIE容器配置
 * 支持从classpath和外部文件系统加载规则文件，支持热重载
 */
@Configuration
public class DroolsConfig {

    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);

    @Value("${drools.rules.paths:classpath:rules/}")
    private String rulesPath;

    private final AtomicReference<KieContainer> kieContainerRef = new AtomicReference<>();

    @Bean
    public KieServices kieServices() {
        return KieServices.Factory.get();
    }

    @Bean
    public KieFileSystem kieFileSystem(KieServices kieServices) {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // 从kmodule.xml加载规则（META-INF/kmodule.xml中定义的packages）
        kieFileSystem.write(ResourceFactory.newClassPathResource("META-INF/kmodule.xml"));

        return kieFileSystem;
    }

    @Bean
    public KieContainer kieContainer(KieServices kieServices, KieFileSystem kieFileSystem) {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("Drools规则编译错误: " + kieBuilder.getResults().toString());
        }

        KieRepository kieRepository = kieServices.getRepository();
        KieModule kieModule = kieRepository.getDefaultKieModule();
        KieContainer container = kieServices.newKieContainer(kieModule.getReleaseId());

        kieContainerRef.set(container);
        logger.info("Drools KIE容器初始化成功，ReleaseId: {}", kieModule.getReleaseId());

        return container;
    }

    /**
     * 获取质量评分规则会话
     */
    @Bean(name = "qualitySession")
    public KieSession qualitySession(KieContainer kieContainer) {
        KieSession session = kieContainer.newKieSession("qualitySession");
        logger.info("质量评分规则会话创建成功");
        return session;
    }

    /**
     * 获取合规检查规则会话
     */
    @Bean(name = "complianceSession")
    public KieSession complianceSession(KieContainer kieContainer) {
        return kieContainer.newKieSession("complianceSession");
    }

    /**
     * 获取价值评估规则会话
     */
    @Bean(name = "valueSession")
    public KieSession valueSession(KieContainer kieContainer) {
        return kieContainer.newKieSession("valueSession");
    }

    /**
     * 获取生命周期规则会话
     */
    @Bean(name = "lifecycleSession")
    public KieSession lifecycleSession(KieContainer kieContainer) {
        return kieContainer.newKieSession("lifecycleSession");
    }

    /**
     * 获取治理规则会话
     */
    @Bean(name = "governanceSession")
    public KieSession governanceSession(KieContainer kieContainer) {
        return kieContainer.newKieSession("governanceSession");
    }

    /**
     * 重新加载所有规则
     * 清除现有KIE容器，重建规则引擎
     */
    public void reloadRules(KieServices kieServices) {
        try {
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write(ResourceFactory.newClassPathResource("META-INF/kmodule.xml"));

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                logger.error("Drools规则重载编译错误: {}", kieBuilder.getResults().toString());
                throw new RuntimeException("规则重载失败: 编译错误");
            }

            KieRepository kieRepository = kieServices.getRepository();
            KieModule kieModule = kieRepository.getDefaultKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            kieContainerRef.set(newContainer);
            logger.info("Drools规则重载成功，新ReleaseId: {}", kieModule.getReleaseId());
        } catch (Exception e) {
            logger.error("Drools规则重载失败", e);
            throw new RuntimeException("规则重载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取当前KIE容器引用
     */
    public KieContainer getCurrentKieContainer() {
        return kieContainerRef.get();
    }
}
