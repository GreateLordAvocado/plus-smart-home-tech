package ru.yandex.practicum.kafka.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.ScenarioActionLink;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.ScenarioActionLinkId;

import java.util.List;

public interface ScenarioActionLinkRepository
        extends JpaRepository<ScenarioActionLink, ScenarioActionLinkId> {

    List<ScenarioActionLink> findByIdScenarioId(Long scenarioId);

    void deleteByIdScenarioId(Long scenarioId);

    void deleteByIdSensorId(String sensorId);
}
