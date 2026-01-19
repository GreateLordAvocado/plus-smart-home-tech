package ru.yandex.practicum.kafka.telemetry.collector.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldAcceptLightSensorEvent() throws Exception {
        String json = """
            {
              "id": "sensor.light.3",
              "hubId": "hub-2",
              "timestamp": "2024-08-06T15:11:24.157Z",
              "linkQuality": 75,
              "luminosity": 59,
              "type": "LIGHT_SENSOR_EVENT"
            }
            """;

        mvc.perform(post("/events/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldAcceptDeviceAddedHubEvent() throws Exception {
        String json = """
            {
              "hubId": "hub-2",
              "timestamp": "2024-08-06T15:11:24.157Z",
              "type": "DEVICE_ADDED",
              "id": "sensor.motion.1",
              "deviceType": "MOTION_SENSOR"
            }
            """;

        mvc.perform(post("/events/hubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted());
    }
}
