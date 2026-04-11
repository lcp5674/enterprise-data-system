package com.enterprise.edams.analytics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.analytics.dto.AnalyticsReportCreateRequest;
import com.enterprise.edams.analytics.dto.AnalyticsReportDTO;

import java.util.List;
import java.util.Map;

/**
 * 分析报告服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AnalyticsReportService {

    /**
     * 创建报告
     */
    AnalyticsReportDTO createReport(AnalyticsReportCreateRequest request, Long creatorId, String creatorName);

    /**
     * 更新报告
     */
    AnalyticsReportDTO updateReport(Long id, AnalyticsReportCreateRequest request);

    /**
     * 删除报告
     */
    void deleteReport(Long id);

    /**
     * 获取报告详情
     */
    AnalyticsReportDTO getReportById(Long id);

    /**
     * 根据编码获取报告
     */
    AnalyticsReportDTO getReportByCode(String reportCode);

    /**
     * 分页查询报告列表
     */
    IPage<AnalyticsReportDTO> queryReports(String keyword, String reportType, Integer status, 
                                             String accessLevel, int pageNum, int pageSize);

    /**
     * 根据类型查询报告
     */
    List<AnalyticsReportDTO> getReportsByType(String reportType);

    /**
     * 获取用户的报告列表
     */
    List<AnalyticsReportDTO> getReportsByCreator(Long creatorId);

    /**
     * 发布报告
     */
    void publishReport(Long id);

    /**
     * 归档报告
     */
    void archiveReport(Long id);

    /**
     * 执行报告
     */
    Map<String, Object> executeReport(Long id);

    /**
     * 获取报告数据
     */
    List<Map<String, Object>> getReportData(Long id);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 克隆报告
     */
    AnalyticsReportDTO cloneReport(Long id, String newReportName, Long creatorId, String creatorName);

    /**
     * 统计报告总数
     */
    long countTotalReports();

    /**
     * 统计各状态报告数量
     */
    long countByStatus(Integer status);
}
