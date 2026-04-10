package com.enterprise.edams.datasource.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.datasource.entity.DatasourceCredential;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据源凭证Mapper接口
 */
@Mapper
public interface DatasourceCredentialRepository extends BaseMapper<DatasourceCredential> {

    /**
     * 根据数据源ID查询凭证
     *
     * @param datasourceId 数据源ID
     * @return 凭证列表
     */
    List<DatasourceCredential> selectByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 根据数据源ID查询有效凭证
     *
     * @param datasourceId 数据源ID
     * @return 凭证
     */
    DatasourceCredential selectValidByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 根据密钥标识查询凭证
     *
     * @param keyIdentifier 密钥标识
     * @return 凭证
     */
    DatasourceCredential selectByKeyIdentifier(@Param("keyIdentifier") String keyIdentifier);

    /**
     * 查询需要轮换的凭证
     *
     * @return 凭证列表
     */
    List<DatasourceCredential> selectCredentialsNeedRotation();
}
