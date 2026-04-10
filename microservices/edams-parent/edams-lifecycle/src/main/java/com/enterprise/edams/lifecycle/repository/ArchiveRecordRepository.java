package com.enterprise.edams.lifecycle.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 归档记录数据访问层
 * 
 * @author EDAMS Team
 */
@Mapper
public interface ArchiveRecordRepository extends BaseMapper<ArchiveRecord> {

    /**
     * 根据业务ID查询归档记录
     */
    @Select("SELECT * FROM lc_archive_record WHERE business_id = #{businessId} AND deleted = false ORDER BY created_time DESC")
    List<ArchiveRecord> findByBusinessId(@Param("businessId") String businessId);

    /**
     * 根据策略ID查询归档记录
     */
    @Select("SELECT * FROM lc_archive_record WHERE policy_id = #{policyId} AND deleted = false ORDER BY created_time DESC")
    List<ArchiveRecord> findByPolicyId(@Param("policyId") String policyId);

    /**
     * 查询待归档的记录
     */
    @Select("SELECT * FROM lc_archive_record WHERE status = 0 AND deleted = false")
    List<ArchiveRecord> findPendingArchives();

    /**
     * 查询归档失败的记录
     */
    @Select("SELECT * FROM lc_archive_record WHERE status = 3 AND deleted = false")
    List<ArchiveRecord> findFailedArchives();
}
