package ru.yandex.practicum.kafka.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.*;
import ru.yandex.practicum.kafka.telemetry.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionLinkRepository scenarioConditionLinkRepository;
    private final ScenarioActionLinkRepository scenarioActionLinkRepository;

    @Transactional
    public void handle(HubEventAvro event) {
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro e) {
            handleDeviceAdded(event.getHubId(), e);
            return;
        }
        if (payload instanceof DeviceRemovedEventAvro e) {
            handleDeviceRemoved(event.getHubId(), e);
            return;
        }
        if (payload instanceof ScenarioAddedEventAvro e) {
            handleScenarioAdded(event.getHubId(), e);
            return;
        }
        if (payload instanceof ScenarioRemovedEventAvro e) {
            handleScenarioRemoved(event.getHubId(), e);
            return;
        }

        log.warn("Unknown hub event payload type: {}", payload == null ? "null" : payload.getClass().getName());
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro e) {
        String sensorId = e.getId();

        sensorRepository.findById(sensorId).ifPresentOrElse(existing -> {
            if (existing.getHubId() == null || !existing.getHubId().equals(hubId)) {
                existing.setHubId(hubId);
                sensorRepository.save(existing);
            }
        }, () -> {
            Sensor s = new Sensor();
            s.setId(sensorId);
            s.setHubId(hubId);
            sensorRepository.save(s);
        });

        log.info("Device added: hubId={}, sensorId={}, deviceType={}", hubId, sensorId, e.getType());
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro e) {
        String sensorId = e.getId();

        scenarioConditionLinkRepository.deleteByIdSensorId(sensorId);
        scenarioActionLinkRepository.deleteByIdSensorId(sensorId);

        sensorRepository.deleteById(sensorId);

        log.info("Device removed: hubId={}, sensorId={}", hubId, sensorId);
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro e) {
        String name = e.getName();

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, name)
                .orElseGet(Scenario::new);

        scenario.setHubId(hubId);
        scenario.setName(name);

        Scenario savedScenario = scenarioRepository.save(scenario);

        scenarioConditionLinkRepository.deleteByIdScenarioId(savedScenario.getId());
        scenarioActionLinkRepository.deleteByIdScenarioId(savedScenario.getId());

        Set<String> allSensorIds = new HashSet<>();
        e.getConditions().forEach(c -> allSensorIds.add(c.getSensorId()));
        e.getActions().forEach(a -> allSensorIds.add(a.getSensorId()));

        upsertSensorsForScenario(hubId, allSensorIds);

        // conditions
        e.getConditions().forEach(c -> {
            Condition cond = new Condition();
            cond.setType(ConditionType.valueOf(c.getType().name()));
            cond.setOperation(ConditionOperation.valueOf(c.getOperation().name()));
            cond.setValue(mapConditionValueToInteger(c.getValue()));

            Condition savedCond = conditionRepository.save(cond);

            ScenarioConditionLink link = new ScenarioConditionLink();
            ScenarioConditionLinkId id = new ScenarioConditionLinkId();
            id.setScenarioId(savedScenario.getId());
            id.setSensorId(c.getSensorId());
            id.setConditionId(savedCond.getId());
            link.setId(id);

            scenarioConditionLinkRepository.save(link);
        });

        // actions
        e.getActions().forEach(a -> {
            Action act = new Action();
            act.setType(ActionType.valueOf(a.getType().name()));
            act.setValue(a.getValue());

            Action savedAct = actionRepository.save(act);

            ScenarioActionLink link = new ScenarioActionLink();
            ScenarioActionLinkId id = new ScenarioActionLinkId();
            id.setScenarioId(savedScenario.getId());
            id.setSensorId(a.getSensorId());
            id.setActionId(savedAct.getId());
            link.setId(id);

            scenarioActionLinkRepository.save(link);
        });

        log.info("Scenario saved: hubId={}, name={}, conditions={}, actions={}",
                hubId, name, e.getConditions().size(), e.getActions().size());
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro e) {
        String name = e.getName();

        scenarioRepository.findByHubIdAndName(hubId, name).ifPresent(s -> {
            scenarioConditionLinkRepository.deleteByIdScenarioId(s.getId());
            scenarioActionLinkRepository.deleteByIdScenarioId(s.getId());
            scenarioRepository.delete(s);
            log.info("Scenario removed: hubId={}, name={}", hubId, name);
        });
    }

    private void upsertSensorsForScenario(String hubId, Set<String> sensorIds) {
        for (String sensorId : sensorIds) {
            sensorRepository.findById(sensorId).ifPresentOrElse(existing -> {
                if (existing.getHubId() == null || !existing.getHubId().equals(hubId)) {
                    existing.setHubId(hubId);
                    sensorRepository.save(existing);
                }
            }, () -> {
                Sensor s = new Sensor();
                s.setId(sensorId);
                s.setHubId(hubId);
                sensorRepository.save(s);
            });
        }
    }

    private Integer mapConditionValueToInteger(Object unionValue) {
        if (unionValue == null) {
            return null;
        }
        if (unionValue instanceof Boolean b) {
            return b ? 1 : 0;
        }
        if (unionValue instanceof Integer i) {
            return i;
        }
        if (unionValue instanceof Long l) {
            return l.intValue();
        }
        throw new IllegalArgumentException("Unsupported condition union value: " + unionValue.getClass().getName());
    }
}
