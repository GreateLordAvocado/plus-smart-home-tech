package ru.yandex.practicum.kafka.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.analyzer.model.*;
import ru.yandex.practicum.kafka.telemetry.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

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

    private static final Map<ConditionType, Function<Object, Integer>> VALUE_EXTRACTORS =
            new EnumMap<>(ConditionType.class);

    static {
        VALUE_EXTRACTORS.put(ConditionType.MOTION, payload -> {
            if (payload instanceof MotionSensorAvro m) {
                return m.getMotion() ? 1 : 0;
            }
            return null;
        });

        VALUE_EXTRACTORS.put(ConditionType.SWITCH, payload -> {
            if (payload instanceof SwitchSensorAvro s) {
                return s.getState() ? 1 : 0;
            }
            return null;
        });

        VALUE_EXTRACTORS.put(ConditionType.LUMINOSITY, payload -> {
            if (payload instanceof LightSensorAvro l) {
                return l.getLuminosity();
            }
            return null;
        });

        VALUE_EXTRACTORS.put(ConditionType.TEMPERATURE, payload -> {
            if (payload instanceof TemperatureSensorAvro t) {
                return t.getTemperatureC();
            }
            if (payload instanceof ClimateSensorAvro c) {
                return c.getTemperatureC();
            }
            return null;
        });

        VALUE_EXTRACTORS.put(ConditionType.CO2LEVEL, payload -> {
            if (payload instanceof ClimateSensorAvro c) {
                return c.getCo2Level();
            }
            return null;
        });

        VALUE_EXTRACTORS.put(ConditionType.HUMIDITY, payload -> {
            if (payload instanceof ClimateSensorAvro c) {
                return c.getHumidity();
            }
            return null;
        });
    }

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

            Integer sensorValue = extractValue(sensorState.getData(), cond.getType());
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

    private Integer extractValue(Object payload, ConditionType type) {
        Function<Object, Integer> extractor = VALUE_EXTRACTORS.get(type);
        if (extractor == null) {
            return null;
        }
        return extractor.apply(payload);
    }
}