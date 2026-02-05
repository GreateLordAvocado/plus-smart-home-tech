package ru.yandex.practicum.kafka.telemetry.analyzer.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzerKafkaProperties {

    private String bootstrapServers;
    private Consumers consumers = new Consumers();

    @Getter
    @Setter
    public static class Consumers {
        private Consumer hubEvents = new Consumer();
        private Consumer snapshots = new Consumer();
    }

    @Getter
    @Setter
    public static class Consumer {
        private String topic;
        private String groupId;
        private int pollTimeoutMs = 1000;
        private int maxPollRecords = 200;
        private boolean enableAutoCommit = true;
    }
}
