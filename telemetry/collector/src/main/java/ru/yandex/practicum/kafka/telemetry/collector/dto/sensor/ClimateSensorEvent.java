package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import lombok.Data;

@Data
public class ClimateSensorEvent extends SensorEvent {

    private int temperatureC;
    private int humidity;
    private int co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
