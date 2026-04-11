package com.enterprise.dataplatform.metadata.service;

import com.enterprise.dataplatform.metadata.dto.request.MetadataFieldRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataRegisterRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataSearchRequest;
import com.enterprise.dataplatform.metadata.dto.response.MetadataResponse;
import com.enterprise.dataplatform.metadata.entity.MetadataField;
import com.enterprise.dataplatform.metadata.entity.MetadataObject;
import com.enterprise.dataplatform.metadata.repository.MetadataFieldRepository;
import com.enterprise.dataplatform.metadata.repository.MetadataObjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Metadata Management Service
 * Core service for registering, updating, querying and managing metadata objects
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MetadataService {

    private final MetadataObjectRepository metadataObjectRepository;
    private final MetadataFieldRepository metadataFieldRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String METADATA_TOPIC = "metadata.changes";

    /**
     * Register or update metadata object
     */
    public MetadataResponse registerMetadata(MetadataRegisterRequest request, List<MetadataFieldRequest> fields) {
        log.info("Registering metadata for objectId: {}", request.getObjectId());

        Optional<MetadataObject> existingOpt = metadataObjectRepository.findByObjectId(request.getObjectId());
        MetadataObject metadataObject;

        if (existingOpt.isPresent()) {
            metadataObject = existingOpt.get();
            updateObjectFromRequest(metadataObject, request);
        } else {
            metadataObject = createObjectFromRequest(request);
        }

        metadataObject = metadataObjectRepository.save(metadataObject);

        // Save field information
        if (fields != null && !fields.isEmpty()) {
            saveFieldMetadata(request.getObjectId(), fields);
        }

        // Publish Kafka event
        publishMetadataEvent("REGISTER", metadataObject);

        log.info("Successfully registered metadata for objectId: {}", request.getObjectId());
        return buildResponseWithFields(metadataObject);
    }

    /**
     * Update existing metadata object
     */
    public MetadataResponse updateMetadata(String objectId, MetadataRegisterRequest request, List<MetadataFieldRequest> fields) {
        log.info("Updating metadata for objectId: {}", objectId);

        MetadataObject metadataObject = metadataObjectRepository.findByObjectId(objectId)
                .orElseThrow(() -> new RuntimeException("Metadata object not found: " + objectId));

        updateObjectFromRequest(metadataObject, request);
        metadataObject.setLastUpdated(LocalDateTime.now());
        metadataObject = metadataObjectRepository.save(metadataObject);

        // Update field information if provided
        if (fields != null) {
            metadataFieldRepository.deleteByObjectId(objectId);
            if (!fields.isEmpty()) {
                saveFieldMetadata(objectId, fields);
            }
        }

        // Publish Kafka event
        publishMetadataEvent("UPDATE", metadataObject);

        log.info("Successfully updated metadata for objectId: {}", objectId);
        return buildResponseWithFields(metadataObject);
    }

    /**
     * Delete metadata (logical delete - set status to DEPRECATED)
     */
    public void deleteMetadata(String objectId) {
        log.info("Deleting metadata for objectId: {}", objectId);

        MetadataObject metadataObject = metadataObjectRepository.findByObjectId(objectId)
                .orElseThrow(() -> new RuntimeException("Metadata object not found: " + objectId));

        metadataObject.setStatus("DEPRECATED");
        metadataObject.setLastUpdated(LocalDateTime.now());
        metadataObjectRepository.save(metadataObject);

        // Publish Kafka event
        publishMetadataEvent("DELETE", metadataObject);

        log.info("Successfully deleted metadata for objectId: {}", objectId);
    }

    /**
     * Get metadata by objectId
     */
    @Transactional(readOnly = true)
    public MetadataResponse getMetadata(String objectId) {
        log.debug("Getting metadata for objectId: {}", objectId);

        MetadataObject metadataObject = metadataObjectRepository.findByObjectId(objectId)
                .orElseThrow(() -> new RuntimeException("Metadata object not found: " + objectId));

        return buildResponseWithFields(metadataObject);
    }

    /**
     * Search metadata with pagination and filters
     */
    @Transactional(readOnly = true)
    public Page<MetadataResponse> searchMetadata(MetadataSearchRequest request) {
        log.debug("Searching metadata with criteria: {}", request);

        Specification<MetadataObject> spec = buildSearchSpecification(request);
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                Sort.by("updatedAt").descending()
        );

        Page<MetadataObject> page = metadataObjectRepository.findAll(spec, pageable);

        return page.map(this::buildResponseWithFields);
    }

    /**
     * Get metadata objects by domain code
     */
    @Transactional(readOnly = true)
    public List<MetadataResponse> getMetadataByDomain(String domainCode) {
        log.debug("Getting metadata for domain: {}", domainCode);

        List<MetadataObject> objects = metadataObjectRepository.findByDomainCode(domainCode);
        return objects.stream()
                .map(this::buildResponseWithFields)
                .collect(Collectors.toList());
    }

    /**
     * Get metadata statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMetadataStats() {
        log.debug("Getting metadata statistics");

        Map<String, Object> stats = new HashMap<>();

        // Total count
        long totalCount = metadataObjectRepository.count();
        stats.put("totalCount", totalCount);

        // Count by object type
        List<String> objectTypes = Arrays.asList("TABLE", "VIEW", "API", "FILE", "STREAM");
        Map<String, Long> typeCount = new HashMap<>();
        for (String type : objectTypes) {
            typeCount.put(type, metadataObjectRepository.countByObjectType(type));
        }
        stats.put("byObjectType", typeCount);

        // Count by sensitivity
        List<String> sensitivities = Arrays.asList("PUBLIC", "INTERNAL", "CONFIDENTIAL", "SECRET");
        Map<String, Long> sensitivityCount = new HashMap<>();
        for (String sensitivity : sensitivities) {
            sensitivityCount.put(sensitivity, metadataObjectRepository.countBySensitivity(sensitivity));
        }
        stats.put("bySensitivity", sensitivityCount);

        // Count by domain
        Set<String> domains = metadataObjectRepository.findAll().stream()
                .map(MetadataObject::getDomainCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, Long> domainCount = new HashMap<>();
        for (String domain : domains) {
            domainCount.put(domain, metadataObjectRepository.countByDomainCode(domain));
        }
        stats.put("byDomain", domainCount);

        return stats;
    }

    /**
     * Sync metadata from asset service (used by Kafka consumer)
     */
    public MetadataResponse syncFromAsset(String assetId, Map<String, Object> assetInfo) {
        log.info("Syncing metadata from asset: {}", assetId);

        MetadataRegisterRequest request = MetadataRegisterRequest.builder()
                .objectId(assetId)
                .objectType(convertAssetType((String) assetInfo.get("assetType")))
                .domainCode((String) assetInfo.get("domainCode"))
                .name((String) assetInfo.get("name"))
                .displayName((String) assetInfo.get("displayName"))
                .description((String) assetInfo.get("description"))
                .owner((String) assetInfo.get("owner"))
                .ownerEmail((String) assetInfo.get("ownerEmail"))
                .sensitivity((String) assetInfo.getOrDefault("sensitivityLevel", "INTERNAL"))
                .dataSource((String) assetInfo.get("dataSource"))
                .locationPath((String) assetInfo.get("location"))
                .rowCount(assetInfo.get("rowCount") != null ? ((Number) assetInfo.get("rowCount")).longValue() : null)
                .sizeBytes(assetInfo.get("sizeBytes") != null ? ((Number) assetInfo.get("sizeBytes")).longValue() : null)
                .build();

        // Parse schema info if provided
        if (assetInfo.get("schemaInfo") != null) {
            request.setSchemaInfo(assetInfo.get("schemaInfo").toString());
        }

        // Parse tags if provided
        if (assetInfo.get("tags") != null) {
            try {
                request.setTags(objectMapper.writeValueAsString(assetInfo.get("tags")));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize tags", e);
            }
        }

        return registerMetadata(request, null);
    }

    // =============== Private Helper Methods ===============

    private MetadataObject createObjectFromRequest(MetadataRegisterRequest request) {
        return MetadataObject.builder()
                .objectId(request.getObjectId())
                .objectType(request.getObjectType())
                .domainCode(request.getDomainCode())
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .schemaInfo(request.getSchemaInfo())
                .tags(request.getTags())
                .owner(request.getOwner())
                .ownerEmail(request.getOwnerEmail())
                .sensitivity(request.getSensitivity() != null ? request.getSensitivity() : "INTERNAL")
                .status("ACTIVE")
                .dataSource(request.getDataSource())
                .locationPath(request.getLocationPath())
                .rowCount(request.getRowCount())
                .sizeBytes(request.getSizeBytes())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private void updateObjectFromRequest(MetadataObject object, MetadataRegisterRequest request) {
        object.setObjectType(request.getObjectType());
        object.setDomainCode(request.getDomainCode());
        object.setName(request.getName());
        object.setDisplayName(request.getDisplayName());
        object.setDescription(request.getDescription());
        if (request.getSchemaInfo() != null) {
            object.setSchemaInfo(request.getSchemaInfo());
        }
        if (request.getTags() != null) {
            object.setTags(request.getTags());
        }
        if (request.getOwner() != null) {
            object.setOwner(request.getOwner());
        }
        if (request.getOwnerEmail() != null) {
            object.setOwnerEmail(request.getOwnerEmail());
        }
        if (request.getSensitivity() != null) {
            object.setSensitivity(request.getSensitivity());
        }
        if (request.getDataSource() != null) {
            object.setDataSource(request.getDataSource());
        }
        if (request.getLocationPath() != null) {
            object.setLocationPath(request.getLocationPath());
        }
        if (request.getRowCount() != null) {
            object.setRowCount(request.getRowCount());
        }
        if (request.getSizeBytes() != null) {
            object.setSizeBytes(request.getSizeBytes());
        }
        object.setLastUpdated(LocalDateTime.now());
    }

    private void saveFieldMetadata(String objectId, List<MetadataFieldRequest> fields) {
        List<MetadataField> fieldEntities = new ArrayList<>();
        int position = 0;
        for (MetadataFieldRequest field : fields) {
            MetadataField entity = MetadataField.builder()
                    .objectId(objectId)
                    .fieldName(field.getFieldName())
                    .fieldType(field.getFieldType())
                    .fieldLength(field.getFieldLength())
                    .nullable(field.getNullable() != null ? field.getNullable() : true)
                    .primaryKey(field.getPrimaryKey() != null ? field.getPrimaryKey() : false)
                    .description(field.getDescription())
                    .sampleValues(field.getSampleValues())
                    .sensitivityLevel(field.getSensitivityLevel())
                    .businessComment(field.getBusinessComment())
                    .ordinalPosition(field.getOrdinalPosition() != null ? field.getOrdinalPosition() : position++)
                    .build();
            fieldEntities.add(entity);
        }
        metadataFieldRepository.saveAll(fieldEntities);
    }

    private MetadataResponse buildResponseWithFields(MetadataObject object) {
        MetadataResponse response = MetadataResponse.fromEntity(object);

        List<MetadataField> fields = metadataFieldRepository.findByObjectId(object.getObjectId());
        List<MetadataResponse.MetadataFieldInfo> fieldInfos = fields.stream()
                .map(this::convertToFieldInfo)
                .collect(Collectors.toList());

        response.setFields(fieldInfos);
        return response;
    }

    private MetadataResponse.MetadataFieldInfo convertToFieldInfo(MetadataField field) {
        return MetadataResponse.MetadataFieldInfo.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .fieldType(field.getFieldType())
                .fieldLength(field.getFieldLength())
                .nullable(field.getNullable())
                .primaryKey(field.getPrimaryKey())
                .description(field.getDescription())
                .sampleValues(field.getSampleValues())
                .sensitivityLevel(field.getSensitivityLevel())
                .businessComment(field.getBusinessComment())
                .ordinalPosition(field.getOrdinalPosition())
                .build();
    }

    private Specification<MetadataObject> buildSearchSpecification(MetadataSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search (name or description)
            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            // Object type filter
            if (StringUtils.hasText(request.getObjectType())) {
                predicates.add(criteriaBuilder.equal(root.get("objectType"), request.getObjectType()));
            }

            // Domain code filter
            if (StringUtils.hasText(request.getDomainCode())) {
                predicates.add(criteriaBuilder.equal(root.get("domainCode"), request.getDomainCode()));
            }

            // Sensitivity filter
            if (StringUtils.hasText(request.getSensitivity())) {
                predicates.add(criteriaBuilder.equal(root.get("sensitivity"), request.getSensitivity()));
            }

            // Status filter
            if (StringUtils.hasText(request.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            } else {
                // Default to ACTIVE status
                predicates.add(criteriaBuilder.equal(root.get("status"), "ACTIVE"));
            }

            // Owner filter
            if (StringUtils.hasText(request.getOwner())) {
                predicates.add(criteriaBuilder.equal(root.get("owner"), request.getOwner()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void publishMetadataEvent(String eventType, MetadataObject object) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("objectId", object.getObjectId());
            event.put("objectType", object.getObjectType());
            event.put("domainCode", object.getDomainCode());
            event.put("name", object.getName());
            event.put("timestamp", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(METADATA_TOPIC, object.getObjectId(), message);
            log.debug("Published {} event for objectId: {}", eventType, object.getObjectId());
        } catch (Exception e) {
            log.error("Failed to publish metadata event for objectId: {}", object.getObjectId(), e);
        }
    }

    private String convertAssetType(String assetType) {
        if (assetType == null) {
            return "TABLE";
        }
        return switch (assetType.toUpperCase()) {
            case "DATABASE", "TABLE", "VIEW" -> "TABLE";
            case "API", "SERVICE" -> "API";
            case "FILE", "FILE_SYSTEM" -> "FILE";
            case "STREAM", "KAFKA", "MESSAGE_QUEUE" -> "STREAM";
            default -> "TABLE";
        };
    }
}
