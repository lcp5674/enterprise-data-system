package com.enterprise.dataplatform.iot.repository;

import com.enterprise.dataplatform.iot.entity.DeviceGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceGroupRepository extends MongoRepository<DeviceGroup, String> {

    Optional<DeviceGroup> findByGroupId(String groupId);

    List<DeviceGroup> findByParentId(String parentId);

    List<DeviceGroup> findByGroupNameContaining(String groupName);

    List<DeviceGroup> findByDeleted(boolean deleted);
}
