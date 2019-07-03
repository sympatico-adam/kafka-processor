package org.sympatico.kafka.processor.producer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SimpleKafkaProducerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaProducerTest.class);
    private static final String TOPIC = "test";
    private static final Properties config = new Properties();

    private final Executor executor = Executors.newFixedThreadPool(4);
    private final ConcurrentLinkedQueue<Pair<String, byte[]>> queue = new ConcurrentLinkedQueue<>();
    private ProducerRunnable[] producers;

    @Before
    public void setUp() throws Exception {
        File file = new File("app/src/java/test/resources/processor.test.properties");
        LOG.info("Config: " + file.getAbsolutePath());
        config.load(new FileInputStream(file));
        int poolSize = Integer.parseInt(config.getProperty("producer.thread.pool.size"));
        producers = new ProducerRunnable[poolSize];
        for (int i = 0; i < poolSize; i++) {
            producers[i] = new ProducerRunnable(config, queue);
            executor.execute(producers[i]);
        }
    }

    @Test
    public void producerTest() throws Exception {
        FileInputStream fstream = new FileInputStream(new File("app/src/java/test/resources/sample.csv"));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
            String line;
            br.readLine(); // header
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] splitLine = line.split(",");
                queue.add(new ImmutablePair<>(TOPIC,
                        new JSONObject().put("id", splitLine[1]).put("rating", splitLine[2]).toString().getBytes(StandardCharsets.UTF_8)));
            }
            while (!queue.isEmpty()) {
                Thread.sleep(1000L);
            }
        } finally {
            for (ProducerRunnable producer: producers) {
                producer.shutdown();
            }
        }
    }
}