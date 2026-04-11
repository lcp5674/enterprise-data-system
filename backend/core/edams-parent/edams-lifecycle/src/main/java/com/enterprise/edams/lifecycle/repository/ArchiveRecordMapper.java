package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 归档记录Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ArchiveRecordMapper extends BaseMapper<ArchiveRecord> {

    @Select("SELECT * FROM archive_record WHERE data_asset_id = #{dataAssetId} AND deleted = 0")
    IPage<ArchiveRecord> findByDataAssetId(Page<ArchiveRecord> page, @Param("dataAssetId") Long dataAssetти);

    @Select("SELECT * FROM archive_record WHERE archive_status = #{status} AND deleted = 0")
    IPage<ArchiveRecord> findByArchiveStatus(Page<ArchiveRecord> page, @Param("status") Integer status);

    @Select("SELECT * FROM archive_record WHERE restore_status = #{status} AND deleted = 0")
    IPage<ArchiveRecord> findByRestoreStatus(Page<ArchiveRecord> page, @Param("status") Integer status);
}