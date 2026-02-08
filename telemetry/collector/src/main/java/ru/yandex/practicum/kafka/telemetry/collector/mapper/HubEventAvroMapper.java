package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ConditionOperation;
import ru.yandex.practicum.grpc.telemetry.event.ConditionType;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceType;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto;

import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.time.Instant;
import java.util.List;

import static ru.yandex.practicum.grpc.telemetry.event.HubEventProto.PayloadCase.*;

@Component
public class HubEventAvroMapper {

    public HubEventAvro toAvro(HubEventProto event) {
        final Object payload = switch (event.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(event.getDeviceAdded());
            case DEVICE_REMOVED -> mapDeviceRemoved(event.getDeviceRemoved());
            case SCENARIO_ADDED -> mapScenarioAdded(event.getScenarioAdded());
            case SCENARIO_REMOVED -> mapScenarioRemoved(event.getScenarioRemoved());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("HubEvent payload is not set");
        };

        final long ts = event.hasTimestamp()
                ? event.getTimestamp().getSeconds() * 1000L + event.getTimestamp().getNanos() / 1_000_000L
                : Instant.now().toEpochMilli();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro mapDeviceAdded(DeviceAddedEventProto e) {
        DeviceType t = e.getType();
        return DeviceAddedEventAvro.newBuilder()
                .setId(e.getId())
                .setType(DeviceTypeAvro.valueOf(t.name()))
                .build();
    }

    private DeviceRemovedEventAvro mapDeviceRemoved(DeviceRemovedEventProto e) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(e.getId())
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAdded(ScenarioAddedEventProto e) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(e.getName())
                .setConditions(mapConditions(e.getConditionsList()))
                .setActions(mapActions(e.getActionsList()))
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemoved(ScenarioRemovedEventProto e) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(e.getName())
                .build();
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioConditionProto> conditions) {
        return conditions.stream().map(this::mapCondition).toList();
    }

    private ScenarioConditionAvro mapCondition(ScenarioConditionProto c) {
        ConditionType type = c.getType();
        ConditionOperation op = c.getOperation();

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(c.getSensorId())
                .setType(ConditionTypeAvro.valueOf(type.name()))
                .setOperation(ConditionOperationAvro.valueOf(op.name()))
                .setValue(mapConditionValue(c))
                .build();
    }

    private Object mapConditionValue(ScenarioConditionProto c) {
        return switch (c.getValueCase()) {
            case INT_VALUE -> c.getIntValue();
            case BOOL_VALUE -> c.getBoolValue();
            case VALUE_NOT_SET -> null;
        };
    }

    private List<DeviceActionAvro> mapActions(List<DeviceActionProto> actions) {
        return actions.stream().map(this::mapAction).toList();
    }

    private DeviceActionAvro mapAction(DeviceActionProto a) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(a.getSensorId())
                .setType(ActionTypeAvro.valueOf(a.getType().name()))
                .setValue(a.getValue())
                .build();
    }
}
