package org.sympatico.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SimpleKafkaConsumerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaConsumer.class);
    private static final Properties config = new Properties();
    private static ConsumerRunnable[] consumers;
    private static final Executor executor = Executors.newFixedThreadPool(4);

    @Before
    public void setup() throws IOException {
        File file = new File("app/src/java/test/resources/processor.test.properties");
        LOG.info("Config: " + file.getAbsolutePath());
        config.load(new FileInputStream(file));

    }

    @Test
    public void execute() {
        KafkaConsumer<Long, byte[]> consumer = new SimpleKafkaConsumer(config, config.getProperty("consumer.topic")).getConsumer();
        for (int i = 0; i < 100005; i++) {
            ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(1000L));
            for (final ConsumerRecord<Long, byte[]> record : records) {
                byte[] bytes = record.value();
                LOG.debug("\n**********************\n"
                        + "Message Retrieved:\n"
                        + new String(bytes)
                        + "\n**********************\n");
                consumer.commitAsync();
            }
        }

    }
    @Test
    public void executePool() throws Exception {
        int total = 100005;
        int count = 0;
        int poolSize = Integer.parseInt(config.getProperty("producer.thread.pool.size"));
        ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();
        consumers = new ConsumerRunnable[poolSize];
        for (int i = 0; i < poolSize; i++) {
            consumers[i] = new ConsumerRunnable(config, config.getProperty("consumer.topic"), queue);
            executor.execute(consumers[i]);
        }
        Thread.sleep(5000L);
        for (int i = 0; i <= 100005; i++) {
            System.out.println("Messages processed: " + count);
            byte[] record = queue.poll();
            if (record != null) {
                count++;
                LOG.debug("\n**********************\n"
                        + "Message Retrieved:\n"
                        + new String(record, StandardCharsets.UTF_8)
                        + "\n**********************\n");
            } else {
                Thread.sleep(1000L);
            }
        }
        Assert.assertEquals(total, count);
    }
}