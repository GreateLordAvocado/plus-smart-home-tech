package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import lombok.Data;

@Data
public class TemperatureSensorEvent extends SensorEvent {

    private int temperatureC;
    private int temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}
