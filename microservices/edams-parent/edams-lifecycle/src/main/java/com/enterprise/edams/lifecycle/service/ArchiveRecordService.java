package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.lifecycle.dto.ArchiveRecordDTO;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;

import java.util.List;

/**
 * 归档记录服务接口
 * 
 * @author EDAMS Team
 */
public interface ArchiveRecordService extends IService<ArchiveRecord> {

    /**
     * 创建归档记录
     */
    ArchiveRecordDTO createArchiveRecord(ArchiveRecord record);

    /**
     * 获取归档记录详情
     */
    ArchiveRecordDTO getArchiveRecord(String id);

    /**
     * 分页查询归档记录
     */
    Page<ArchiveRecordDTO> listArchiveRecords(Page<ArchiveRecord> page, String policyId, String businessType, Integer status);

    /**
     * 根据业务ID查询归档记录
     */
    List<ArchiveRecordDTO> getArchiveRecordsByBusinessId(String businessId);

    /**
     * 恢复归档数据
     */
    void restoreArchive(String id, String operatorId, String operatorName);

    /**
     * 删除归档记录
     */
    void deleteArchiveRecord(String id);

    /**
     * 下载归档文件
     */
    byte[] downloadArchiveFile(String id);
}
