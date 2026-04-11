package com.enterprise.edams.lifecycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;
import com.enterprise.edams.lifecycle.repository.ArchiveRecordMapper;
import com.enterprise.edams.lifecycle.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 归档服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveRecordMapper archiveRecordMapper;

    /**
     * 创建归档记录
     */
    @Override
    @Transactional
    public ArchiveRecord createArchiveRecord(ArchiveRecord archiveRecord) {
        archiveRecord.setArchiveTime(LocalDateTime.now());
        archiveRecord.setArchiveStatus(1);
        archiveRecord.setRestoreStatus(0);
        archiveRecordMapper.insert(archiveRecord);
        return archiveRecord;
    }

    /**
     * 更新归档记录
     */
    @Override
    @Transactional
    public ArchiveRecord updateArchiveRecord(Long id, ArchiveRecord archiveRecord) {
        ArchiveRecord existing = archiveRecordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("归档记录不存在");
        }

        archiveRecordMapper.updateById(archiveRecord);
        return archiveRecordMapper.selectById(id);
    }

    /**
     * 获取归档记录
     */
    @Override
    public ArchiveRecord getArchiveRecord(Long id) {
        return archiveRecordMapper.selectById(id);
    }

    /**
     * 删除归档记录
     */
    @Override
    @Transactional
    public void deleteArchiveRecord(Long id) {
        archiveRecordMapper.deleteById(id);
    }

    /**
     * 分页查询归档记录
     */
    @Override
    public IPage<ArchiveRecord> listArchiveRecords(Integer pageNum, Integer pageSize) {
        Page<ArchiveRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ArchiveRecord> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("archive_time");
        return archiveRecordMapper.selectPage(page, wrapper);
    }

    /**
     * 根据数据资产ID查询归档记录
     */
    @Override
    public IPage<ArchiveRecord> listArchiveRecordsByDataAssetId(Long dataAssetId, Integer pageNum, Integer pageSize) {
        Page<ArchiveRecord> page = new Page<>(pageNum, pageSize);
        return archiveRecordMapper.findByDataAssetId(page, dataAssetId);
    }

    /**
     * 根据归档状态查询归档记录
     */
    @Override
    public IPage<ArchiveRecord> listArchiveRecordsByStatus(Integer status, Integer pageNum, Integer pageSize) {
        Page<ArchiveRecord> page = new Page<>(pageNum, pageSize);
        return archiveRecordMapper.findByArchiveStatus(page, status);
    }

    /**
     * 根据还原状态查询归档记录
     */
    @Override
    public IPage<ArchiveRecord> listArchiveRecordsByRestoreStatus(Integer status, Integer pageNum, Integer pageSize) {
        Page<ArchiveRecord> page = new Page<>(pageNum, pageSize);
        return archiveRecordMapper.findByRestoreStatus(page, status);
    }

    /**
     * 归档数据资产
     */
    @Override
    @Transactional
    public ArchiveRecord archiveDataAsset(Long dataAssetId, String assetName, Integer archiveType, String operator) {
        ArchiveRecord archiveRecord = new ArchiveRecord();
        archiveRecord.setDataAssetId(dataAssetId);
        archiveRecord.setDataAssetName(assetName);
        archiveRecord.setArchiveType(archiveType);
        archiveRecord.setArchiveOperator(operator);
        archiveRecord.setArchiveTime(LocalDateTime.now());
        archiveRecord.setArchiveStatus(1);
        archiveRecord.setRestoreStatus(0);
        archiveRecord.setArchiveStorageType(1); // 默认对象存储
        archiveRecordMapper.insert(archiveRecord);
        return archiveRecord;
    }

    /**
     * 还原归档数据资产
     */
    @Override
    @Transactional
    public ArchiveRecord restoreDataAsset(Long archiveRecordId, String operator) {
        ArchiveRecord archiveRecord = archiveRecordMapper.selectById(archiveRecordId);
        if (archiveRecord == null) {
            throw new BusinessException("归档记录不存在");
        }

        if (archiveRecord.getRestoreStatus() == 1) {
            throw new BusinessException("数据已还原");
        }

        archiveRecord.setRestoreStatus(1);
        archiveRecord.setRestoreTime(LocalDateTime.now());
        archiveRecord.setRestoreOperator(operator);
        archiveRecordMapper.updateById(archiveRecord);
        return archiveRecord;
    }

    /**
     * 更新归档状态
     */
    @Override
    @Transactional
    public ArchiveRecord updateArchiveStatus(Long id, Integer status) {
        ArchiveRecord archiveRecord = archiveRecordMapper.selectById(id);
        if (archiveRecord == null) {
            throw new BusinessException("归档记录不存在");
        }

        archiveRecord.setArchiveStatus(status);
        archiveRecordMapper.updateById(archiveRecord);
        return archiveRecord;
    }
}