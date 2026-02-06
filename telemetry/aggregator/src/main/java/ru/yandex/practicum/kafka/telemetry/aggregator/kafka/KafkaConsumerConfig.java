package ru.yandex.practicum.kafka.telemetry.aggregator.kafka;

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
    public KafkaConsumer<String, byte[]> sensorsConsumer(AggregatorKafkaProperties props) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        p.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumer().getGroupId());
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(props.getConsumer().isEnableAutoCommit()));
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getConsumer().getAutoOffsetReset());
        p.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(props.getConsumer().getMaxPollRecords()));

        p.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");
        p.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");

        return new KafkaConsumer<>(p);
    }
}
