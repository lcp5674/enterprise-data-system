package com.enterprise.edams.datasource.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.datasource.dto.DatasourceQueryDTO;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据源配置Mapper接口
 */
@Mapper
public interface DatasourceConfigRepository extends BaseMapper<DatasourceConfig> {

    /**
     * 分页查询数据源配置
     *
     * @param page   分页参数
     * @param query  查询条件
     * @return 分页结果
     */
    IPage<DatasourceConfig> selectPageList(Page<DatasourceConfig> page, @Param("query") DatasourceQueryDTO query);

    /**
     * 根据数据源编码查询
     *
     * @param code 数据源编码
     * @return 数据源配置
     */
    DatasourceConfig selectByCode(@Param("code") String code);

    /**
     * 根据数据源编码列表查询
     *
     * @param codes 数据源编码列表
     * @return 数据源配置列表
     */
    List<DatasourceConfig> selectByCodes(@Param("codes") List<String> codes);

    /**
     * 查询启用的数据源
     *
     * @return 数据源配置列表
     */
    List<DatasourceConfig> selectActiveDatasources();

    /**
     * 查询需要同步的数据源
     *
     * @return 数据源配置列表
     */
    List<DatasourceConfig> selectDatasourcesNeedSync();

    /**
     * 根据类型统计数量
     *
     * @param datasourceType 数据源类型
     * @return 数量
     */
    Long countByType(@Param("datasourceType") String datasourceType);

    /**
     * 根据状态统计数量
     *
     * @param status 状态
     * @return 数量
     */
    Long countByStatus(@Param("status") String status);

    /**
     * 根据健康状态统计数量
     *
     * @param healthStatus 健康状态
     * @return 数量
     */
    Long countByHealthStatus(@Param("healthStatus") String healthStatus);
}
