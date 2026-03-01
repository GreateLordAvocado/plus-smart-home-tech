package ru.yandex.practicum.kafka.telemetry.aggregator.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.aggregator.kafka.AggregatorKafkaProperties;
import ru.yandex.practicum.kafka.telemetry.aggregator.kafka.AvroBytesSerializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
public class SnapshotProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final AvroBytesSerializer serializer;
    private final AggregatorKafkaProperties props;

    public SnapshotProducer(KafkaTemplate<String, byte[]> kafkaTemplate,
                            AvroBytesSerializer serializer,
                            AggregatorKafkaProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.serializer = serializer;
        this.props = props;
    }

    public void send(SensorsSnapshotAvro snapshot) {
        byte[] payload = serializer.serialize(snapshot);
        String topic = props.getTopics().getSnapshots();

        kafkaTemplate.send(topic, snapshot.getHubId().toString(), payload);
    }
}
