package ru.yandex.practicum.kafka.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.ScenarioConditionLink;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.ScenarioConditionLinkId;

import java.util.List;

public interface ScenarioConditionLinkRepository
        extends JpaRepository<ScenarioConditionLink, ScenarioConditionLinkId> {

    List<ScenarioConditionLink> findByIdScenarioId(Long scenarioId);

    void deleteByIdScenarioId(Long scenarioId);

    void deleteByIdSensorId(String sensorId);
}
