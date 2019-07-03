package org.guild.dataprocessor;

import org.guild.dataprocessor.consumer.SimpleKafkaConsumer;

import java.io.*;
import java.util.Properties;

public class FilmProcessor {

    private static SimpleKafkaConsumer consumers;

    public static void main(String[] args) {
        Properties config = new Properties();
        if (args != null && args.length == 1) {
            try {
                config.load(new FileInputStream(new File(args[1])));
            } catch (IOException e) {
                System.out.println("Unable to load config file: " + args[1]);
            }
        }
        consumers = new SimpleKafkaConsumer(config);
        consumers.execute();
    }


    private static final void stop() {
        consumers.shutdown();
    }
}
