package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SnapshotAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SnapshotAggregationService {

    private final Map<String, Map<String, SensorEventAvro>> state = new ConcurrentHashMap<>();

    public Optional<SnapshotAvro> onSensorEvent(SensorEventAvro event) {
        String hubId = event.getHubId().toString();
        String sensorId = event.getId().toString();

        Map<String, SensorEventAvro> hubSensors =
                state.computeIfAbsent(hubId, __ -> new ConcurrentHashMap<>());

        SensorEventAvro prev = hubSensors.get(sensorId);

        if (prev != null && Objects.equals(prev.getPayload(), event.getPayload())) {
            return Optional.empty();
        }

        hubSensors.put(sensorId, event);

        SnapshotAvro snapshot = SnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(Instant.now().toEpochMilli())
                .setSensors(new ArrayList<>(hubSensors.values()))
                .build();

        return Optional.of(snapshot);
    }
}
