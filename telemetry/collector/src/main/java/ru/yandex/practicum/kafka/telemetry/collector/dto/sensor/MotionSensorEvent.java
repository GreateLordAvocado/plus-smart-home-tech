package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MotionSensorEvent extends SensorEvent {

    @Min(0)
    private int linkQuality;

    private boolean motion;

    @Min(0)
    private int voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
