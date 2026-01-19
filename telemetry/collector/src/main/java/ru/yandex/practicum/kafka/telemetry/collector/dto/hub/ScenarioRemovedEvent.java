package ru.yandex.practicum.kafka.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;

public class ScenarioRemovedEvent extends HubEvent {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}
