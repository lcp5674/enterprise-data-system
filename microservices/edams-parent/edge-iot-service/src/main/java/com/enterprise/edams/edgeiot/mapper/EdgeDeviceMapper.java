package com.enterprise.edams.edgeiot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.edgeiot.entity.EdgeDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 边缘设备Mapper
 */
@Mapper
public interface EdgeDeviceMapper extends BaseMapper<EdgeDevice> {
}
