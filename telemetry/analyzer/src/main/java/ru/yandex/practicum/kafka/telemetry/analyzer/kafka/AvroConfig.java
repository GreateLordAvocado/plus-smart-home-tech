package ru.yandex.practicum.kafka.telemetry.analyzer.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvroConfig {

    @Bean
    public AvroBytesDeserializer avroBytesDeserializer() {
        return new AvroBytesDeserializer();
    }
}
