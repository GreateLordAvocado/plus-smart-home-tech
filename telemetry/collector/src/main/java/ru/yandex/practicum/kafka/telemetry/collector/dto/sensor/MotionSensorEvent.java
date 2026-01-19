package ru.yandex.practicum.kafka.telemetry.collector.dto.sensor;

import jakarta.validation.constraints.Min;

public class MotionSensorEvent extends SensorEvent {

    @Min(0)
    private int linkQuality;

    private boolean motion;

    @Min(0)
    private int voltage;

    public int getLinkQuality() {
        return linkQuality;
    }
    public void setLinkQuality(int linkQuality) {
        this.linkQuality = linkQuality;
    }

    public boolean isMotion() {
        return motion;
    }
    public void setMotion(boolean motion) {
        this.motion = motion;
    }

    public int getVoltage() {
        return voltage;
    }
    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
