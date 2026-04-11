package com.enterprise.edams.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.llm.dto.LlmModelDTO;
import com.enterprise.edams.llm.entity.LlmModel;
import com.enterprise.edams.llm.repository.LlmModelMapper;
import com.enterprise.edams.llm.service.LlmModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 大模型服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmModelServiceImpl extends ServiceImpl<LlmModelMapper, LlmModel> implements LlmModelService {

    @Override
    public Page<LlmModel> selectPage(LlmModelDTO dto, int pageNum, int pageSize) {
        Page<LlmModel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LlmModel> wrapper = new LambdaQueryWrapper<>();
        if (dto.getModelCode() != null) {
            wrapper.like(LlmModel::getModelCode, dto.getModelCode());
        }
        if (dto.getModelName() != null) {
            wrapper.like(LlmModel::getModelName, dto.getModelName());
        }
        if (dto.getProvider() != null) {
            wrapper.eq(LlmModel::getProvider, dto.getProvider());
        }
        if (dto.getModelType() != null) {
            wrapper.eq(LlmModel::getModelType, dto.getModelType());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(LlmModel::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(LlmModel::getPriority);
        return page(page, wrapper);
    }

    @Override
    public LlmModelDTO getById(Long id) {
        LlmModel model = baseMapper.selectById(id);
        if (model == null) {
            return null;
        }
        return convertToDTO(model);
    }

    @Override
    public LlmModelDTO getByCode(String modelCode) {
        LlmModel model = baseMapper.selectByCode(modelCode);
        if (model == null) {
            return null;
        }
        return convertToDTO(model);
    }

    @Override
    @Transactional
    public LlmModelDTO create(LlmModelDTO dto) {
        LlmModel model = new LlmModel();
        BeanUtils.copyProperties(dto, model);
        model.setEnabled(true);
        model.setStatus("ACTIVE");
        baseMapper.insert(model);
        log.info("Created LLM model: {}", model.getId());
        return convertToDTO(model);
    }

    @Override
    @Transactional
    public LlmModelDTO update(Long id, LlmModelDTO dto) {
        LlmModel model = baseMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("Model not found: " + id);
        }
        BeanUtils.copyProperties(dto, model, "id", "createTime");
        baseMapper.updateById(model);
        log.info("Updated LLM model: {}", id);
        return convertToDTO(model);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
        log.info("Deleted LLM model: {}", id);
    }

    @Override
    public List<LlmModelDTO> getEnabledModels() {
        List<LlmModel> models = baseMapper.selectEnabledModels();
        return models.stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<LlmModelDTO> getByProvider(String provider) {
        List<LlmModel> models = baseMapper.selectByProvider(provider);
        return models.stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<LlmModelDTO> getByType(String modelType) {
        List<LlmModel> models = baseMapper.selectByType(modelType);
        return models.stream().map(this::convertToDTO).toList();
    }

    @Override
    @Transactional
    public LlmModelDTO setEnabled(Long id, boolean enabled) {
        LlmModel model = baseMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("Model not found: " + id);
        }
        model.setEnabled(enabled);
        baseMapper.updateById(model);
        log.info("Set model {} enabled: {}", id, enabled);
        return convertToDTO(model);
    }

    @Override
    @Transactional
    public LlmModelDTO updateStatus(Long id, String status) {
        LlmModel model = baseMapper.selectById(id);
        if (model == null) {
            throw new RuntimeException("Model not found: " + id);
        }
        model.setStatus(status);
        baseMapper.updateById(model);
        log.info("Updated model {} status: {}", id, status);
        return convertToDTO(model);
    }

    private LlmModelDTO convertToDTO(LlmModel model) {
        LlmModelDTO dto = new LlmModelDTO();
        BeanUtils.copyProperties(model, dto);
        return dto;
    }
}
