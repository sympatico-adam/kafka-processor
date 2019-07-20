package org.ab.kafka.processor.producer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ProducerHandlerRunnable implements Runnable {

    private static final Logger LOG  = LoggerFactory.getLogger(SimpleKafkaProducer.class);

    private static final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final SimpleKafkaProducer kafkaProducer;
    private final ConcurrentLinkedQueue<Pair<String, JSONObject>> producerQueue;
    private final int batchSize;
    private final boolean batchMode;

    public ProducerHandlerRunnable(Properties config, Boolean batch, ConcurrentLinkedQueue<Pair<String, JSONObject>> queue) {
        kafkaProducer = new SimpleKafkaProducer(config);
        producerQueue = queue;
        batchMode = batch;
        batchSize = Integer.parseInt(config.getProperty("producer.batch.size", "16384"));
        LOG.info("Producer thread has started.");
    }

    @Override
    public void run() {
        if (batchMode) {
            runBatch();
        } else {
            KafkaProducer<Long, byte[]> producer = kafkaProducer.getProducer();
            try {
                while (!shutdown.get()) {
                    try {
                        Pair<String, JSONObject> msg = producerQueue.poll();
                        if (msg != null) {
                            final ProducerRecord<Long, byte[]> record =
                                    new ProducerRecord<>(msg.getKey(), msg.getValue().toString().getBytes(StandardCharsets.UTF_8));
                            producer.send(record, (metadata, e) -> {
                                if (metadata != null) {
                                    LOG.trace("sent record(topic=" + record.topic() + " key=" + record.key() +
                                            " value=" + new String(record.value(), StandardCharsets.UTF_8) + ") " +
                                            "meta(partition=" + metadata.partition() + ", offset=" + metadata.offset() +
                                            ")");
                                }
                                if (e != null) {
                                    LOG.error("Producer failed: " + e);
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            Thread.sleep(1000L);
                        }
                    } finally {
                        producer.flush();
                    }
                }
            } catch (Exception e) {
                LOG.error("Exception caught in producer thread: " + e);

            } finally {
                producer.flush();
                shutdown();
            }
        }
    }

    public void runBatch() {
        KafkaProducer<Long, byte[]> producer = kafkaProducer.getProducer();
        try {
            while (!shutdown.get()) {
                long time = System.currentTimeMillis();
                final CountDownLatch countDownLatch = new CountDownLatch(batchSize);
                try {
                    for (long index = time; index < time + batchSize; index++) {
                        Pair<String, JSONObject> msg = producerQueue.poll();

                        if (msg != null) {
                            final ProducerRecord<Long, byte[]> record =
                                    new ProducerRecord<>(msg.getKey(), index, msg.getValue().toString().getBytes(StandardCharsets.UTF_8));
                            producer.send(record, (metadata, e) -> {
                                long elapsedTime = System.currentTimeMillis() - time;
                                if (metadata != null) {
                                    LOG.trace("sent record(topic=" + record.topic() + " key=" + record.key() +
                                            " value=" + new String(record.value(), StandardCharsets.UTF_8) + ") " +
                                            "meta(partition=" + metadata.partition() + ", offset=" + metadata.offset() +
                                            ") time=" + elapsedTime + "\n");
                                }
                                if (e != null) {
                                    LOG.error("Producer failed: " + e);
                                    e.printStackTrace();
                                }
                                LOG.trace("Producer batch countdown wait list: " + countDownLatch.getCount());
                                countDownLatch.countDown();
                            });
                        } else {
                            Thread.sleep(1000L);
                        }
                    }
                    countDownLatch.await(10000, TimeUnit.MILLISECONDS);
                } finally {
                    producer.flush();
                }
            }
        } catch (Exception e) {
            LOG.error("Exception caught in producer thread: " + e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        shutdown.set(true);
    }
}
