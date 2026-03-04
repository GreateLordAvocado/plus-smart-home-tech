package ru.yandex.practicum.kafka.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    private static final Map<Class<?>, PayloadValueExtractor<?>> EXTRACTORS_BY_CLASS = Map.of(
            MotionSensorAvro.class, new MotionExtractor(),
            SwitchSensorAvro.class, new SwitchExtractor(),
            LightSensorAvro.class, new LightExtractor(),
            TemperatureSensorAvro.class, new TemperatureExtractor(),
            ClimateSensorAvro.class, new ClimateExtractor()
    );

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

            Integer sensorValue;
            try {
                sensorValue = extractValue(sensorState.getData(), cond.getType());
            } catch (IllegalArgumentException ex) {
                log.debug("Cannot extract value for scenario={}, sensorId={}, conditionType={}, reason={}",
                        scenario.getName(), sensorId, cond.getType(), ex.getMessage());
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
        if (payload == null) {
            throw new IllegalArgumentException("payload is null");
        }
        if (type == null) {
            throw new IllegalArgumentException("condition type is null");
        }

        PayloadValueExtractor<Object> extractor = findExtractor(payload);
        return extractor.extract(payload, type);
    }

    @SuppressWarnings("unchecked")
    private PayloadValueExtractor<Object> findExtractor(Object payload) {
        PayloadValueExtractor<?> extractor = EXTRACTORS_BY_CLASS.get(payload.getClass());
        if (extractor == null) {
            throw new IllegalArgumentException("unsupported payload class: " + payload.getClass().getName());
        }
        return (PayloadValueExtractor<Object>) extractor;
    }

    private interface PayloadValueExtractor<T> {
        Integer extract(T payload, ConditionType type);
    }

    private static final class MotionExtractor implements PayloadValueExtractor<MotionSensorAvro> {
        @Override
        public Integer extract(MotionSensorAvro payload, ConditionType type) {
            if (type != ConditionType.MOTION) {
                throw new IllegalArgumentException("conditionType " + type + " is not supported for MotionSensorAvro");
            }
            return payload.getMotion() ? 1 : 0;
        }
    }

    private static final class SwitchExtractor implements PayloadValueExtractor<SwitchSensorAvro> {
        @Override
        public Integer extract(SwitchSensorAvro payload, ConditionType type) {
            if (type != ConditionType.SWITCH) {
                throw new IllegalArgumentException("conditionType " + type + " is not supported for SwitchSensorAvro");
            }
            return payload.getState() ? 1 : 0;
        }
    }

    private static final class LightExtractor implements PayloadValueExtractor<LightSensorAvro> {
        @Override
        public Integer extract(LightSensorAvro payload, ConditionType type) {
            if (type != ConditionType.LUMINOSITY) {
                throw new IllegalArgumentException("conditionType " + type + " is not supported for LightSensorAvro");
            }
            return payload.getLuminosity();
        }
    }

    private static final class TemperatureExtractor implements PayloadValueExtractor<TemperatureSensorAvro> {
        @Override
        public Integer extract(TemperatureSensorAvro payload, ConditionType type) {
            if (type != ConditionType.TEMPERATURE) {
                throw new IllegalArgumentException("conditionType " + type + " is not supported for TemperatureSensorAvro");
            }
            return payload.getTemperatureC();
        }
    }

    private static final class ClimateExtractor implements PayloadValueExtractor<ClimateSensorAvro> {
        private static final Map<ConditionType, java.util.function.Function<ClimateSensorAvro, Integer>> CLIMATE_MAP =
                new EnumMap<>(ConditionType.class);

        static {
            CLIMATE_MAP.put(ConditionType.TEMPERATURE, ClimateSensorAvro::getTemperatureC);
            CLIMATE_MAP.put(ConditionType.CO2LEVEL, ClimateSensorAvro::getCo2Level);
            CLIMATE_MAP.put(ConditionType.HUMIDITY, ClimateSensorAvro::getHumidity);
        }

        @Override
        public Integer extract(ClimateSensorAvro payload, ConditionType type) {
            var fn = CLIMATE_MAP.get(type);
            if (fn == null) {
                throw new IllegalArgumentException("conditionType " + type + " is not supported for ClimateSensorAvro");
            }
            return fn.apply(payload);
        }
    }
}