package com.enterprise.dataplatform.iot.repository;

import com.enterprise.dataplatform.iot.entity.DeviceData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceDataRepository extends MongoRepository<DeviceData, String> {

    List<DeviceData> findByDeviceId(String deviceId);

    List<DeviceData> findByDeviceIdAndSyncStatus(String deviceId, DeviceData.SyncStatus syncStatus);

    @Query("{'deviceId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<DeviceData> findByDeviceIdAndTimeRange(String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    List<DeviceData> findBySyncStatus(DeviceData.SyncStatus syncStatus);

    @Query("{'syncStatus': ?0, 'timestamp': {$lt: ?1}}")
    List<DeviceData> findBySyncStatusAndOlderThan(DeviceData.SyncStatus syncStatus, LocalDateTime threshold);

    long countByDeviceIdAndSyncStatus(String deviceId, DeviceData.SyncStatus syncStatus);

    void deleteByDeviceIdAndTimestampBefore(String deviceId, LocalDateTime threshold);
}
