package com.enterprise.edams.asset.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.asset.entity.Asset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 资产Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AssetMapper extends BaseMapper<Asset> {

    /**
     * 根据资产编码查询
     */
    @Select("SELECT * FROM edams_asset WHERE asset_code = #{assetCode} AND deleted = 0 LIMIT 1")
    Asset selectByAssetCode(@Param("assetCode") String assetCode);

    /**
     * 根据资产名称查询
     */
    @Select("SELECT * FROM edams_asset WHERE asset_name = #{assetName} AND deleted = 0 LIMIT 1")
    Asset selectByAssetName(@Param("assetName") String assetName);

    /**
     * 检查资产编码是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset WHERE asset_code = #{assetCode} AND deleted = 0")
    int existsByAssetCode(@Param("assetCode") String assetCode);

    /**
     * 检查资产名称是否存在
     */
    @Select("SELECT COUNT(1) FROM edams_asset WHERE asset_name = #{assetName} AND deleted = 0")
    int existsByAssetName(@Param("assetName") String assetName);

    /**
     * 根据目录ID查询资产列表
     */
    @Select("SELECT * FROM edams_asset WHERE catalog_id = #{catalogId} AND deleted = 0 ORDER BY created_time DESC")
    List<Asset> selectByCatalogId(@Param("catalogId") Long catalogId);

    /**
     * 根据业务域ID查询资产列表
     */
    @Select("SELECT * FROM edams_asset WHERE domain_id = #{domainId} AND deleted = 0 ORDER BY created_time DESC")
    List<Asset> selectByDomainId(@Param("domainId") Long domainId);

    /**
     * 根据负责人ID查询资产列表
     */
    @Select("SELECT * FROM edams_asset WHERE owner_id = #{ownerId} AND deleted = 0 ORDER BY created_time DESC")
    List<Asset> selectByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 根据数据源ID查询资产列表
     */
    @Select("SELECT * FROM edams_asset WHERE datasource_id = #{datasourceId} AND deleted = 0 ORDER BY created_time DESC")
    List<Asset> selectByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 更新资产状态
     */
    @Update("UPDATE edams_asset SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新资产版本号
     */
    @Update("UPDATE edams_asset SET version = version + 1, updated_time = NOW() WHERE id = #{id}")
    int incrementVersion(@Param("id") Long id);

    /**
     * 根据标签ID查询关联的资产列表
     */
    @Select("SELECT a.* FROM edams_asset a " +
            "INNER JOIN edams_asset_tag_relation atr ON a.id = atr.asset_id " +
            "WHERE atr.tag_id = #{tagId} AND a.deleted = 0 ORDER BY a.created_time DESC")
    List<Asset> selectByTagId(@Param("tagId") Long tagId);

    /**
     * 统计目录下资产数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset WHERE catalog_id = #{catalogId} AND deleted = 0")
    int countByCatalogId(@Param("catalogId") Long catalogId);

    /**
     * 统计业务域下资产数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset WHERE domain_id = #{domainId} AND deleted = 0")
    int countByDomainId(@Param("domainId") Long domainId);

    /**
     * 根据状态统计资产数量
     */
    @Select("SELECT COUNT(1) FROM edams_asset WHERE status = #{status} AND deleted = 0")
    int countByStatus(@Param("status") String status);
}
