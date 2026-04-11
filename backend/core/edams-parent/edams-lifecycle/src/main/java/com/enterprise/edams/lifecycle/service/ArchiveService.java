package com.enterprise.edams.lifecycle.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;

/**
 * 归档服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ArchiveService {

    /**
     * 创建归档记录
     */
    ArchiveRecord createArchiveRecord(ArchiveRecord archiveRecord);

    /**
     * 更新归档记录
     */
    ArchiveRecord updateArchiveRecord(Long id, ArchiveRecord archiveRecord);

    /**
     * 获取归档记录
     */
    ArchiveRecord getArchiveRecord(Long id);

    /**
     * 删除归档记录
     */
    void deleteArchiveRecord(Long id);

    /**
     * 分页查询归档记录
     */
    IPage<ArchiveRecord> listArchiveRecords(Integer pageNum, Integer pageSize);

    /**
     * 根据数据资产ID查询归档记录
     */
    IPage<ArchiveRecord> listArchiveRecordsByDataAssetId(Long dataAssetId, Integer pageNum, Integer pageSize);

    /**
     * 根据归档状态查询归档记录
     */
    IPage<ArchiveRecord> listArchiveRecordsByStatus(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据还原状态查询归档记录
     */
    IPage<ArchiveRecord> listArchiveRecordsByRestoreStatus(Integer status, Integer pageNum, Integer pageSize);

    /**
     * 归档数据资产
     */
    ArchiveRecord archiveDataAsset(Long dataAssetId, String assetName, 
                                   Integer archiveType, String operator);

    /**
     * 还原归档数据资产
     */
    ArchiveRecord restoreDataAsset(Long archiveRecordId, String operator);

    /**
     * 更新归档状态
     */
    ArchiveRecord updateArchiveStatus(Long id, Integer status);
}