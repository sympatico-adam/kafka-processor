package org.ab.kafka.processor.producer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SimpleKafkaProducerTest {

    private static Properties config = new Properties();
    private final Executor executor = Executors.newFixedThreadPool(4);
    private ProducerHandlerRunnable[] producers;
    private final ConcurrentLinkedQueue<Pair<String, JSONObject>> queue = new ConcurrentLinkedQueue<>();

    @Before
    public void setUp() throws Exception {
        File file = new File("conf/processor.test.properties");
        System.out.println("Config: " + file.getAbsolutePath());
        config.load(new FileInputStream(file));
        int poolSize = Integer.valueOf(config.getProperty("producer.thread.pool.size"));
        producers = new ProducerHandlerRunnable[poolSize];
        for (int i = 0; i < poolSize; i++) {
            producers[i] = new ProducerHandlerRunnable(config, false, queue);
            executor.execute(producers[i]);
        }
    }

    public void producerTest() throws Exception {
        File file = new File("/home/underride/IdeaProjects/csv-transform-parser/bin/data/ratings.csv");
        System.out.println("Reading ratings file: " + file.getAbsolutePath());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // header
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] splitLine = line.split(",");
                queue.add(new ImmutablePair<>("ratings",
                        new JSONObject().put("id", splitLine[1]).put("rating", splitLine[2])));
            }
        } finally {
            for (ProducerHandlerRunnable producer: producers) {
                producer.shutdown();
            }
        }
    }
}