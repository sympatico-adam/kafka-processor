package org.guild.dataprocessor.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerHandlerRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerHandlerRunnable.class);

    private ConsumerRecord consumerRecord;

    public ConsumerHandlerRunnable(ConsumerRecord consumerRecord) {
        this.consumerRecord = consumerRecord;

    }

    @Override
    public void run() {
        LOG.info("Consumer Record:(" + consumerRecord.key() + ", " +
                consumerRecord.value() + ", " + consumerRecord.partition() + ", " +
                consumerRecord.partition() + ")\n," + consumerRecord.offset());
        try {
            MetadataProcessor.process(consumerRecord.value().toString());
        } catch (Exception e) {

        }
    }
}