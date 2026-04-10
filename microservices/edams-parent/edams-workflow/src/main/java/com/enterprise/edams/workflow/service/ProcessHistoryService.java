package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.workflow.dto.ProcessHistoryDTO;
import com.enterprise.edams.workflow.entity.ProcessHistory;

import java.util.List;

/**
 * 流程历史记录服务接口
 * 
 * @author EDAMS Team
 */
public interface ProcessHistoryService extends IService<ProcessHistory> {

    /**
     * 查询流程实例的历史记录
     */
    List<ProcessHistoryDTO> getProcessHistory(String processInstanceId);

    /**
     * 查询节点的历史记录
     */
    List<ProcessHistoryDTO> getNodeHistory(String processInstanceId, String nodeId);

    /**
     * 记录操作历史
     */
    void recordHistory(ProcessHistory history);
}
