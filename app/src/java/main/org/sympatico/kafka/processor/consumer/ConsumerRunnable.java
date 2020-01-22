package org.sympatico.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerRunnable.class);
    private static final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final ConcurrentLinkedQueue<JSONObject> consumerQueue;
    private final SimpleKafkaConsumer kafkaConsumer;

    public ConsumerRunnable(Properties config, ConcurrentLinkedQueue<JSONObject> queue) {
        consumerQueue = queue;
        kafkaConsumer = new SimpleKafkaConsumer(config);
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
                        consumerQueue.add(new JSONObject(new String(bytes, StandardCharsets.UTF_8)));
                    }
                } catch (NullPointerException NPException) {
                    Thread.sleep(1000L);
                }
                consumer.commitSync();
            }
        } catch (
            WakeupException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown.set(true);
        kafkaConsumer.getConsumer().wakeup();
        kafkaConsumer.getConsumer().close();
    }
}