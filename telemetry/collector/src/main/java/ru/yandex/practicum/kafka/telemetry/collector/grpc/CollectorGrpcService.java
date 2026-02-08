package ru.yandex.practicum.kafka.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.kafka.TelemetryKafkaProducer;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@GrpcService
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final TelemetryKafkaProducer producer;

    public CollectorGrpcService(TelemetryKafkaProducer producer) {
        this.producer = producer;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        SensorEventAvro avro = toSensorEventAvro(request);
        if (avro != null) {
            producer.sendSensor(avro);
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        HubEventAvro avro = toHubEventAvro(request);
        if (avro != null) {
            producer.sendHub(avro);
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static long toEpochMillis(Timestamp ts) {
        if (ts == null) {
            return Instant.now().toEpochMilli();
        }
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }

    private static SensorEventAvro toSensorEventAvro(SensorEventProto p) {
        if (!StringUtils.hasText(p.getId()) || !StringUtils.hasText(p.getHubId())) {
            return null;
        }

        Object payload = switch (p.getPayloadCase()) {
            case CLIMATE -> {
                ClimateSensorAvro e = new ClimateSensorAvro();
                e.setTemperatureC(p.getClimate().getTemperatureC());
                e.setHumidity(p.getClimate().getHumidity());
                e.setCo2Level(p.getClimate().getCo2Level());
                yield e;
            }
            case LIGHT -> {
                LightSensorAvro e = new LightSensorAvro();
                e.setLinkQuality(p.getLight().getLinkQuality());
                e.setLuminosity(p.getLight().getLuminosity());
                yield e;
            }
            case MOTION -> {
                MotionSensorAvro e = new MotionSensorAvro();
                e.setLinkQuality(p.getMotion().getLinkQuality());
                e.setMotion(p.getMotion().getMotion());
                e.setVoltage(p.getMotion().getVoltage());
                yield e;
            }
            case SWITCH_SENSOR -> {
                SwitchSensorAvro e = new SwitchSensorAvro();
                e.setState(p.getSwitchSensor().getState());
                yield e;
            }
            case TEMPERATURE -> {
                TemperatureSensorAvro e = new TemperatureSensorAvro();
                e.setTemperatureC(p.getTemperature().getTemperatureC());
                e.setTemperatureF(p.getTemperature().getTemperatureF());
                yield e;
            }
            case PAYLOAD_NOT_SET -> null;
        };

        if (payload == null) return null;

        SensorEventAvro avro = new SensorEventAvro();
        avro.setId(p.getId());
        avro.setHubId(p.getHubId());
        avro.setTimestamp(toEpochMillis(p.getTimestamp()));
        avro.setPayload(payload);
        return avro;
    }

    private static HubEventAvro toHubEventAvro(HubEventProto p) {
        if (!StringUtils.hasText(p.getHubId())) {
            return null;
        }

        Object payload = switch (p.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventAvro e = new DeviceAddedEventAvro();
                e.setId(p.getDeviceAdded().getId());
                e.setType(DeviceTypeAvro.valueOf(p.getDeviceAdded().getType().name()));
                yield e;
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventAvro e = new DeviceRemovedEventAvro();
                e.setId(p.getDeviceRemoved().getId());
                yield e;
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventAvro e = new ScenarioAddedEventAvro();
                e.setName(p.getScenarioAdded().getName());

                List<ScenarioConditionAvro> conditions = new ArrayList<>();
                p.getScenarioAdded().getConditionsList().forEach(c -> {
                    ScenarioConditionAvro ca = new ScenarioConditionAvro();
                    ca.setSensorId(c.getSensorId());
                    ca.setType(ConditionTypeAvro.valueOf(c.getType().name()));
                    ca.setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));

                    Object value = switch (c.getValueCase()) {
                        case INT_VALUE -> c.getIntValue();
                        case BOOL_VALUE -> c.getBoolValue();
                        case VALUE_NOT_SET -> null;
                    };

                    ca.setValue(value);
                    conditions.add(ca);
                });

                List<DeviceActionAvro> actions = new ArrayList<>();
                p.getScenarioAdded().getActionsList().forEach(a -> {
                    DeviceActionAvro aa = new DeviceActionAvro();
                    aa.setSensorId(a.getSensorId());
                    aa.setType(ActionTypeAvro.valueOf(a.getType().name()));

                    // FIX: value в proto = optional int32, значит используем hasValue()
                    aa.setValue(a.hasValue() ? a.getValue() : null);

                    actions.add(aa);
                });

                e.setConditions(conditions);
                e.setActions(actions);
                yield e;
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventAvro e = new ScenarioRemovedEventAvro();
                e.setName(p.getScenarioRemoved().getName());
                yield e;
            }
            case PAYLOAD_NOT_SET -> null;
        };

        if (payload == null) return null;

        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(p.getHubId());
        avro.setTimestamp(toEpochMillis(p.getTimestamp()));
        avro.setPayload(payload);
        return avro;
    }
}
