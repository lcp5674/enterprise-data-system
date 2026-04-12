package com.edams.watermark.repository;

import com.edams.watermark.entity.Watermark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WatermarkMapper {

    @Insert("INSERT INTO t_watermark(asset_id,asset_type,watermark_code,watermark_type,owner_id,owner_name,status,embed_time,create_time) " +
            "VALUES(#{assetId},#{assetType},#{watermarkCode},#{watermarkType},#{ownerId},#{ownerName},#{status},#{embedTime},#{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Watermark watermark);

    @Select("<script>SELECT * FROM t_watermark WHERE 1=1 " +
            "<if test='assetId != null'>AND asset_id=#{assetId}</if> " +
            "ORDER BY create_time DESC LIMIT #{offset},#{size}</script>")
    List<Watermark> findAll(@Param("assetId") String assetId, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>SELECT COUNT(*) FROM t_watermark WHERE 1=1 " +
            "<if test='assetId != null'>AND asset_id=#{assetId}</if></script>")
    long count(@Param("assetId") String assetId);

    @Select("SELECT * FROM t_watermark WHERE id=#{id}")
    Watermark findById(Long id);

    @Select("SELECT * FROM t_watermark WHERE asset_id=#{assetId} AND status='ACTIVE' LIMIT 1")
    Watermark findByAssetId(String assetId);

    @Select("SELECT * FROM t_watermark WHERE watermark_code=#{code} LIMIT 1")
    Watermark findByCode(String code);

    @Delete("DELETE FROM t_watermark WHERE id=#{id}")
    int deleteById(Long id);
}
