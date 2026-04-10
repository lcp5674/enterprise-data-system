package com.enterprise.edams.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.asset.entity.DataAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 数据资产Mapper
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Mapper
public interface DataAssetMapper extends BaseMapper<DataAsset> {

    /**
     * 根据资产名称查询
     */
    @Select("SELECT * FROM data_asset WHERE asset_name = #{assetName} AND deleted = 0 LIMIT 1")
    DataAsset selectByAssetName(@Param("assetName") String assetName);

    /**
     * 检查资产名称是否存在
     */
    @Select("SELECT COUNT(1) FROM data_asset WHERE asset_name = #{assetName} AND deleted = 0")
    int existsByAssetName(@Param("assetName") String assetName);
}
