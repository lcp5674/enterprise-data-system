package com.enterprise.edams.datasource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.datasource.dto.*;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import com.enterprise.edams.datasource.vo.DatasourceDetailVO;
import com.enterprise.edams.datasource.vo.DatasourceStatisticsVO;
import com.enterprise.edams.datasource.vo.DatasourceVO;

import java.util.List;

/**
 * 数据源配置服务接口
 */
public interface DatasourceConfigService {

    /**
     * 创建数据源配置
     *
     * @param request 创建请求
     * @return 创建的数据源ID
     */
    Long createDatasource(CreateDatasourceRequest request);

    /**
     * 更新数据源配置
     *
     * @param id      数据源ID
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateDatasource(Long id, UpdateDatasourceRequest request);

    /**
     * 删除数据源配置
     *
     * @param id 数据源ID
     * @return 是否成功
     */
    boolean deleteDatasource(Long id);

    /**
     * 获取数据源详情
     *
     * @param id 数据源ID
     * @return 数据源详情VO
     */
    DatasourceDetailVO getDatasourceDetail(Long id);

    /**
     * 分页查询数据源列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<DatasourceVO> listDatasources(DatasourceQueryDTO query);

    /**
     * 启用数据源
     *
     * @param id 数据源ID
     * @return 是否成功
     */
    boolean enableDatasource(Long id);

    /**
     * 禁用数据源
     *
     * @param id 数据源ID
     * @return 是否成功
     */
    boolean disableDatasource(Long id);

    /**
     * 测试数据源连接
     *
     * @param request 连接测试请求
     * @return 测试结果
     */
    ConnectionTestResponse testConnection(ConnectionTestRequest request);

    /**
     * 测试已存在的数据源连接
     *
     * @param id 数据源ID
     * @return 测试结果
     */
    ConnectionTestResponse testDatasourceConnection(Long id);

    /**
     * 获取数据源统计信息
     *
     * @return 统计信息
     */
    DatasourceStatisticsVO getStatistics();

    /**
     * 根据数据源编码获取数据源
     *
     * @param code 数据源编码
     * @return 数据源配置
     */
    DatasourceConfig getByCode(String code);

    /**
     * 批量获取数据源
     *
     * @param codes 数据源编码列表
     * @return 数据源列表
     */
    List<DatasourceConfig> listByCodes(List<String> codes);

    /**
     * 验证数据源编码是否唯一
     *
     * @param code 数据源编码
     * @return 是否唯一
     */
    boolean isCodeUnique(String code);
}
