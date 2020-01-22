package org.sympatico.kafka.processor.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class SimpleKafkaProducer {

    private static final Logger LOG  = LoggerFactory.getLogger(SimpleKafkaProducer.class);

    private KafkaProducer<Long, byte[]> producer;

    public SimpleKafkaProducer(Properties config) {
        final Properties kafkaConfig = new Properties();
        for (Map.Entry<Object, Object> entry: config.entrySet()) {
            String kafkaProperty = String.valueOf(entry.getKey());
            LOG.info("Checking property: " + kafkaProperty);
            if (Pattern.matches("^producer.[-_a-z.]+", kafkaProperty)) {
                String key = kafkaProperty.replace("producer.", "");
                LOG.info("Found matching config: " + key);
                kafkaConfig.put(key, config.getProperty(kafkaProperty, ""));
            }
        }
        createProducer(kafkaConfig);
    }

    public KafkaProducer<Long, byte[]> getProducer() {
        return producer;
    }

    private void createProducer(Properties kafkaConfig) {
        producer = new KafkaProducer<>(kafkaConfig);
    }
}