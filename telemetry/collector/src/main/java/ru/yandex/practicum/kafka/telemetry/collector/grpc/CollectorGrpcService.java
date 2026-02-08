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
        if (ts == null) return Instant.now().toEpochMilli();
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
    }

    private static SensorEventAvro toSensorEventAvro(SensorEventProto p) {
        if (!StringUtils.hasText(p.getId()) || !StringUtils.hasText(p.getHubId())) {
            return null;
        }

        Object payload = switch (p.getPayloadCase()) {
            case CLIMATE -> {
                ClimateSensorAvro c = new ClimateSensorAvro();
                c.setTemperatureC(p.getClimate().getTemperatureC());
                c.setHumidity(p.getClimate().getHumidity());
                c.setCo2Level(p.getClimate().getCo2Level());
                yield c;
            }
            case LIGHT -> {
                LightSensorAvro l = new LightSensorAvro();
                l.setLinkQuality(p.getLight().getLinkQuality());
                l.setLuminosity(p.getLight().getLuminosity());
                yield l;
            }
            case MOTION -> {
                MotionSensorAvro m = new MotionSensorAvro();
                m.setLinkQuality(p.getMotion().getLinkQuality());
                m.setMotion(p.getMotion().getMotion());
                m.setVoltage(p.getMotion().getVoltage());
                yield m;
            }
            case SWITCH_SENSOR -> {
                SwitchSensorAvro s = new SwitchSensorAvro();
                s.setState(p.getSwitchSensor().getState());
                yield s;
            }
            case TEMPERATURE -> {
                TemperatureSensorAvro t = new TemperatureSensorAvro();
                t.setTemperatureC(p.getTemperature().getTemperatureC());
                t.setTemperatureF(p.getTemperature().getTemperatureF());
                yield t;
            }
            case PAYLOAD_NOT_SET -> null;
        };

        if (payload == null) {
            return null;
        }

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

                ArrayList<ScenarioConditionAvro> conditions = new ArrayList<>();
                for (var c : p.getScenarioAdded().getConditionsList()) {
                    ScenarioConditionAvro ca = new ScenarioConditionAvro();
                    ca.setSensorId(c.getSensorId());
                    ca.setType(ConditionTypeAvro.valueOf(c.getType().name()));
                    ca.setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));

                    Object v = switch (c.getValueCase()) {
                        case INT_VALUE -> Integer.valueOf(c.getIntValue());
                        case BOOL_VALUE -> Boolean.valueOf(c.getBoolValue());
                        case VALUE_NOT_SET -> null;
                    };
                    ca.setValue(v);
                    conditions.add(ca);
                }

                ArrayList<DeviceActionAvro> actions = new ArrayList<>();
                for (var a : p.getScenarioAdded().getActionsList()) {
                    DeviceActionAvro aa = new DeviceActionAvro();
                    aa.setSensorId(a.getSensorId());
                    aa.setType(ActionTypeAvro.valueOf(a.getType().name()));
                    aa.setValue(a.hasValue() ? Integer.valueOf(a.getValue()) : null);
                    actions.add(aa);
                }

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

        if (payload == null) {
            return null;
        }

        HubEventAvro avro = new HubEventAvro();
        avro.setHubId(p.getHubId());
        avro.setTimestamp(toEpochMillis(p.getTimestamp()));
        avro.setPayload(payload);
        return avro;
    }
}
