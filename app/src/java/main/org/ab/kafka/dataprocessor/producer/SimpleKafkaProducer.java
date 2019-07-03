package org.guild.dataprocessor.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class SimpleKafkaProducer implements Runnable {
    private final KafkaProducer<Long, String> producer;
    private final String topic;

    public SimpleKafkaProducer(Properties config) {
        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", config.getProperty("producer.bootstrap.servers"));
        producerConfig.put("acks", "all");
        producerConfig.put("retries", 0);
        producerConfig.put("batch.size", 16384);
        producerConfig.put("linger.ms", 1);
        producerConfig.put("buffer.memory", 33554432);
        producerConfig.put("key.serializer", config.getProperty("key.serializer"));
        producerConfig.put("value.serializer", config.getProperty("value.serializer"));

        this.producer = new KafkaProducer<Long, String>(producerConfig);
        this.topic = config.getProperty("producer.topic");
    }

    @Override
    public void run() {
        System.out.println("Produces 5 messages ");
        for (int i = 0; i < 5; i++) {
            String msg = "Message  " + i;
            producer.send(new ProducerRecord<Long, String>(topic, msg), new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                    System.out.println("Sent: " + msg + ", Offset:  " + metadata.offset());
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        producer.close();
    }
}