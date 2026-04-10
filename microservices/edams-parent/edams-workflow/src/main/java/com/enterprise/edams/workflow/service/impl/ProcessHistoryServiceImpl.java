package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.workflow.dto.ProcessHistoryDTO;
import com.enterprise.edams.workflow.entity.ProcessHistory;
import com.enterprise.edams.workflow.repository.ProcessHistoryRepository;
import com.enterprise.edams.workflow.service.ProcessHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程历史记录服务实现
 *
 * @author EDAMS Team
 */
@Service
@RequiredArgsConstructor
public class ProcessHistoryServiceImpl extends ServiceImpl<ProcessHistoryRepository, ProcessHistory>
        implements ProcessHistoryService {

    private final ProcessHistoryRepository processHistoryRepository;

    @Override
    public List<ProcessHistoryDTO> getProcessHistory(String processInstanceId) {
        List<ProcessHistory> histories = processHistoryRepository.findByProcessInstanceId(processInstanceId);
        return histories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessHistoryDTO> getNodeHistory(String processInstanceId, String nodeId) {
        List<ProcessHistory> histories = processHistoryRepository.findByProcessInstanceIdAndNodeId(processInstanceId, nodeId);
        return histories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void recordHistory(ProcessHistory history) {
        save(history);
    }

    private ProcessHistoryDTO convertToDTO(ProcessHistory history) {
        ProcessHistoryDTO dto = new ProcessHistoryDTO();
        BeanUtils.copyProperties(history, dto);
        return dto;
    }
}
