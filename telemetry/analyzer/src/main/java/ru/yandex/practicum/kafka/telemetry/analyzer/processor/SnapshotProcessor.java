package ru.yandex.practicum.kafka.telemetry.analyzer.processor;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.analyzer.kafka.AnalyzerKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.analyzer.kafka.AvroBytesDeserializer;
import ru.yandex.practicum.kafka.telemetry.analyzer.service.HubRouterActionSender;
import ru.yandex.practicum.kafka.telemetry.analyzer.service.ScenarioEvaluationService;
import ru.yandex.practicum.kafka.telemetry.event.SnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, byte[]> consumer;
    private final AnalyzerKafkaProperties props;
    private final AvroBytesDeserializer avro;
    private final ScenarioEvaluationService scenarioEvaluationService;
    private final HubRouterActionSender hubRouterActionSender;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public SnapshotProcessor(KafkaConsumer<String, byte[]> snapshotsConsumer,
                             AnalyzerKafkaProperties props,
                             AvroBytesDeserializer avro,
                             ScenarioEvaluationService scenarioEvaluationService,
                             HubRouterActionSender hubRouterActionSender) {
        this.consumer = snapshotsConsumer;
        this.props = props;
        this.avro = avro;
        this.scenarioEvaluationService = scenarioEvaluationService;
        this.hubRouterActionSender = hubRouterActionSender;
    }

    public void start() {
        String topic = props.getConsumers().getSnapshots().getTopic();
        int timeoutMs = props.getConsumers().getSnapshots().getPollTimeoutMs();

        consumer.subscribe(List.of(topic));
        log.info("SnapshotProcessor subscribed to topic='{}'", topic);

        try {
            while (running.get()) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(timeoutMs));
                if (records.isEmpty()) {
                    continue;
                }

                boolean batchOk = true;

                for (var r : records) {
                    try {
                        SnapshotAvro snapshot = avro.deserialize(r.value(), SnapshotAvro.class);

                        String hubId = snapshot.getHubId();
                        long ts = snapshot.getTimestamp();

                        var actions = scenarioEvaluationService.evaluate(snapshot);

                        if (!actions.isEmpty()) {
                            hubRouterActionSender.sendActions(hubId, ts, actions);
                            log.info("Actions sent: hubId={}, actions={}, ts={}", hubId, actions.size(), ts);
                        } else {
                            log.debug("No actions to execute: hubId={}, ts={}", hubId, ts);
                        }
                    } catch (Exception ex) {
                        batchOk = false;
                        log.error("Failed to process snapshot. topic={}, partition={}, offset={}",
                                r.topic(), r.partition(), r.offset(), ex);
                        break;
                    }
                }

                if (batchOk) {
                    consumer.commitSync();
                }
            }
        } catch (WakeupException e) {

        } catch (Exception e) {
            log.error("SnapshotProcessor failed", e);
        } finally {
            consumer.close();
            log.info("SnapshotProcessor stopped");
        }
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        consumer.wakeup();
    }
}
