package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class SensorEventAvroMapper {

    public SensorEventAvro toAvro(SensorEventProto event) {
        if (event == null) {
            return null;
        }
        if (event.getId() == null || event.getId().isEmpty()) {
            return null;
        }
        if (!StringUtils.hasText(event.getHubId())) {
            return null;
        }

        final Object payload = switch (event.getPayloadCase()) {
            case LIGHT -> mapLight(event.getLight());
            case MOTION -> mapMotion(event.getMotion());
            case SWITCH_SENSOR -> mapSwitch(event.getSwitchSensor());
            case TEMPERATURE -> mapTemperature(event.getTemperature());
            case CLIMATE -> mapClimate(event.getClimate());
            case PAYLOAD_NOT_SET -> null;
        };

        if (payload == null) {
            return null;
        }

        final long ts = event.hasTimestamp()
                ? Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()).toEpochMilli()
                : Instant.now().toEpochMilli();

        return SensorEventAvro.newBuilder()
                .setId(sensorIdToString(event.getId()))
                .setHubId(event.getHubId())
                .setTimestamp(ts)
                .setPayload(payload)
                .build();
    }

    private static String sensorIdToString(ByteString id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("SensorEventProto: id must be set");
        }

        byte[] bytes = id.toByteArray();

        if (bytes.length == 16) {
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            long msb = bb.getLong();
            long lsb = bb.getLong();
            return new UUID(msb, lsb).toString();
        }

        return new String(bytes, StandardCharsets.UTF_8);
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
