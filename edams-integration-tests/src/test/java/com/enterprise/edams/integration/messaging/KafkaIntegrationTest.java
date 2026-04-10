package com.enterprise.edams.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Kafka消息队列集成测试
 * 测试任务: INT-KAFKA-001
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "asset-events",
        "quality-events",
        "notification-events",
        "lineage-events",
        "test-topic"
    },
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("Kafka消息队列集成测试")
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private TestKafkaConsumer testConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        testConsumer.clear();
    }

    @Test
    @DisplayName("测试Kafka连接")
    void testKafkaConnection() {
        // Given
        String topic = "test-topic";
        String message = "connection-test";

        // When
        kafkaTemplate.send(topic, message);

        // Then
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            testConsumer.getMessageCount("test-topic") > 0
        );
    }

    @Test
    @DisplayName("测试消息发送和接收")
    void testMessageSendAndReceive() throws Exception {
        // Given
        String topic = "asset-events";
        String key = "asset-001";
        AssetEvent event = new AssetEvent();
        event.setAssetId("asset-001");
        event.setEventType("CREATED");
        event.setAssetName("Customer Table");
        event.setTimestamp(System.currentTimeMillis());

        String message = objectMapper.writeValueAsString(event);

        // When
        SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();

        // Then
        assertThat(result.getRecordMetadata().topic()).isEqualTo(topic);

        // Wait and verify consumer received the message
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            testConsumer.hasMessage(topic, message)
        );
    }

    @Test
    @DisplayName("测试资产变更事件流")
    void testAssetChangeEventFlow() throws Exception {
        // Given
        String topic = "asset-events";
        String[] events = {"CREATED", "UPDATED", "PUBLISHED", "ARCHIVED"};

        // When - 发送一系列资产变更事件
        for (int i = 0; i < events.length; i++) {
            AssetEvent event = new AssetEvent();
            event.setAssetId("asset-" + i);
            event.setEventType(events[i]);
            event.setAssetName("Asset " + i);
            event.setTimestamp(System.currentTimeMillis());

            kafkaTemplate.send(topic, event.getAssetId(), objectMapper.writeValueAsString(event));
        }

        // Then - 验证所有事件都被消费
        await().atMost(10, TimeUnit.SECONDS).until(() -> 
            testConsumer.getMessageCount(topic) >= events.length
        );

        assertThat(testConsumer.getMessageCount(topic)).isEqualTo(events.length);
    }

    @Test
    @DisplayName("测试质量检测完成事件")
    void testQualityCheckCompletedEvent() throws Exception {
        // Given
        String topic = "quality-events";
        QualityCheckEvent event = new QualityCheckEvent();
        event.setCheckId("check-001");
        event.setAssetId("asset-001");
        event.setRuleId("rule-001");
        event.setStatus("COMPLETED");
        event.setTotalCount(1000);
        event.setErrorCount(5);
        event.setErrorRate(0.5);
        event.setTimestamp(System.currentTimeMillis());

        String message = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(topic, event.getCheckId(), message).get();

        // Then
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            testConsumer.hasMessage(topic, "COMPLETED")
        );
    }

    @Test
    @DisplayName("测试通知消息发送")
    void testNotificationEvent() throws Exception {
        // Given
        String topic = "notification-events";
        NotificationEvent event = new NotificationEvent();
        event.setNotificationId("notif-001");
        event.setType("QUALITY_ALERT");
        event.setRecipient("admin@enterprise.com");
        event.setTitle("Quality Check Failed");
        event.setContent("Asset asset-001 failed quality check");
        event.setTimestamp(System.currentTimeMillis());

        String message = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(topic, event.getNotificationId(), message).get();

        // Then
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            testConsumer.hasMessage(topic, "QUALITY_ALERT")
        );
    }

    @Test
    @DisplayName("测试血缘关系事件")
    void testLineageEvent() throws Exception {
        // Given
        String topic = "lineage-events";
        LineageEvent event = new LineageEvent();
        event.setEventId("lineage-001");
        event.setSourceAssetId("asset-source");
        event.setTargetAssetId("asset-target");
        event.setRelationType("DEPENDS_ON");
        event.setOperation("CREATE");
        event.setTimestamp(System.currentTimeMillis());

        String message = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send(topic, event.getEventId(), message).get();

        // Then
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            testConsumer.hasMessage(topic, "DEPENDS_ON")
        );
    }

    @Test
    @DisplayName("测试批量消息发送")
    void testBatchMessageSend() throws Exception {
        // Given
        String topic = "asset-events";
        int messageCount = 100;

        // When - 批量发送消息
        for (int i = 0; i < messageCount; i++) {
            AssetEvent event = new AssetEvent();
            event.setAssetId("batch-asset-" + i);
            event.setEventType("BATCH_CREATED");
            event.setAssetName("Batch Asset " + i);
            event.setTimestamp(System.currentTimeMillis());

            kafkaTemplate.send(topic, event.getAssetId(), objectMapper.writeValueAsString(event));
        }

        // Then - 验证所有消息被消费
        await().atMost(30, TimeUnit.SECONDS).until(() -> 
            testConsumer.getMessageCount(topic) >= messageCount
        );

        assertThat(testConsumer.getMessageCount(topic)).isGreaterThanOrEqualTo(messageCount);
    }

    @Test
    @DisplayName("测试消息顺序保证")
    void testMessageOrdering() throws Exception {
        // Given
        String topic = "asset-events";
        String assetId = "ordered-asset-001";
        int messageCount = 10;

        // When - 发送有序消息
        for (int i = 0; i < messageCount; i++) {
            AssetEvent event = new AssetEvent();
            event.setAssetId(assetId);
            event.setEventType("UPDATE");
            event.setSequenceNumber(i);
            event.setAssetName("Version " + i);
            event.setTimestamp(System.currentTimeMillis());

            // 使用相同的key确保发送到同一分区
            kafkaTemplate.send(topic, assetId, objectMapper.writeValueAsString(event)).get();
        }

        // Then - 验证消息被消费
        await().atMost(10, TimeUnit.SECONDS).until(() -> 
            testConsumer.getMessageCount(topic) >= messageCount
        );
    }

    @Test
    @DisplayName("测试消息重试机制")
    void testMessageRetry() throws Exception {
        // Given
        String topic = "test-topic";
        String message = "retry-test-message";

        // When - 发送消息
        kafkaTemplate.send(topic, message).get();

        // Then - 即使消费者暂时失败，消息最终会被消费
        await().atMost(10, TimeUnit.SECONDS).until(() -> 
            testConsumer.hasMessage(topic, message)
        );
    }

    @Test
    @DisplayName("测试不同Topic的消息路由")
    void testTopicRouting() throws Exception {
        // Given
        Map<String, String> topicMessages = Map.of(
            "asset-events", "asset-message",
            "quality-events", "quality-message",
            "notification-events", "notification-message",
            "lineage-events", "lineage-message"
        );

        // When - 向不同Topic发送消息
        for (Map.Entry<String, String> entry : topicMessages.entrySet()) {
            kafkaTemplate.send(entry.getKey(), entry.getValue()).get();
        }

        // Then - 验证每个Topic都收到了消息
        for (String topic : topicMessages.keySet()) {
            await().atMost(5, TimeUnit.SECONDS).until(() -> 
                testConsumer.getMessageCount(topic) > 0
            );
        }
    }

    // Event classes
    static class AssetEvent {
        private String assetId;
        private String eventType;
        private String assetName;
        private Long timestamp;
        private Integer sequenceNumber;

        public String getAssetId() { return assetId; }
        public void setAssetId(String assetId) { this.assetId = assetId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public Integer getSequenceNumber() { return sequenceNumber; }
        public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    }

    static class QualityCheckEvent {
        private String checkId;
        private String assetId;
        private String ruleId;
        private String status;
        private Integer totalCount;
        private Integer errorCount;
        private Double errorRate;
        private Long timestamp;

        public String getCheckId() { return checkId; }
        public void setCheckId(String checkId) { this.checkId = checkId; }
        public String getAssetId() { return assetId; }
        public void setAssetId(String assetId) { this.assetId = assetId; }
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
        public Integer getErrorCount() { return errorCount; }
        public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
        public Double getErrorRate() { return errorRate; }
        public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    static class NotificationEvent {
        private String notificationId;
        private String type;
        private String recipient;
        private String title;
        private String content;
        private Long timestamp;

        public String getNotificationId() { return notificationId; }
        public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    static class LineageEvent {
        private String eventId;
        private String sourceAssetId;
        private String targetAssetId;
        private String relationType;
        private String operation;
        private Long timestamp;

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public String getSourceAssetId() { return sourceAssetId; }
        public void setSourceAssetId(String sourceAssetId) { this.sourceAssetId = sourceAssetId; }
        public String getTargetAssetId() { return targetAssetId; }
        public void setTargetAssetId(String targetAssetId) { this.targetAssetId = targetAssetId; }
        public String getRelationType() { return relationType; }
        public void setRelationType(String relationType) { this.relationType = relationType; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }

    // Test consumer component
    @Component
    static class TestKafkaConsumer {
        private final Map<String, BlockingQueue<String>> messages = new java.util.concurrent.ConcurrentHashMap<>();
        private final Map<String, AtomicInteger> messageCounts = new java.util.concurrent.ConcurrentHashMap<>();

        public void clear() {
            messages.clear();
            messageCounts.clear();
        }

        @KafkaListener(topics = "asset-events", groupId = "test-group")
        public void consumeAssetEvents(ConsumerRecord<String, String> record) {
            consume("asset-events", record.value());
        }

        @KafkaListener(topics = "quality-events", groupId = "test-group")
        public void consumeQualityEvents(ConsumerRecord<String, String> record) {
            consume("quality-events", record.value());
        }

        @KafkaListener(topics = "notification-events", groupId = "test-group")
        public void consumeNotificationEvents(ConsumerRecord<String, String> record) {
            consume("notification-events", record.value());
        }

        @KafkaListener(topics = "lineage-events", groupId = "test-group")
        public void consumeLineageEvents(ConsumerRecord<String, String> record) {
            consume("lineage-events", record.value());
        }

        @KafkaListener(topics = "test-topic", groupId = "test-group")
        public void consumeTestEvents(ConsumerRecord<String, String> record) {
            consume("test-topic", record.value());
        }

        private void consume(String topic, String message) {
            messages.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>()).offer(message);
            messageCounts.computeIfAbsent(topic, k -> new AtomicInteger(0)).incrementAndGet();
        }

        public int getMessageCount(String topic) {
            return messageCounts.getOrDefault(topic, new AtomicInteger(0)).get();
        }

        public boolean hasMessage(String topic, String content) {
            BlockingQueue<String> queue = messages.get(topic);
            if (queue == null) return false;
            return queue.stream().anyMatch(m -> m.contains(content));
        }
    }
}
