package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SnapshotAggregationService {

    private final Map<String, Map<String, Object>> state = new ConcurrentHashMap<>();

    public Optional<SnapshotAvro> onSensorEvent(SensorEventAvro event) {
        if (event == null) {
            return Optional.empty();
        }

        final String hubId = event.getHubId().toString();
        final String sensorId = event.getId().toString();
        final Object payload = event.getPayload();

        Map<String, Object> hubSensors =
                state.computeIfAbsent(hubId, __ -> new ConcurrentHashMap<>());

        Object prevPayload = hubSensors.get(sensorId);

        if (prevPayload != null && Objects.equals(prevPayload, payload)) {
            return Optional.empty();
        }

        hubSensors.put(sensorId, payload);

        SnapshotAvro snapshot = SnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(event.getTimestamp())
                .setSensors(new HashMap<>(hubSensors))
                .build();

        return Optional.of(snapshot);
    }
}
