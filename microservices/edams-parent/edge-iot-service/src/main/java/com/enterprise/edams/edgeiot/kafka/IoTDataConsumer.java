package com.enterprise.edams.edgeiot.kafka;

import com.enterprise.edams.edgeiot.entity.SensorReading;
import com.enterprise.edams.edgeiot.entity.enums.DataQuality;
import com.enterprise.edams.edgeiot.mapper.SensorReadingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * IoT数据消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IoTDataConsumer {
    
    private final SensorReadingMapper sensorReadingMapper;
    
    @KafkaListener(topics = "${spring.kafka.consumer.properties.topic:iot-sensor-data}", 
                   groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeSensorData(@Payload String message, Acknowledgment acknowledgment) {
        try {
            log.debug("接收IoT传感器数据: {}", message);
            
            // 解析消息（实际项目中应使用JSON解析）
            // 格式: deviceId|sensorId|sensorType|sensorName|value|unit|quality
            String[] parts = message.split("\\|");
            if (parts.length >= 6) {
                SensorReading reading = SensorReading.builder()
                        .deviceId(parts[0])
                        .sensorId(parts[1])
                        .sensorType(com.enterprise.edams.edgeiot.entity.enums.SensorType.valueOf(parts[2]))
                        .sensorName(parts[3])
                        .value(Double.parseDouble(parts[4]))
                        .unit(parts[5])
                        .quality(parts.length > 6 ? DataQuality.valueOf(parts[6]) : DataQuality.GOOD)
                        .readingTime(LocalDateTime.now())
                        .build();
                
                sensorReadingMapper.insert(reading);
                log.debug("传感器数据保存成功: deviceId={}, sensorId={}", reading.getDeviceId(), reading.getSensorId());
            }
            
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("处理IoT传感器数据失败: {}", message, e);
            // 不确认消息，允许重试
        }
    }
}
