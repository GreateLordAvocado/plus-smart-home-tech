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

        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(event.getId());
        avro.setHubId(event.getHubId());
        avro.setTimestamp(ts);
        avro.setPayload(payload);
        return avro;
    }

    private LightSensorAvro mapLight(LightSensorProto e) {
        LightSensorAvro a = new LightSensorAvro();
        a.setLinkQuality(e.getLinkQuality());
        a.setLuminosity(e.getLuminosity());
        return a;
    }

    private MotionSensorAvro mapMotion(MotionSensorProto e) {
        MotionSensorAvro a = new MotionSensorAvro();
        a.setLinkQuality(e.getLinkQuality());
        a.setMotion(e.getMotion());
        a.setVoltage(e.getVoltage());
        return a;
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorProto e) {
        SwitchSensorAvro a = new SwitchSensorAvro();
        a.setState(e.getState());
        return a;
    }

    private TemperatureSensorAvro mapTemperature(TemperatureSensorProto e) {
        TemperatureSensorAvro a = new TemperatureSensorAvro();
        a.setTemperatureC(e.getTemperatureC());
        a.setTemperatureF(e.getTemperatureF());
        return a;
    }

    private ClimateSensorAvro mapClimate(ClimateSensorProto e) {
        ClimateSensorAvro a = new ClimateSensorAvro();
        a.setTemperatureC(e.getTemperatureC());
        a.setHumidity(e.getHumidity());
        a.setCo2Level(e.getCo2Level());
        return a;
    }
}
