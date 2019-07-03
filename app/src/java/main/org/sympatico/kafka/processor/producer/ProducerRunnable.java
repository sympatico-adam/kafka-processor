package org.sympatico.kafka.processor.producer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProducerRunnable implements Runnable {

    private static final Logger LOG  = LoggerFactory.getLogger(SimpleKafkaProducer.class);

    private static final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final SimpleKafkaProducer kafkaProducer;
    private final ConcurrentLinkedQueue<Pair<String, byte[]>> producerQueue;
    private final int batchSize;

    public ProducerRunnable(Properties config, ConcurrentLinkedQueue<Pair<String, byte[]>> queue) {
        kafkaProducer = new SimpleKafkaProducer(config);
        producerQueue = queue;
        batchSize = Integer.parseInt(config.getProperty("producer.batch.size", "16384"));
        LOG.info("Producer thread has started.");
    }

    @Override
    public void run() {
        batch();
        KafkaProducer<Long, byte[]> producer = kafkaProducer.getProducer();
        try {
            while (!shutdown.get()) {
                Pair<String, byte[]> msg = producerQueue.poll();
                try {
                    producer.send(new ProducerRecord<>(msg.getKey(), msg.getValue()), (metadata, e) -> {
                        try {LOG.trace("Produced message\ntopic: " +  metadata.topic() +
                            "\ntimestamp: " + metadata.timestamp() + "\nvalue: " + metadata.toString() + ") " +
                            "partition: " + metadata.partition() + ", " + "offset=" + metadata.offset() + ")");
                        } catch (NullPointerException nullException) {
                            LOG.error("Producer NPE: " + nullException);
                            e.printStackTrace();}});
                } catch (NullPointerException e) {
                    // Quietly handle NPEs
                    Thread.sleep(1000L);
                }
            }
        } catch (InterruptedException e) {
            producer.abortTransaction();
        } finally {
            producer.flush();
            shutdown();
        }
    }

    public void batch() {
        if (batchSize > 0) return;
        KafkaProducer<Long, byte[]> producer = kafkaProducer.getProducer();
        while (!shutdown.get()) {
            try {
                long time = System.currentTimeMillis();
                final CountDownLatch countDownLatch = new CountDownLatch(batchSize);
                Pair<String, byte[]> msg = producerQueue.poll();;
                for (long index = time; index < time + batchSize; index++) {
                    try {
                        producer.send(new ProducerRecord<>(msg.getKey(), index, msg.getValue()), (metadata, e) -> {
                            LOG.trace("Produced message\ntopic: " + metadata.topic() +
                                    "\ntimestamp: " + metadata.timestamp() + "\nvalue: " + metadata.toString() + ") " +
                                    "partition: " + metadata.partition() + ", " + "offset=" + metadata.offset() + ")");
                            LOG.trace("Producer batch countdown wait list: " + countDownLatch.getCount());
                            countDownLatch.countDown();
                        });
                    } catch (NullPointerException NPException) {
                        Thread.sleep(1000L);
                        NPException.printStackTrace();
                    }
                }
                countDownLatch.await(10000, TimeUnit.MILLISECONDS);
                producer.flush();
            } catch (InterruptedException e) {
                LOG.error("Producer thread shutting down: " + e);
            } finally {
                producer.flush();
                shutdown();
            }
        }
    }

    public void shutdown() {
        shutdown.set(true);
    }
}
