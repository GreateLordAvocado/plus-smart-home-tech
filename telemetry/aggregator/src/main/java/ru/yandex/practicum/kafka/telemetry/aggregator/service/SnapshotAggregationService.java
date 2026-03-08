package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SnapshotAggregationService {

    /**
     * hubId -> (sensorId -> last SensorState)
     */
    private final Map<String, Map<String, SensorStateAvro>> state = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> onSensorEvent(SensorEventAvro event) {
        if (event == null || event.getHubId() == null || event.getId() == null) {
            return Optional.empty();
        }

        final String hubId = event.getHubId().toString();
        final String sensorId = event.getId().toString();
        final Object payload = event.getPayload();

        if (payload == null) {
            return Optional.empty();
        }

        final Instant ts = Instant.ofEpochMilli(event.getTimestamp());

        Map<String, SensorStateAvro> hubSensors =
                state.computeIfAbsent(hubId, __ -> new ConcurrentHashMap<>());

        SensorStateAvro prevState = hubSensors.get(sensorId);

        if (prevState != null && Objects.equals(prevState.getData(), payload)) {
            return Optional.empty();
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(ts)
                .setData(payload)
                .build();

        hubSensors.put(sensorId, newState);

        SensorsSnapshotAvro snapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(ts)
                .setSensorsState(new HashMap<>(hubSensors))
                .build();

        return Optional.of(snapshot);
    }
}
