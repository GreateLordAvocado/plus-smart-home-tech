package ru.yandex.practicum.kafka.telemetry.collector.dto.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceAddedEvent.class, name = "DEVICE_ADDED"),
        @JsonSubTypes.Type(value = DeviceRemovedEvent.class, name = "DEVICE_REMOVED"),
        @JsonSubTypes.Type(value = ScenarioAddedEvent.class, name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type(value = ScenarioRemovedEvent.class, name = "SCENARIO_REMOVED")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class HubEvent {

    @NotBlank
    private String hubId;

    private Instant timestamp;

    @NotNull
    public abstract HubEventType getType();

    public String getHubId() {
        return hubId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
