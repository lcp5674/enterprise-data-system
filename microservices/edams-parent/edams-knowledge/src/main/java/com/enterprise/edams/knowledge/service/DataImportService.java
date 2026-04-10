package com.enterprise.edams.knowledge.service;

import com.enterprise.edams.knowledge.dto.DataImportDTO;
import com.enterprise.edams.knowledge.dto.DataImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据导入服务接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
public interface DataImportService {

    /**
     * 导入数据
     *
     * @param dto 导入配置
     * @return 导入结果
     */
    DataImportResultDTO importData(DataImportDTO dto);

    /**
     * 从文件导入
     *
     * @param graphId 图谱ID
     * @param nodeType 节点类型
     * @param file 文件
     * @return 导入结果
     */
    DataImportResultDTO importFromFile(String graphId, String nodeType, MultipartFile file);

    /**
     * 获取导入状态
     *
     * @param taskId 任务ID
     * @return 导入结果
     */
    DataImportResultDTO getImportStatus(String taskId);

    /**
     * 取消导入任务
     *
     * @param taskId 任务ID
     */
    void cancelImport(String taskId);

    /**
     * 从数据库导入
     *
     * @param graphId 图谱ID
     * @param datasourceId 数据源ID
     * @param sql SQL查询
     * @param nodeType 节点类型
     * @param nameField 名称字段
     * @return 导入结果
     */
    DataImportResultDTO importFromDatabase(String graphId, String datasourceId, String sql, 
                                            String nodeType, String nameField);
}
