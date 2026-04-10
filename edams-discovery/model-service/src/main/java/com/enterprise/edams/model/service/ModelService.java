package com.enterprise.edams.model.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.model.dto.CreateModelRequest;
import com.enterprise.edams.model.dto.ModelQueryDTO;
import com.enterprise.edams.model.dto.UpdateModelRequest;
import com.enterprise.edams.model.vo.ModelDetailVO;
import com.enterprise.edams.model.vo.ModelStatisticsVO;
import com.enterprise.edams.model.vo.ModelVO;

import java.util.List;

/**
 * 数据模型服务接口
 */
public interface ModelService {

    /**
     * 创建数据模型
     */
    Long createModel(CreateModelRequest request);

    /**
     * 更新数据模型
     */
    boolean updateModel(Long id, UpdateModelRequest request);

    /**
     * 删除数据模型
     */
    boolean deleteModel(Long id);

    /**
     * 发布模型
     */
    boolean publishModel(Long id);

    /**
     * 废弃模型
     */
    boolean deprecateModel(Long id);

    /**
     * 获取模型详情
     */
    ModelDetailVO getModelDetail(Long id);

    /**
     * 分页查询模型
     */
    IPage<ModelVO> listModels(ModelQueryDTO query);

    /**
     * 获取模型树
     */
    List<ModelVO> getModelTree();

    /**
     * 获取模型统计
     */
    ModelStatisticsVO getStatistics();

    /**
     * 检查编码唯一性
     */
    boolean isCodeUnique(String code);
}
