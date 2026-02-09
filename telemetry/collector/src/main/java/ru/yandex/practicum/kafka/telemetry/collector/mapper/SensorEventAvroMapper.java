package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
public class SensorEventAvroMapper {

    public SensorEventAvro toAvro(SensorEventProto event) {
        if (event == null) {
            return null;
        }
        if (!StringUtils.hasText(event.getId())) {
            return null;
        }
        if (!StringUtils.hasText(event.getHubId())) {
            return null;
        }

        final Object payload = switch (event.getPayloadCase()) {
            case LIGHT_SENSOR -> mapLight(event.getLightSensor());
            case MOTION_SENSOR -> mapMotion(event.getMotionSensor());
            case SWITCH_SENSOR -> mapSwitch(event.getSwitchSensor());
            case TEMPERATURE_SENSOR -> mapTemperature(event.getTemperatureSensor());
            case CLIMATE_SENSOR -> mapClimate(event.getClimateSensor());
            case PAYLOAD_NOT_SET -> null;
        };

        if (payload == null) {
            return null;
        }

        final long ts = event.hasTimestamp()
                ? Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()).toEpochMilli()
                : Instant.now().toEpochMilli();

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private LightSensorAvro mapLight(LightSensorProto e) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(e.getLinkQuality())
                .setLuminosity(e.getLuminosity())
                .build();
    }

    private MotionSensorAvro mapMotion(MotionSensorProto e) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(e.getLinkQuality())
                .setMotion(e.getMotion())
                .setVoltage(e.getVoltage())
                .build();
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorProto e) {
        return SwitchSensorAvro.newBuilder()
                .setState(e.getState())
                .build();
    }

    private TemperatureSensorAvro mapTemperature(TemperatureSensorProto e) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(e.getTemperatureC())
                .setTemperatureF(e.getTemperatureF())
                .build();
    }

    private ClimateSensorAvro mapClimate(ClimateSensorProto e) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(e.getTemperatureC())
                .setHumidity(e.getHumidity())
                .setCo2Level(e.getCo2Level())
                .build();
    }
}
