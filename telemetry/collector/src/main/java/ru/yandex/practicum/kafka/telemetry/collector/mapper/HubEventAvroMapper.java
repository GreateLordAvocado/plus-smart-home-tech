package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.collector.dto.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

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

        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp().toEpochMilli());
        avro.setPayload(payload);
        return avro;
    }

    private DeviceAddedEventAvro toDeviceAdded(DeviceAddedEvent e) {
        DeviceAddedEventAvro a = new DeviceAddedEventAvro();
        a.setId(e.getId());
        a.setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()));
        return a;
    }

    private DeviceRemovedEventAvro toDeviceRemoved(DeviceRemovedEvent e) {
        DeviceRemovedEventAvro a = new DeviceRemovedEventAvro();
        a.setId(e.getId());
        return a;
    }

    private ScenarioAddedEventAvro toScenarioAdded(ScenarioAddedEvent e) {
        ScenarioAddedEventAvro a = new ScenarioAddedEventAvro();
        a.setName(e.getName());
        a.setConditions(mapConditions(e.getConditions()));
        a.setActions(mapActions(e.getActions()));
        return a;
    }

    private ScenarioRemovedEventAvro toScenarioRemoved(ScenarioRemovedEvent e) {
        ScenarioRemovedEventAvro a = new ScenarioRemovedEventAvro();
        a.setName(e.getName());
        return a;
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioCondition> conditions) {
        return conditions.stream().map(this::toCondition).toList();
    }

    private ScenarioConditionAvro toCondition(ScenarioCondition c) {
        ScenarioConditionAvro a = new ScenarioConditionAvro();
        a.setSensorId(c.getSensorId());
        a.setType(ConditionTypeAvro.valueOf(c.getType().name()));
        a.setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));
        a.setValue(mapUnionValue(c.getValue()));
        return a;
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
        DeviceActionAvro av = new DeviceActionAvro();
        av.setSensorId(a.getSensorId());
        av.setType(ActionTypeAvro.valueOf(a.getType().name()));
        av.setValue(a.getValue());
        return av;
    }
}
