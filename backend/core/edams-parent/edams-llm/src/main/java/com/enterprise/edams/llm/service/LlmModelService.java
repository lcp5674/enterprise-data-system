package com.enterprise.edams.llm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.llm.dto.LlmModelDTO;
import com.enterprise.edams.llm.entity.LlmModel;

import java.util.List;

/**
 * 大模型服务接口
 */
public interface LlmModelService extends IService<LlmModel> {

    /**
     * 分页查询模型
     */
    Page<LlmModel> selectPage(LlmModelDTO dto, int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    LlmModelDTO getById(Long id);

    /**
     * 根据模型代码查询
     */
    LlmModelDTO getByCode(String modelCode);

    /**
     * 创建模型
     */
    LlmModelDTO create(LlmModelDTO dto);

    /**
     * 更新模型
     */
    LlmModelDTO update(Long id, LlmModelDTO dto);

    /**
     * 删除模型
     */
    void delete(Long id);

    /**
     * 获取启用的模型列表
     */
    List<LlmModelDTO> getEnabledModels();

    /**
     * 根据提供商获取模型
     */
    List<LlmModelDTO> getByProvider(String provider);

    /**
     * 根据类型获取模型
     */
    List<LlmModelDTO> getByType(String modelType);

    /**
     * 启用/禁用模型
     */
    LlmModelDTO setEnabled(Long id, boolean enabled);

    /**
     * 更新模型状态
     */
    LlmModelDTO updateStatus(Long id, String status);
}
