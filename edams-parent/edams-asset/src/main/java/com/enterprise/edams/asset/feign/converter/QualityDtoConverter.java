package com.enterprise.edams.asset.feign.converter;

import com.enterprise.edams.asset.dto.quality.QualityCheckRequest;
import com.enterprise.edams.asset.dto.quality.QualityCheckResultDTO;
import com.enterprise.edams.asset.dto.quality.QualityRuleDTO;
import com.enterprise.dataplatform.quality.domain.entity.QualityCheckResult;
import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.QualityCheckRequest as QualityCheckRequest2;
import com.enterprise.dataplatform.quality.dto.response.QualityCheckResultResponse;
import com.enterprise.dataplatform.quality.dto.response.QualityRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 质量服务DTO转换器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper(componentModel = "spring")
public interface QualityDtoConverter {

    QualityDtoConverter INSTANCE = Mappers.getMapper(QualityDtoConverter.class);

    /**
     * 质量检查请求转DTO
     */
    QualityCheckRequest toRequest(QualityCheckRequest2 request);

    /**
     * 质量检查结果转DTO
     */
    QualityCheckResultDTO toResultDto(QualityCheckResultResponse response);

    /**
     * 质量规则转DTO
     */
    QualityRuleDTO toRuleDto(QualityRuleResponse response);

    /**
     * 质量规则列表转DTO列表
     */
    List<QualityRuleDTO> toRuleDtoList(List<QualityRuleResponse> responses);

    /**
     * 质量检查请求转内部请求
     */
    QualityCheckRequest2 toInternalRequest(QualityCheckRequest request);
}
