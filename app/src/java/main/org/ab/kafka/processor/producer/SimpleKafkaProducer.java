package org.ab.kafka.processor.producer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleKafkaProducer {

    private static final Logger LOG  = LoggerFactory.getLogger(SimpleKafkaProducer.class);


    private final KafkaProducer<Long, byte[]> producer;

    public SimpleKafkaProducer(Properties config) {
        final Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("producer.bootstrap.servers"));
        producerConfig.put(ProducerConfig.ACKS_CONFIG, config.getProperty("producer.acks", "1"));
        producerConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, config.getProperty("producer.enable.idempotence", "false"));
        producerConfig.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, config.getProperty("producer.max.in.flight.requests.per.connection", "5"));
        producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, config.getProperty("producer.linger.ms", "0"));
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, config.getProperty("producer.retries", "0"));
        producerConfig.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.getProperty("producer.compression.type", "none"));
        producerConfig.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.parseInt(config.getProperty("producer.batch.size", "16384")));
        producerConfig.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.getProperty("producer.buffer.memory", "33554432"));
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getProperty("producer.key.serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getProperty("producer.value.serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        producer = new KafkaProducer<>(producerConfig);
    }

    public KafkaProducer<Long, byte[]> getProducer() {
        return producer;
    }
}