package com.enterprise.edams.asset.feign.converter;

import com.enterprise.edams.asset.dto.lineage.CreateLineageRequest;
import com.enterprise.edams.asset.dto.lineage.ImpactAnalysisDTO;
import com.enterprise.edams.asset.dto.lineage.LineageGraphDTO;
import com.enterprise.dataplatform.lineage.domain.entity.LineageRelation;
import com.enterprise.dataplatform.lineage.dto.request.CreateLineageRequest as LineageCreateRequest;
import com.enterprise.dataplatform.lineage.dto.response.ImpactAnalysisResponse;
import com.enterprise.dataplatform.lineage.dto.response.LineageGraphResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 血缘服务DTO转换器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LineageDtoConverter {

    LineageDtoConverter INSTANCE = Mappers.getMapper(LineageDtoConverter.class);

    /**
     * 血缘关系转创建请求
     */
    CreateLineageRequest toCreateRequest(LineageRelation entity);

    /**
     * 血缘图谱响应转DTO
     */
    LineageGraphDTO toGraphDto(LineageGraphResponse response);

    /**
     * 影响分析响应转DTO
     */
    ImpactAnalysisDTO toImpactAnalysisDto(ImpactAnalysisResponse response);

    /**
     * 创建请求转血缘关系实体
     */
    LineageCreateRequest toLineageCreateRequest(CreateLineageRequest request);
}
