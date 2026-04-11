package com.enterprise.dataplatform.index.repository;

import com.enterprise.dataplatform.index.document.AssetIndexDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 资产索引仓库 - Elasticsearch数据访问层
 *
 * @author Team-D
 * @version 1.0.0
 */
@Repository
public interface AssetIndexRepository extends ElasticsearchRepository<AssetIndexDocument, String> {

    /**
     * 根据资产ID查询
     *
     * @param assetId 资产ID
     * @return 资产索引文档
     */
    Optional<AssetIndexDocument> findByAssetId(String assetId);

    /**
     * 根据资产ID删除
     *
     * @param assetId 资产ID
     */
    void deleteByAssetId(String assetId);

    /**
     * 根据对象类型查询
     *
     * @param objectType 对象类型
     * @return 资产列表
     */
    List<AssetIndexDocument> findByObjectType(String objectType);

    /**
     * 根据领域编码查询
     *
     * @param domainCode 领域编码
     * @return 资产列表
     */
    List<AssetIndexDocument> findByDomainCode(String domainCode);

    /**
     * 根据负责人查询
     *
     * @param owner 负责人ID
     * @return 资产列表
     */
    List<AssetIndexDocument> findByOwner(String owner);

    /**
     * 根据状态查询
     *
     * @param status 状态
     * @return 资产列表
     */
    List<AssetIndexDocument> findByStatus(String status);

    /**
     * 根据敏感度等级查询
     *
     * @param sensitivity 敏感度等级
     * @return 资产列表
     */
    List<AssetIndexDocument> findBySensitivity(String sensitivity);

    /**
     * 根据标签查询
     *
     * @param tag 标签
     * @return 资产列表
     */
    List<AssetIndexDocument> findByTagsContaining(String tag);

    /**
     * 批量根据资产ID查询
     *
     * @param assetIds 资产ID列表
     * @return 资产列表
     */
    List<AssetIndexDocument> findByAssetIdIn(List<String> assetIds);

    /**
     * 检查资产是否存在
     *
     * @param assetId 资产ID
     * @return 是否存在
     */
    boolean existsByAssetId(String assetId);
}
