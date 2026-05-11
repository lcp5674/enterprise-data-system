package com.enterprise.dataplatform.iot.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeviceRepository extends BaseMapper<EdgeDevice> {

    @Select("SELECT * FROM edge_device WHERE device_id = #{deviceId} AND deleted = false")
    EdgeDevice findByDeviceId(@Param("deviceId") String deviceId);

    @Select("SELECT * FROM edge_device WHERE status = #{status} AND deleted = false")
    List<EdgeDevice> findByStatus(@Param("status") EdgeDevice.DeviceStatus status);

    @Select("SELECT * FROM edge_device WHERE online = true AND deleted = false")
    List<EdgeDevice> findOnlineDevices();

    @Select("SELECT * FROM edge_device WHERE last_heartbeat < #{threshold} AND online = true AND deleted = false")
    List<EdgeDevice> findOfflineDevicesByHeartbeat(@Param("threshold") LocalDateTime threshold);

    @Select("SELECT * FROM edge_device WHERE device_type = #{deviceType} AND deleted = false")
    List<EdgeDevice> findByDeviceType(@Param("deviceType") String deviceType);

    @Select("SELECT * FROM edge_device WHERE " +
            "(@tags IS NOT NULL AND JSON_CONTAINS(tags, #{tagValue})) " +
            "AND deleted = false")
    List<EdgeDevice> findByTag(@Param("tagValue") String tagValue);

    @Select("SELECT COUNT(*) FROM edge_device WHERE status = #{status} AND deleted = false")
    long countByStatus(@Param("status") EdgeDevice.DeviceStatus status);

    @Select("SELECT COUNT(*) FROM edge_device WHERE online = true AND deleted = false")
    long countOnlineDevices();

    @Select("SELECT * FROM edge_device WHERE " +
            "(#{keyword} IS NULL OR device_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR device_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR serial_number LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "AND (#{deviceType} IS NULL OR device_type = #{deviceType}) " +
            "AND deleted = false")
    List<EdgeDevice> searchDevices(
            @Param("keyword") String keyword,
            @Param("status") EdgeDevice.DeviceStatus status,
            @Param("deviceType") String deviceType);
}
