package org.sympatico.kafka.processor.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleKafkaConsumer.class);

    private KafkaConsumer<Long, byte[]> consumer;

    public SimpleKafkaConsumer(Properties config, String topic) {
        final Properties kafkaConfig = new Properties();
        try {
            LOG.info("Constructing new Kafka consumer: " + topic);
            for (Map.Entry<Object, Object> entry: config.entrySet()) {
                String kafkaProperty = String.valueOf(entry.getKey());
                if (Pattern.matches("^consumer.[-_a-z.]+", kafkaProperty)) {
                    String key = kafkaProperty.replace("consumer.", "");
                    LOG.info("Found matching config: " + key);
                    kafkaConfig.put(key, config.getProperty(kafkaProperty));
                }
            }
        } catch (NumberFormatException e) {
            LOG.error("Unable to read properties file!");
            throw e;
        }
        createConsumer(kafkaConfig);
    }

    public KafkaConsumer<Long, byte[]> getConsumer() {
        return consumer;
    }

    private void createConsumer(Properties kafkaConfig) {
        final int startingOffset = Integer.parseInt(kafkaConfig.getProperty("consumer.starting.offset", "-2"));
        consumer = new KafkaConsumer<>(kafkaConfig);
        // Subscribe to the topic.
        consumer.subscribe(Collections.singleton(kafkaConfig.getProperty("topic")),
                new ConsumerRebalanceListener() {
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                LOG.debug(Arrays.toString(partitions.toArray()) + "topic-partitions are revoked from this consumer\n");
            }

            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                LOG.debug(Arrays.toString(partitions.toArray()) + " topic-partitions are assigned to this consumer\n");
                for (TopicPartition topicPartition : partitions) {
                    LOG.debug("Current offset is " + consumer.position(topicPartition) + " committed offset is ->" + consumer.committed(topicPartition));
                    if (startingOffset == -2) {
                        LOG.debug("Leaving it alone");
                    } else if (startingOffset == 0) {
                        LOG.debug("Setting offset to beginning");
                        consumer.seekToBeginning(Collections.singleton(topicPartition));
                    } else if (startingOffset == -1) {
                        LOG.debug("Setting it to the end ");
                        consumer.seekToEnd(Collections.singleton(topicPartition));
                    } else {
                        LOG.debug("Resetting offset to " + startingOffset);
                        consumer.seek(topicPartition, startingOffset);
                    }
                }
            }
        });
    }
}