package ru.yandex.practicum.kafka.telemetry.aggregator.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.aggregator.kafka.AggregatorKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.aggregator.kafka.AvroBytesDeserializer;
import ru.yandex.practicum.kafka.telemetry.aggregator.producer.SnapshotProducer;
import ru.yandex.practicum.kafka.telemetry.aggregator.service.SnapshotAggregationService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SensorEventProcessor {

    private final KafkaConsumer<String, byte[]> consumer;
    private final AggregatorKafkaProperties props;
    private final AvroBytesDeserializer avroDeserializer;
    private final SnapshotAggregationService aggregationService;
    private final SnapshotProducer snapshotProducer;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public SensorEventProcessor(KafkaConsumer<String, byte[]> consumer,
                                AggregatorKafkaProperties props,
                                AvroBytesDeserializer avroDeserializer,
                                SnapshotAggregationService aggregationService,
                                SnapshotProducer snapshotProducer) {
        this.consumer = consumer;
        this.props = props;
        this.avroDeserializer = avroDeserializer;
        this.aggregationService = aggregationService;
        this.snapshotProducer = snapshotProducer;
    }

    public void start() {
        running.set(true);

        String topic = props.getTopics().getSensors();
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Aggregator subscribed to topic: {}", topic);

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));

                boolean anyProcessed = false;

                for (ConsumerRecord<String, byte[]> record : records) {
                    anyProcessed = true;

                    SensorEventAvro event = avroDeserializer.deserialize(record.value(), SensorEventAvro.class);

                    Optional<SensorsSnapshotAvro> snapshotOpt = aggregationService.onSensorEvent(event);
                    snapshotOpt.ifPresent(snapshotProducer::send);
                }

                if (anyProcessed && !props.getConsumer().isEnableAutoCommit()) {
                    consumer.commitSync();
                }
            }
        } catch (Exception e) {
            log.error("Aggregator loop crashed", e);
            throw e;
        } finally {
            safeClose();
        }
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        safeClose();
    }

    private void safeClose() {
        try {
            consumer.wakeup();
        } catch (Exception ignored) {
        }
        try {
            consumer.close();
        } catch (Exception ignored) {
        }
    }
}
