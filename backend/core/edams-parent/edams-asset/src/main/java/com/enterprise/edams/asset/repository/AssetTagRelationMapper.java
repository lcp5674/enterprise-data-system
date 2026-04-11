package com.enterprise.edams.asset.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.asset.entity.AssetTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资产标签关联Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AssetTagRelationMapper extends BaseMapper<AssetTagRelation> {

    /**
     * 根据资产ID查询关联记录
     */
    @Select("SELECT * FROM edams_asset_tag_relation WHERE asset_id = #{assetId} AND deleted = 0")
    List<AssetTagRelation> selectByAssetId(@Param("assetId") Long assetId);

    /**
     * 根据标签ID查询关联记录
     */
    @Select("SELECT * FROM edams_asset_tag_relation WHERE tag_id = #{tagId} AND deleted = 0")
    List<AssetTagRelation> selectByTagId(@Param("tagId") Long tagId);

    /**
     * 删除资产的所有标签关联
     */
    @Select("UPDATE edams_asset_tag_relation SET deleted = 1 WHERE asset_id = #{assetId}")
    int deleteByAssetId(@Param("assetId") Long assetId);

    /**
     * 检查关联是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset_tag_relation WHERE asset_id = #{assetId} AND tag_id = #{tagId} AND deleted = 0")
    int exists(@Param("assetId") Long assetId, @Param("tagId") Long tagId);

    /**
     * 统计资产的标签数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset_tag_relation WHERE asset_id = #{assetId} AND deleted = 0")
    int countByAssetId(@Param("assetId") Long assetId);

    /**
     * 统计标签关联的资产数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset_tag_relation WHERE tag_id = #{tagId} AND deleted = 0")
    int countByTagId(@Param("tagId") Long tagId);
}
