package ru.yandex.practicum.kafka.telemetry.aggregator.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka")
@Data
public class AggregatorKafkaProperties {

    private String bootstrapServers;
    private Consumer consumer = new Consumer();
    private Producer producer = new Producer();
    private Topics topics = new Topics();

    @Data
    public static class Consumer {
        private String groupId;
        private boolean enableAutoCommit;
        private String autoOffsetReset;
        private int maxPollRecords;
    }

    @Data
    public static class Producer {
        private String acks = "all";
        private int lingerMs = 5;
        private int batchSize = 16384;
    }

    @Data
    public static class Topics {
        private String sensors;
        private String snapshots;
    }
}
