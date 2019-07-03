package org.sympatico.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerRunnable.class);
    private static final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final ConcurrentLinkedQueue<byte[]> consumerQueue;
    private final SimpleKafkaConsumer kafkaConsumer;

    public ConsumerRunnable(Properties config, String topic, ConcurrentLinkedQueue<byte[]> queue) {
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
                try {
                    ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                    for (final ConsumerRecord<Long, byte[]> record : records) {
                        byte[] bytes = record.value();
                        consumerQueue.offer(bytes);
                    }
                } catch (NullPointerException NPException) {
                    LOG.info("Consumer poll returned null...\n" + NPException.getMessage());
                    Thread.sleep(1000L);
                }
                consumer.commitSync();
            }
        } catch (WakeupException | InterruptedException e) {
            LOG.info("Consumer shutting down...");
            shutdown();
        }
    }

    public void shutdown() {
        shutdown.set(true);
        kafkaConsumer.getConsumer().wakeup();
        kafkaConsumer.getConsumer().close();
    }
}