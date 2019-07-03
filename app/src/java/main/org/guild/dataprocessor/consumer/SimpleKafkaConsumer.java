package org.guild.dataprocessor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.guild.dataprocessor.processor.ConsumerHandlerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaConsumer.class);

    private final KafkaConsumer<Long, String> consumer;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private ExecutorService executor;

    public SimpleKafkaConsumer(Properties config) {
        try {
            final Properties kafkaConfig = new Properties();
            final int poolSize = Integer.valueOf(config.getProperty("consumer.thread.pool.size"));
            kafkaConfig.put("bootstrap.servers", config.getProperty("consumer.bootstrap.servers"));
            kafkaConfig.put("group.id", config.getProperty("consumer.group.id"));
            kafkaConfig.put("key.deserializer", config.getProperty("consumer.key.deserializer"));
            kafkaConfig.put("value.deserializer", config.getProperty("consumer.value.deserializer"));
            consumer = createConsumer(kafkaConfig, config.getProperty("consumer.topics.list"));
            executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
        } catch (Exception e) {
            LOG.error("Unable to read properties file!");
            throw e;
        }
    }

    private KafkaConsumer<Long, String> createConsumer(Properties kafkaConfig, String topic) {
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(kafkaConfig);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    public void execute() {
        try {
            while (true) {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(100));
                for (final ConsumerRecord record: records) {
                    executor.submit(new ConsumerHandlerRunnable(record));
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }
}