package ru.yandex.practicum.kafka.telemetry.collector.dto.hub;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ScenarioCondition {

    @NotBlank
    private String sensorId;

    @NotNull
    private ConditionType type;

    @NotNull
    private ConditionOperation operation;

    private JsonNode value;

    public String getSensorId() {
        return sensorId;
    }
    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public ConditionType getType() {
        return type;
    }
    public void setType(ConditionType type) {
        this.type = type;
    }

    public ConditionOperation getOperation() {
        return operation;
    }
    public void setOperation(ConditionOperation operation) {
        this.operation = operation;
    }

    public JsonNode getValue() {
        return value;
    }
    public void setValue(JsonNode value) {
        this.value = value;
    }
}
