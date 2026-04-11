package com.enterprise.edams.analytics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.analytics.dto.DashboardConfigCreateRequest;
import com.enterprise.edams.analytics.dto.DashboardConfigDTO;

import java.util.List;

/**
 * 仪表盘配置服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface DashboardConfigService {

    /**
     * 创建仪表盘
     */
    DashboardConfigDTO createDashboard(DashboardConfigCreateRequest request, Long creatorId, String creatorName);

    /**
     * 更新仪表盘
     */
    DashboardConfigDTO updateDashboard(Long id, DashboardConfigCreateRequest request);

    /**
     * 删除仪表盘
     */
    void deleteDashboard(Long id);

    /**
     * 获取仪表盘详情
     */
    DashboardConfigDTO getDashboardById(Long id);

    /**
     * 根据编码获取仪表盘
     */
    DashboardConfigDTO getDashboardByCode(String dashboardCode);

    /**
     * 分页查询仪表盘
     */
    IPage<DashboardConfigDTO> queryDashboards(String keyword, String dashboardType, Integer status, 
                                               int pageNum, int pageSize);

    /**
     * 根据类型获取仪表盘
     */
    List<DashboardConfigDTO> getDashboardsByType(String dashboardType);

    /**
     * 获取用户的仪表盘
     */
    List<DashboardConfigDTO> getDashboardsByUser(Long userId);

    /**
     * 获取系统默认仪表盘
     */
    DashboardConfigDTO getDefaultDashboard();

    /**
     * 获取用户默认仪表盘
     */
    DashboardConfigDTO getUserDefaultDashboard(Long userId);

    /**
     * 设置默认仪表盘
     */
    void setAsDefault(Long id);

    /**
     * 启用仪表盘
     */
    void enableDashboard(Long id);

    /**
     * 禁用仪表盘
     */
    void disableDashboard(Long id);

    /**
     * 记录访问
     */
    void recordAccess(Long id);

    /**
     * 克隆仪表盘
     */
    DashboardConfigDTO cloneDashboard(Long id, String newDashboardName, Long userId);

    /**
     * 统计仪表盘总数
     */
    long countTotalDashboards();
}
