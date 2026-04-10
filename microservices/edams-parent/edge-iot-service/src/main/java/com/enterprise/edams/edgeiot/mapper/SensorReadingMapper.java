package com.enterprise.edams.edgeiot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.edgeiot.entity.SensorReading;
import org.apache.ibatis.annotations.Mapper;

/**
 * 传感器读数Mapper
 */
@Mapper
public interface SensorReadingMapper extends BaseMapper<SensorReading> {
}
