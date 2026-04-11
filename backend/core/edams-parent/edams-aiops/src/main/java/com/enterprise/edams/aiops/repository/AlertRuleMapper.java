package com.enterprise.edams.aiops.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.aiops.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警规则Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {
}
