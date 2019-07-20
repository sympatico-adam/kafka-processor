package org.ab.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleKafkaConsumerTest {

    private static final Properties config = new Properties();
    private static final ConcurrentLinkedQueue<JSONObject> queue = new ConcurrentLinkedQueue<>();
    private KafkaConsumer<Long, byte[]> consumer;

    @Before
    public void setUp() throws Exception {
        File file = new File("conf/processor.test.properties");
        System.out.println("Config: " + file.getAbsolutePath());
        config.load(new FileInputStream(file));
        consumer = new SimpleKafkaConsumer(config, "ratings").getConsumer();
    }

    public void execute() throws JSONException {
        int i = 0;
        while (i < 100) {
            ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
            for (final ConsumerRecord record : records) {
                if (record.value() instanceof byte[]) {
                    JSONObject json = new JSONObject(new String((byte[]) record.value(), StandardCharsets.UTF_8));
                    System.out.println(
                            "\n**********************\n"
                                    + "Message Retrieved:\n"
                                    + json.toString()
                                    + "\n**********************\n");
                    i++;
                } else
                    System.out.println("Consumer message not a byte[]!!");
            }
        }
    }
}