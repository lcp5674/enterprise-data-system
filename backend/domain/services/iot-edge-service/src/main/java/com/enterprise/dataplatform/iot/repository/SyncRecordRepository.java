package com.enterprise.dataplatform.iot.repository;

import com.enterprise.dataplatform.iot.entity.SyncRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SyncRecordRepository extends MongoRepository<SyncRecord, String> {

    List<SyncRecord> findByDeviceId(String deviceId);

    List<SyncRecord> findByStatus(SyncRecord.Status status);

    List<SyncRecord> findByDeviceIdAndStatus(String deviceId, SyncRecord.Status status);

    List<SyncRecord> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<SyncRecord> findByDeviceIdAndCreatedAtBetween(String deviceId, LocalDateTime startTime, LocalDateTime endTime);
}
