package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsFrameworkRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsFrameworkResponse;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsFramework;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import com.enterprise.dataplatform.ethics.repository.EthicsFrameworkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EthicsFrameworkService {

    private final EthicsFrameworkRepository frameworkRepository;

    @Transactional
    public EthicsFrameworkResponse createFramework(EthicsFrameworkRequest request, String creator) {
        log.info("创建伦理框架: {}, 创建人: {}", request.getFrameworkCode(), creator);

        if (frameworkRepository.existsByFrameworkCode(request.getFrameworkCode())) {
            throw new IllegalArgumentException("框架编码已存在: " + request.getFrameworkCode());
        }

        EthicsFramework framework = EthicsFramework.builder()
                .frameworkCode(request.getFrameworkCode())
                .frameworkName(request.getFrameworkName())
                .description(request.getDescription())
                .principles(request.getPrinciples())
                .riskThreshold(request.getRiskThreshold() != null ?
                        EthicsLevel.valueOf(request.getRiskThreshold()) : null)
                .category(request.getCategory())
                .source(request.getSource())
                .tags(request.getTags())
                .status("DRAFT")
                .enabled(true)
                .version(1)
                .creator(creator)
                .build();

        framework = frameworkRepository.save(framework);

        log.info("伦理框架创建成功: {}", framework.getId());
        return EthicsFrameworkResponse.fromEntity(framework);
    }

    @Transactional
    public EthicsFrameworkResponse updateFramework(Long id, EthicsFrameworkRequest request, String updater) {
        log.info("更新伦理框架: {}, 更新人: {}", id, updater);

        EthicsFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("框架不存在: " + id));

        framework.setFrameworkName(request.getFrameworkName());
        framework.setDescription(request.getDescription());
        framework.setPrinciples(request.getPrinciples());
        if (request.getRiskThreshold() != null) {
            framework.setRiskThreshold(EthicsLevel.valueOf(request.getRiskThreshold()));
        }
        framework.setCategory(request.getCategory());
        framework.setSource(request.getSource());
        framework.setTags(request.getTags());
        framework.setVersion(framework.getVersion() + 1);
        framework.setUpdater(updater);

        framework = frameworkRepository.save(framework);

        log.info("伦理框架更新成功: {}", id);
        return EthicsFrameworkResponse.fromEntity(framework);
    }

    @Transactional
    public EthicsFrameworkResponse publishFramework(Long id, String publisher) {
        log.info("发布伦理框架: {}, 发布人: {}", id, publisher);

        EthicsFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("框架不存在: " + id));

        framework.setStatus("ACTIVE");
        framework.setEnabled(true);
        framework.setUpdater(publisher);

        framework = frameworkRepository.save(framework);

        log.info("伦理框架发布成功: {}", id);
        return EthicsFrameworkResponse.fromEntity(framework);
    }

    @Transactional
    public EthicsFrameworkResponse archiveFramework(Long id, String archiver) {
        log.info("归档伦理框架: {}, 操作人: {}", id, archiver);

        EthicsFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("框架不存在: " + id));

        framework.setStatus("ARCHIVED");
        framework.setEnabled(false);
        framework.setUpdater(archiver);

        framework = frameworkRepository.save(framework);

        log.info("伦理框架归档成功: {}", id);
        return EthicsFrameworkResponse.fromEntity(framework);
    }

    public EthicsFrameworkResponse getFramework(Long id) {
        EthicsFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("框架不存在: " + id));
        return EthicsFrameworkResponse.fromEntity(framework);
    }

    public EthicsFrameworkResponse getFrameworkByCode(String frameworkCode) {
        EthicsFramework framework = frameworkRepository.findByFrameworkCode(frameworkCode)
                .orElseThrow(() -> new IllegalArgumentException("框架不存在: " + frameworkCode));
        return EthicsFrameworkResponse.fromEntity(framework);
    }

    public Page<EthicsFrameworkResponse> searchFrameworks(
            String status, String category, Boolean enabled, String keyword, Pageable pageable) {
        return frameworkRepository.searchFrameworks(status, category, enabled, keyword, pageable)
                .map(EthicsFrameworkResponse::fromEntity);
    }

    public List<EthicsFrameworkResponse> getActiveFrameworks() {
        return frameworkRepository.findByEnabled(true).stream()
                .map(EthicsFrameworkResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EthicsFrameworkResponse> getFrameworksByCategory(String category) {
        return frameworkRepository.searchFrameworks(null, category, null, null, Pageable.unpaged())
                .getContent()
                .stream()
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFramework(Long id) {
        log.info("删除伦理框架: {}", id);
        frameworkRepository.deleteById(id);
    }

    public long countByStatus(String status) {
        return frameworkRepository.countByStatus(status);
    }
}
