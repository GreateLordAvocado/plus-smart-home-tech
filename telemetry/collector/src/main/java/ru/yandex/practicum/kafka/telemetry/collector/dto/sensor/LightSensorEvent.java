package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import jakarta.validation.constraints.Min;

public class LightSensorEvent extends SensorEvent {

    @Min(0)
    private int linkQuality;

    @Min(0)
    private int luminosity;

    public int getLinkQuality() {
        return linkQuality;
    }

    public void setLinkQuality(int linkQuality) {
        this.linkQuality = linkQuality;
    }

    public int getLuminosity() {
        return luminosity;
    }

    public void setLuminosity(int luminosity) {
        this.luminosity = luminosity;
    }

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}
