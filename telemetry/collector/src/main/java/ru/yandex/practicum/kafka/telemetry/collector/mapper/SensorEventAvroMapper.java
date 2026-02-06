package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.collector.dto.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
public class SensorEventAvroMapper {

    public SensorEventAvro toAvro(SensorEvent dto) {
        Object payload = switch (dto.getType()) {
            case LIGHT_SENSOR_EVENT -> toLight((LightSensorEvent) dto);
            case MOTION_SENSOR_EVENT -> toMotion((MotionSensorEvent) dto);
            case SWITCH_SENSOR_EVENT -> toSwitch((SwitchSensorEvent) dto);
            case TEMPERATURE_SENSOR_EVENT -> toTemperature((TemperatureSensorEvent) dto);
            case CLIMATE_SENSOR_EVENT -> toClimate((ClimateSensorEvent) dto);
        };

        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(dto.getId());
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(
                dto.getTimestamp() != null
                        ? dto.getTimestamp().toEpochMilli()
                        : Instant.now().toEpochMilli()
        );
        avro.setPayload(payload);
        return avro;
    }
    //
    private LightSensorAvro toLight(LightSensorEvent e) {
        LightSensorAvro a = new LightSensorAvro();
        a.setLinkQuality(e.getLinkQuality());
        a.setLuminosity(e.getLuminosity());
        return a;
    }

    private MotionSensorAvro toMotion(MotionSensorEvent e) {
        MotionSensorAvro a = new MotionSensorAvro();
        a.setLinkQuality(e.getLinkQuality());
        a.setMotion(e.isMotion());
        a.setVoltage(e.getVoltage());
        return a;
    }

    private SwitchSensorAvro toSwitch(SwitchSensorEvent e) {
        SwitchSensorAvro a = new SwitchSensorAvro();
        a.setState(e.isState());
        return a;
    }

    private TemperatureSensorAvro toTemperature(TemperatureSensorEvent e) {
        TemperatureSensorAvro a = new TemperatureSensorAvro();
        a.setTemperatureC(e.getTemperatureC());
        a.setTemperatureF(e.getTemperatureF());
        return a;
    }

    private ClimateSensorAvro toClimate(ClimateSensorEvent e) {
        ClimateSensorAvro a = new ClimateSensorAvro();
        a.setTemperatureC(e.getTemperatureC());
        a.setHumidity(e.getHumidity());
        a.setCo2Level(e.getCo2Level());
        return a;
    }
}
