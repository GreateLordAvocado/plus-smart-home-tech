package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;

import static ru.yandex.practicum.grpc.telemetry.event.SensorEventProto.PayloadCase.*;

@Component
public class SensorEventAvroMapper {

    public SensorEventAvro toAvro(SensorEventProto event) {
        final Object payload = switch (event.getPayloadCase()) {
            case LIGHT -> mapLight(event.getLight());
            case MOTION -> mapMotion(event.getMotion());
            case SWITCH_SENSOR -> mapSwitch(event.getSwitchSensor());
            case TEMPERATURE -> mapTemperature(event.getTemperature());
            case CLIMATE -> mapClimate(event.getClimate());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("SensorEvent payload is not set");
        };

        final long ts = event.hasTimestamp()
                ? event.getTimestamp().getSeconds() * 1000L + event.getTimestamp().getNanos() / 1_000_000L
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
