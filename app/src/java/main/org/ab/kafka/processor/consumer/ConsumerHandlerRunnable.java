package org.ab.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.errors.WakeupException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumerHandlerRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerHandlerRunnable.class);
    private static final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final ConcurrentLinkedQueue<JSONObject> consumerQueue;
    private final SimpleKafkaConsumer kafkaConsumer;

    public ConsumerHandlerRunnable(Properties config, String topic, ConcurrentLinkedQueue<JSONObject> queue) {
        LOG.info("Creating new Kafka consumer for topic " + topic + "...");
        consumerQueue = queue;
        kafkaConsumer = new SimpleKafkaConsumer(config, topic);
    }

    @Override
    public void run() {
        KafkaConsumer<Long, byte[]> consumer = kafkaConsumer.getConsumer();
        try (consumer) {
            LOG.info("Consumer begin consuming from topic [" + consumer.listTopics() + "]");
            while (!shutdown.get()) {
                ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                for (final ConsumerRecord<Long, byte[]> record : records) {
                    try {
                        byte[] bytes = record.value();
                        consumerQueue.add(new JSONObject(new String(bytes, StandardCharsets.UTF_8)));
                    } catch (JSONException e) {
                        LOG.error("Could not parse json object: " + record.key() + ", " +
                                new String(record.value(), StandardCharsets.UTF_8) + ", " + record.partition() + ", " +
                                record.partition() + ")\n," + record.offset() + "\n\n" + e);
                        e.printStackTrace();
                    }
                }
                consumer.commitSync();
            }
        } catch (
                WakeupException e) {
            // Ignore exception if closing
            if (!shutdown.get()) throw e;
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        shutdown.set(true);
        kafkaConsumer.getConsumer().wakeup();
    }
}