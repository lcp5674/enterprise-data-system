package com.enterprise.edams.sandbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.sandbox.entity.SampleDataRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SampleDataRequestMapper extends BaseMapper<SampleDataRequest> {
}
