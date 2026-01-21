package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import lombok.Data;

@Data
public class SwitchSensorEvent extends SensorEvent {

    private boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}
