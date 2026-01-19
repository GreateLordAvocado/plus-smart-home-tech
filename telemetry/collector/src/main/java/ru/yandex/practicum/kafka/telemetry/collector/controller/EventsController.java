package ru.yandex.practicum.kafka.telemetry.collector.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.kafka.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.kafka.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.kafka.TelemetryKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.collector.mapper.HubEventAvroMapper;
import ru.yandex.practicum.kafka.telemetry.collector.mapper.SensorEventAvroMapper;

@RestController
@RequestMapping("/events")
public class EventsController {

    private final SensorEventAvroMapper sensorMapper;
    private final HubEventAvroMapper hubMapper;
    private final TelemetryKafkaProducer kafkaProducer;

    public EventsController(SensorEventAvroMapper sensorMapper,
                            HubEventAvroMapper hubMapper,
                            TelemetryKafkaProducer kafkaProducer) {
        this.sensorMapper = sensorMapper;
        this.hubMapper = hubMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        var avro = sensorMapper.toAvro(event);
        kafkaProducer.sendSensor(avro);
        return ResponseEntity.ok().build(); // 200 OK по спецификации
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        var avro = hubMapper.toAvro(event);
        kafkaProducer.sendHub(avro);
        return ResponseEntity.ok().build(); // 200 OK по спецификации
    }
}
