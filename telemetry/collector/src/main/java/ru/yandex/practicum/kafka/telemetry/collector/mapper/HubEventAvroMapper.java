package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.collector.dto.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

@Component
public class HubEventAvroMapper {

    public HubEventAvro toAvro(HubEvent dto) {
        Object payload = switch (dto.getType()) {
            case DEVICE_ADDED -> toDeviceAdded((DeviceAddedEvent) dto);
            case DEVICE_REMOVED -> toDeviceRemoved((DeviceRemovedEvent) dto);
            case SCENARIO_ADDED -> toScenarioAdded((ScenarioAddedEvent) dto);
            case SCENARIO_REMOVED -> toScenarioRemoved((ScenarioRemovedEvent) dto);
        };

        long ts = dto.getTimestamp() != null
                ? dto.getTimestamp().toEpochMilli()
                : Instant.now().toEpochMilli();

        return HubEventAvro.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private DeviceAddedEventAvro toDeviceAdded(DeviceAddedEvent e) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(e.getId())
                .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                .build();
    }

    private DeviceRemovedEventAvro toDeviceRemoved(DeviceRemovedEvent e) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(e.getId())
                .build();
    }

    private ScenarioAddedEventAvro toScenarioAdded(ScenarioAddedEvent e) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(e.getName())
                .setConditions(mapConditions(e.getConditions()))
                .setActions(mapActions(e.getActions()))
                .build();
    }

    private ScenarioRemovedEventAvro toScenarioRemoved(ScenarioRemovedEvent e) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(e.getName())
                .build();
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioCondition> conditions) {
        return conditions.stream().map(this::toCondition).toList();
    }

    private ScenarioConditionAvro toCondition(ScenarioCondition c) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(c.getSensorId())
                .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                .setValue(mapUnionValue(c.getValue()))
                .build();
    }

    private Object mapUnionValue(JsonNode value) {
        if (value == null || value.isNull()) return null;
        if (value.isBoolean()) return value.booleanValue();
        if (value.isInt() || value.isLong()) return value.intValue();
        throw new IllegalArgumentException("Unsupported condition value type: " + value.getNodeType());
    }

    private List<DeviceActionAvro> mapActions(List<DeviceAction> actions) {
        return actions.stream().map(this::toAction).toList();
    }

    private DeviceActionAvro toAction(DeviceAction a) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(a.getSensorId())
                .setType(ActionTypeAvro.valueOf(a.getType().name()))
                .setValue(a.getValue())
                .build();
    }
}
