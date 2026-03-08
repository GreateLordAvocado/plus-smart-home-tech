package ru.yandex.practicum.kafka.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.analyzer.kafka.AnalyzerKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.analyzer.kafka.AvroBytesDeserializer;
import ru.yandex.practicum.kafka.telemetry.analyzer.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, byte[]> consumer;
    private final AnalyzerKafkaProperties props;
    private final AvroBytesDeserializer avro;
    private final HubEventService hubEventService;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public HubEventProcessor(KafkaConsumer<String, byte[]> hubEventsConsumer,
                             AnalyzerKafkaProperties props,
                             AvroBytesDeserializer avro,
                             HubEventService hubEventService) {
        this.consumer = hubEventsConsumer;
        this.props = props;
        this.avro = avro;
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        String topic = props.getConsumers().getHubEvents().getTopic();
        int timeoutMs = props.getConsumers().getHubEvents().getPollTimeoutMs();

        consumer.subscribe(List.of(topic));
        log.info("HubEventProcessor subscribed to topic='{}'", topic);

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(timeoutMs));
                if (records.isEmpty()) {
                    continue;
                }

                records.forEach(r -> {
                    try {
                        HubEventAvro event = avro.deserialize(r.value(), HubEventAvro.class);
                        hubEventService.handle(event);
                    } catch (Exception ex) {
                        log.error("Failed to process hub event. topic={}, partition={}, offset={}",
                                r.topic(), r.partition(), r.offset(), ex);
                    }
                });

                try {
                    consumer.commitSync();
                } catch (Exception e) {
                    log.warn("HubEventProcessor commit failed", e);
                }
            }
        } catch (WakeupException e) {
        } catch (Exception e) {
            log.error("HubEventProcessor failed", e);
        } finally {
            consumer.close();
            log.info("HubEventProcessor stopped");
        }
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        consumer.wakeup();
    }
}
