package ru.yandex.practicum.kafka.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.*;
import ru.yandex.practicum.kafka.telemetry.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

@Service
@RequiredArgsConstructor
public class ScenarioEvaluationService {

    public record PlannedAction(String sensorId, ActionType type, Integer value, String scenarioName) {
    }

    private final ScenarioRepository scenarioRepository;
    private final ScenarioConditionLinkRepository scenarioConditionLinkRepository;
    private final ScenarioActionLinkRepository scenarioActionLinkRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional(readOnly = true)
    public List<PlannedAction> evaluate(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            return List.of();
        }

        Map<String, SensorStateAvro> states = snapshot.getSensorsState();
        if (states == null || states.isEmpty()) {
            return List.of();
        }

        List<PlannedAction> result = new ArrayList<>();

        for (Scenario scenario : scenarios) {
            if (scenarioMatches(states, scenario)) {
                result.addAll(actionsForScenario(scenario));
            }
        }

        return result;
    }

    private boolean scenarioMatches(Map<String, SensorStateAvro> states, Scenario scenario) {
        var links = scenarioConditionLinkRepository.findByIdScenarioId(scenario.getId());
        if (links.isEmpty()) {
            return false;
        }

        for (var link : links) {
            String sensorId = link.getId().getSensorId();
            Long conditionId = link.getId().getConditionId();

            Condition cond = conditionRepository.findById(conditionId).orElse(null);
            if (cond == null) {
                return false;
            }

            SensorStateAvro sensorState = states.get(sensorId);
            if (sensorState == null || sensorState.getData() == null) {
                return false;
            }

            Integer sensorValue = extractValueByConditionType(sensorState.getData(), cond.getType());
            if (sensorValue == null) {
                return false;
            }

            if (!compare(sensorValue, cond.getValue(), cond.getOperation())) {
                return false;
            }
        }

        return true;
    }

    private List<PlannedAction> actionsForScenario(Scenario scenario) {
        var links = scenarioActionLinkRepository.findByIdScenarioId(scenario.getId());
        if (links.isEmpty()) {
            return List.of();
        }

        List<PlannedAction> actions = new ArrayList<>();
        for (var link : links) {
            String sensorId = link.getId().getSensorId();
            Long actionId = link.getId().getActionId();

            Action act = actionRepository.findById(actionId).orElse(null);
            if (act == null) {
                continue;
            }

            actions.add(new PlannedAction(sensorId, act.getType(), act.getValue(), scenario.getName()));
        }
        return actions;
    }

    private boolean compare(Integer actual, Integer expected, ConditionOperation op) {
        if (expected == null) {
            return false;
        }

        BiPredicate<Integer, Integer> predicate = switch (op) {
            case EQUALS -> Integer::equals;
            case GREATER_THAN -> (a, e) -> a > e;
            case LOWER_THAN -> (a, e) -> a < e;
        };

        return predicate.test(actual, expected);
    }

    private Integer extractValueByConditionType(Object sensorPayload, ConditionType type) {
        if (type == ConditionType.MOTION) {
            if (sensorPayload instanceof MotionSensorAvro m) {
                return m.getMotion() ? 1 : 0;
            }
            return null;
        }

        if (type == ConditionType.SWITCH) {
            if (sensorPayload instanceof SwitchSensorAvro s) {
                return s.getState() ? 1 : 0;
            }
            return null;
        }

        if (type == ConditionType.LUMINOSITY) {
            if (sensorPayload instanceof LightSensorAvro l) {
                return l.getLuminosity();
            }
            return null;
        }

        if (type == ConditionType.TEMPERATURE) {
            if (sensorPayload instanceof TemperatureSensorAvro t) {
                return t.getTemperatureC();
            }
            if (sensorPayload instanceof ClimateSensorAvro c) {
                return c.getTemperatureC();
            }
            return null;
        }

        if (type == ConditionType.CO2LEVEL) {
            if (sensorPayload instanceof ClimateSensorAvro c) {
                return c.getCo2Level();
            }
            return null;
        }

        if (type == ConditionType.HUMIDITY) {
            if (sensorPayload instanceof ClimateSensorAvro c) {
                return c.getHumidity();
            }
            return null;
        }

        return null;
    }
}
