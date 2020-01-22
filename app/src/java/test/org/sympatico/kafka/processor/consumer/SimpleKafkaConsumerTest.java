package org.sympatico.kafka.processor.consumer;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleKafkaConsumerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaConsumerTest.class);
    private static final Properties config = new Properties();
    private static final ConcurrentLinkedQueue<JSONObject> queue = new ConcurrentLinkedQueue<>();
    private KafkaConsumer<Long, byte[]> consumer;

    @Before
    public void setup() throws IOException {
        config.load(new FileInputStream(new File("app/src/java/test/resources/processor.test.properties")));
        consumer = new SimpleKafkaConsumer(config).getConsumer();
    }

    @Test
    public void execute() throws JSONException {
        int i = 0;
        while (i < 100) {
            ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            for (final ConsumerRecord<Long, byte[]> record : records) {
                JSONObject json = new JSONObject(new String(record.value()));
                LOG.info(
                        "\n**********************\n"
                                + "Message Retrieved:\n"
                                + json.toString()
                                + "\n**********************\n");
                i++;
            }
        }
    }
}