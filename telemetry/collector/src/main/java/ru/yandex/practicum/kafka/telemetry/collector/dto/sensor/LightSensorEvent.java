package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class LightSensorEvent extends SensorEvent {

    @Min(0)
    private int linkQuality;

    @Min(0)
    private int luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}
