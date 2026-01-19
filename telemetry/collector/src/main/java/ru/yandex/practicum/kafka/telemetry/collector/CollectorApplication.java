package ru.yandex.practicum.kafka.telemetry.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.yandex.practicum.kafka.telemetry.collector.kafka.CollectorKafkaProperties;

@SpringBootApplication
@EnableConfigurationProperties(CollectorKafkaProperties.class)
public class CollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }
}
