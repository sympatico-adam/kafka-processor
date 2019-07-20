package org.ab.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class SimpleKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaConsumer.class);

    private KafkaConsumer<Long, byte[]> consumer;

    public SimpleKafkaConsumer(Properties config, String topic) {
        try {
            final Properties kafkaConfig = new Properties();
            final int startingOffset = Integer.valueOf(config.getProperty("consumer.starting.offset", "-2"));
            kafkaConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("consumer.bootstrap.servers"));
            kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, config.getProperty("consumer.group.id"));
            kafkaConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getProperty("consumer.key.deserializer"));
            kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getProperty("consumer.value.deserializer"));
            kafkaConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getProperty("consumer.auto.offset.reset", "latest"));
            kafkaConfig.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, config.getProperty("consumer.isolation.level", "read_uncommitted"));
            kafkaConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getProperty("consumer.auto.commit", "true"));
            kafkaConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getProperty("consumer.max.poll.records", "50"));
            consumer = new KafkaConsumer<>(kafkaConfig);
            // Subscribe to the topic.
            consumer.subscribe(Collections.singleton(topic), new ConsumerRebalanceListener() {
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    System.out.println(Arrays.toString(partitions.toArray()) + "topic-partitions are revoked from this consumer\n");
                }
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    System.out.println(Arrays.toString(partitions.toArray()) + " topic-partitions are assigned to this consumer\n");
                    for (TopicPartition topicPartition : partitions) {
                        System.out.println("Current offset is " + consumer.position(topicPartition) + " committed offset is ->" + consumer.committed(topicPartition));
                        if (startingOffset == -2) {
                            System.out.println("Leaving it alone");
                        } else if (startingOffset == 0) {
                            System.out.println("Setting offset to beginning");
                            consumer.seekToBeginning(Collections.singleton(topicPartition));
                        } else if (startingOffset == -1) {
                            System.out.println("Setting it to the end ");
                            consumer.seekToEnd(Collections.singleton(topicPartition));
                        } else {
                            System.out.println("Resetting offset to " + startingOffset);
                            consumer.seek(topicPartition, startingOffset);
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("Unable to read properties file!");
            throw e;
        }
    }

    public KafkaConsumer<Long, byte[]> getConsumer() {
        return consumer;
    }


}