package ru.yandex.practicum.kafka.telemetry.collector.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
public class TelemetryKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryKafkaProducer.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final AvroBytesSerializer avroSerializer;
    private final CollectorKafkaProperties properties;

    public TelemetryKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate,
                                  AvroBytesSerializer avroSerializer,
                                  CollectorKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.avroSerializer = avroSerializer;
        this.properties = properties;
    }

    public void sendSensor(SensorEventAvro event) {
        try {
            byte[] payload = avroSerializer.serialize(event);
            String topic = properties.getTopics().getSensors();
            kafkaTemplate.send(topic, event.getHubId(), payload);
            log.debug("Sent SensorEventAvro to Kafka topic={}, bytes={}", topic, payload.length);
        } catch (Exception e) {
            log.warn("Failed to send SensorEventAvro to Kafka", e);
        }
    }

    public void sendHub(HubEventAvro event) {
        try {
            byte[] payload = avroSerializer.serialize(event);
            String topic = properties.getTopics().getHubs();
            kafkaTemplate.send(topic, event.getHubId(), payload);
            log.debug("Sent HubEventAvro to Kafka topic={}, bytes={}", topic, payload.length);
        } catch (Exception e) {
            log.warn("Failed to send HubEventAvro to Kafka", e);
        }
    }
}
