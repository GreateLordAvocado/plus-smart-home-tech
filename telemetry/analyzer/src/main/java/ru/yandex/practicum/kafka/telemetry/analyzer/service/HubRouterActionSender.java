package ru.yandex.practicum.kafka.telemetry.analyzer.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class HubRouterActionSender {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterActionSender(@GrpcClient("hub-router")
                                 HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void sendActions(String hubId, long snapshotTimestampMs,
                            List<ScenarioEvaluationService.PlannedAction> actions) {

        Instant tsInstant = Instant.ofEpochMilli(snapshotTimestampMs);
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(tsInstant.getEpochSecond())
                .setNanos(tsInstant.getNano())
                .build();

        for (var a : actions) {
            DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                    .setSensorId(a.sensorId())
                    .setType(DeviceActionProto.ActionType.valueOf(a.type().name()))
                    .setValue(a.value() == null ? 0 : a.value())
                    .build();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(a.scenarioName())
                    .setAction(actionProto)
                    .setTimestamp(ts)
                    .build();

            Empty resp = hubRouterClient.handleDeviceAction(request);
            log.debug("HubRouter response: hubId={}, sensorId={}, scenario={}", hubId, a.sensorId(), a.scenarioName());
        }
    }
}
