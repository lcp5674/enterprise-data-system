package com.enterprise.edams.version.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.version.entity.VersionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 版本记录数据访问层
 *
 * @author EDAMS Team
 */
@Mapper
public interface VersionRecordRepository extends BaseMapper<VersionRecord> {

    /**
     * 根据业务查询所有版本
     */
    @Select("SELECT * FROM ver_version_record WHERE business_type = #{businessType} AND business_id = #{businessId} AND deleted = false ORDER BY version DESC")
    List<VersionRecord> findByBusiness(@Param("businessType") String businessType, @Param("businessId") String businessId);

    /**
     * 查询最新版本
     */
    @Select("SELECT * FROM ver_version_record WHERE business_type = #{businessType} AND business_id = #{businessId} AND deleted = false ORDER BY version DESC LIMIT 1")
    VersionRecord findLatestByBusiness(@Param("businessType") String businessType, @Param("businessId") String businessId);

    /**
     * 查询指定版本
     */
    @Select("SELECT * FROM ver_version_record WHERE business_type = #{businessType} AND business_id = #{businessId} AND version = #{version} AND deleted = false")
    VersionRecord findByBusinessAndVersion(@Param("businessType") String businessType, @Param("businessId") String businessId, @Param("version") Integer version);

    /**
     * 查询最大版本号
     */
    @Select("SELECT MAX(version) FROM ver_version_record WHERE business_type = #{businessType} AND business_id = #{businessId} AND deleted = false")
    Integer findMaxVersionByBusiness(@Param("businessType") String businessType, @Param("businessId") String businessId);
}
