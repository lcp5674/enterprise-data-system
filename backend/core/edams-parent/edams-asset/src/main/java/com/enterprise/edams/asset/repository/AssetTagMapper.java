package com.enterprise.edams.asset.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.asset.entity.AssetTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产标签Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AssetTagMapper extends BaseMapper<AssetTag> {

    /**
     * 根据标签编码查询
     */
    @Select("SELECT * FROM edams_asset_tag WHERE tag_code = #{tagCode} AND deleted = 0 LIMIT 1")
    AssetTag selectByTagCode(@Param("tagCode") String tagCode);

    /**
     * 根据标签名称查询
     */
    @Select("SELECT * FROM edams_asset_tag WHERE tag_name = #{tagName} AND deleted = 0 LIMIT 1")
    AssetTag selectByTagName(@Param("tagName") String tagName);

    /**
     * 检查标签编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset_tag WHERE tag_code = #{tagCode} AND deleted = 0")
    int existsByTagCode(@Param("tagCode") String tagCode);

    /**
     * 检查标签名称是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset_tag WHERE tag_name = #{tagName} AND deleted = 0")
    int existsByTagName(@Param("tagName") String tagName);

    /**
     * 根据分类查询标签列表
     */
    @Select("SELECT * FROM edams_asset_tag WHERE category = #{category} AND deleted = 0 ORDER BY usage_count DESC")
    List<AssetTag> selectByCategory(@Param("category") String category);

    /**
     * 根据状态查询标签列表
     */
    @Select("SELECT * FROM edams_asset_tag WHERE status = #{status} AND deleted = 0 ORDER BY usage_count DESC")
    List<AssetTag> selectByStatus(@Param("status") Integer status);

    /**
     * 查询热门标签(按使用次数排序)
     */
    @Select("SELECT * FROM edams_asset_tag WHERE status = 1 AND deleted = 0 ORDER BY usage_count DESC LIMIT #{limit}")
    List<AssetTag> selectHotTags(@Param("limit") Integer limit);

    /**
     * 模糊搜索标签
     */
    @Select("SELECT * FROM edams_asset_tag WHERE tag_name LIKE CONCAT('%', #{keyword}, '%') AND deleted = 0 AND status = 1 ORDER BY usage_count DESC LIMIT #{limit}")
    List<AssetTag> searchByName(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 增加标签使用次数
     */
    @Update("UPDATE edams_asset_tag SET usage_count = usage_count + 1, last_used_time = #{now} WHERE id = #{tagId}")
    int incrementUsageCount(@Param("tagId") Long tagId, @Param("now") LocalDateTime now);

    /**
     * 减少标签使用次数
     */
    @Update("UPDATE edams_asset_tag SET usage_count = GREATEST(0, usage_count - 1) WHERE id = #{tagId}")
    int decrementUsageCount(@Param("tagId") Long tagId);

    /**
     * 查询资产关联的所有标签
     */
    @Select("SELECT t.* FROM edams_asset_tag t " +
            "INNER JOIN edams_asset_tag_relation atr ON t.id = atr.tag_id " +
            "WHERE atr.asset_id = #{assetId} AND t.deleted = 0 AND t.status = 1 ORDER BY t.usage_count DESC")
    List<AssetTag> selectTagsByAssetId(@Param("assetId") Long assetId);

    /**
     * 查询指定分类的标签数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset_tag WHERE category = #{category} AND deleted = 0")
    int countByCategory(@Param("category") String category);
}
