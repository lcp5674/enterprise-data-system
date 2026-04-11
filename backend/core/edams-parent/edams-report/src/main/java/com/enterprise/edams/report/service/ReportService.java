package com.enterprise.edams.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.report.dto.ReportCreateRequest;
import com.enterprise.edams.report.dto.ReportDTO;

import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ReportService {

    /**
     * 创建报表
     */
    ReportDTO createReport(ReportCreateRequest request, Long creatorId, String creatorName);

    /**
     * 更新报表
     */
    ReportDTO updateReport(Long id, ReportCreateRequest request);

    /**
     * 删除报表
     */
    void deleteReport(Long id);

    /**
     * 获取报表详情
     */
    ReportDTO getReportById(Long id);

    /**
     * 根据编码获取报表
     */
    ReportDTO getReportByCode(String reportCode);

    /**
     * 分页查询报表
     */
    IPage<ReportDTO> queryReports(String keyword, String reportType, Integer status, int pageNum, int pageSize);

    /**
     * 根据类型查询报表
     */
    List<ReportDTO> getReportsByType(String reportType);

    /**
     * 获取用户的报表
     */
    List<ReportDTO> getReportsByCreator(Long creatorId);

    /**
     * 根据模板查询报表
     */
    List<ReportDTO> getReportsByTemplate(Long templateId);

    /**
     * 启用报表
     */
    void enableReport(Long id);

    /**
     * 禁用报表
     */
    void disableReport(Long id);

    /**
     * 生成报表
     */
    byte[] generateReport(Long id, Map<String, Object> params);

    /**
     * 导出报表
     */
    byte[] exportReport(Long id, String format, Map<String, Object> params);

    /**
     * 预览报表
     */
    String previewReport(Long id);

    /**
     * 执行报表
     */
    Map<String, Object> executeReport(Long id);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 克隆报表
     */
    ReportDTO cloneReport(Long id, String newReportName, Long creatorId, String creatorName);

    /**
     * 统计报表总数
     */
    long countTotalReports();

    /**
     * 统计各状态报表数量
     */
    long countByStatus(Integer status);
}
