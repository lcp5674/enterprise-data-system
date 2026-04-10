package com.enterprise.edams.model.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.model.dto.ModelQueryDTO;
import com.enterprise.edams.model.entity.DataModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据模型Mapper接口
 */
@Mapper
public interface DataModelRepository extends BaseMapper<DataModel> {

    /**
     * 分页查询
     */
    IPage<DataModel> selectPageList(Page<DataModel> page, @Param("query") ModelQueryDTO query);

    /**
     * 根据编码查询
     */
    DataModel selectByCode(@Param("code") String code);

    /**
     * 查询模型树
     */
    List<DataModel> selectModelTree();

    /**
     * 根据父ID查询子模型
     */
    List<DataModel> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 统计类型数量
     */
    Long countByModelType(@Param("modelType") String modelType);

    /**
     * 统计层级数量
     */
    Long countByLevel(@Param("level") String level);
}
