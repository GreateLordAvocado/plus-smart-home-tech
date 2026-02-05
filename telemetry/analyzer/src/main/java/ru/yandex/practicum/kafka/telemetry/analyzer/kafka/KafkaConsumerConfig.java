package ru.yandex.practicum.kafka.telemetry.analyzer.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaConsumer<String, byte[]> hubEventsConsumer(AnalyzerKafkaProperties props) {
        Properties p = base(props);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumers().getHubEvents().getGroupId());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(props.getConsumers().getHubEvents().isEnableAutoCommit()));
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(props.getConsumers().getHubEvents().getMaxPollRecords()));
        return new KafkaConsumer<>(p);
    }

    @Bean
    public KafkaConsumer<String, byte[]> snapshotsConsumer(AnalyzerKafkaProperties props) {
        Properties p = base(props);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumers().getSnapshots().getGroupId());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(props.getConsumers().getSnapshots().isEnableAutoCommit()));
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(props.getConsumers().getSnapshots().getMaxPollRecords()));
        return new KafkaConsumer<>(p);
    }

    private Properties base(AnalyzerKafkaProperties props) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        p.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        return p;
    }
}
