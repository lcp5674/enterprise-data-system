package com.edams.version.repository;

import com.edams.version.entity.Version;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface VersionMapper {

    @Insert("INSERT INTO t_version(asset_id,asset_type,version_no,description,content_hash,creator_id,status,create_time) " +
            "VALUES(#{assetId},#{assetType},#{versionNo},#{description},#{contentHash},#{creatorId},#{status},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Version version);

    @Select("SELECT * FROM t_version WHERE asset_id=#{assetId} ORDER BY create_time DESC LIMIT #{offset},#{size}")
    List<Version> findByAssetId(@Param("assetId") String assetId, @Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM t_version WHERE id=#{id}")
    Version findById(Long id);

    @Select("SELECT * FROM t_version WHERE asset_id=#{assetId} AND status='ACTIVE' ORDER BY create_time DESC LIMIT 1")
    Version findLatestByAsset(String assetId);

    @Update("UPDATE t_version SET status='HISTORICAL' WHERE asset_id=#{assetId}")
    int markAllHistorical(String assetId);

    @Update("UPDATE t_version SET status='ACTIVE' WHERE id=#{id}")
    int markAsCurrent(Long id);

    @Delete("DELETE FROM t_version WHERE id=#{id}")
    int deleteById(Long id);
}
