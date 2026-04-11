package com.enterprise.edams.asset.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.asset.entity.AssetCatalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资产目录Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AssetCatalogMapper extends BaseMapper<AssetCatalog> {

    /**
     * 根据父目录ID查询子目录列表
     */
    @Select("SELECT * FROM edams_asset_catalog WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC, id ASC")
    List<AssetCatalog> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据目录编码查询
     */
    @Select("SELECT * FROM edams_asset_catalog WHERE code = #{code} AND deleted = 0 LIMIT 1")
    AssetCatalog selectByCode(@Param("code") String code);

    /**
     * 根据目录名称查询
     */
    @Select("SELECT * FROM edams_asset_catalog WHERE name = #{name} AND deleted = 0 LIMIT 1")
    AssetCatalog selectByName(@Param("name") String name);

    /**
     * 根据目录路径查询
     */
    @Select("SELECT * FROM edams_asset_catalog WHERE path = #{path} AND deleted = 0 LIMIT 1")
    AssetCatalog selectByPath(@Param("path") String path);

    /**
     * 检查目录编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset_catalog WHERE code = #{code} AND deleted = 0")
    int existsByCode(@Param("code") String code);

    /**
     * 检查目录名称在同级是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset_catalog WHERE name = #{name} AND parent_id = #{parentId} AND deleted = 0")
    int existsByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    /**
     * 根据层级查询目录列表
     */
    @Select("SELECT * FROM edams_asset_catalog WHERE level = #{level} AND deleted = 0 ORDER BY sort_order ASC")
    List<AssetCatalog> selectByLevel(@Param("level") Integer level);

    /**
     * 查询所有启用的目录
     */
    @Select("SELECT * FROM edams_asset_catalog WHERE status = 1 AND deleted = 0 ORDER BY sort_order ASC, id ASC")
    List<AssetCatalog> selectAllEnabled();

    /**
     * 统计子目录数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset_catalog WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 查询指定层级的最大排序号
     */
    @Select("SELECT MAX(sort_order) FROM edams_asset_catalog WHERE parent_id = #{parentId} AND deleted = 0")
    Integer selectMaxSortOrder(@Param("parentId") Long parentId);
}
