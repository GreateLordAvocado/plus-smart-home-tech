package ru.yandex.practicum.kafka.telemetry.aggregator.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaConsumer<String, byte[]> sensorsConsumer(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        return new KafkaConsumer<>(props);
    }
}