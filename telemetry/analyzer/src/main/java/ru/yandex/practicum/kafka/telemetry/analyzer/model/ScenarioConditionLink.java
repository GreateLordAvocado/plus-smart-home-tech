package ru.yandex.practicum.kafka.telemetry.analyzer.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scenario_conditions")
public class ScenarioConditionLink {

    @EmbeddedId
    private ScenarioConditionLinkId id;
}
