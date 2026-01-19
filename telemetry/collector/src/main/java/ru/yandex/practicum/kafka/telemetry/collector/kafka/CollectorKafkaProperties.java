package ru.yandex.practicum.kafka.telemetry.collector.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.kafka")
public class CollectorKafkaProperties {

    private String bootstrapServers;
    private Topics topics = new Topics();

    public String getBootstrapServers() {
        return bootstrapServers;
    }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Topics getTopics() {
        return topics;
    }
    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public static class Topics {
        private String sensors;
        private String hubs;

        public String getSensors() {
            return sensors;
        }
        public void setSensors(String sensors) {
            this.sensors = sensors;
        }

        public String getHubs() {
            return hubs;
        }
        public void setHubs(String hubs) {
            this.hubs = hubs;
        }
    }
}
