# Runnable Kafka Consumer and Producer 

## Overview
Kafka consumer and producer objects that provides direct handling of the Kafka consumer/producer. This also
 includes a thread handler as well as a batch producer method.
 
## Usage
Create a Kafka properties file that contains prefixed properties for either consumer, producer, or both within the
 same file. Prefix the property name according to the intended target (eg. _producer.bootstrap.servers_ or _consumer
 .bootstrap.servers_) 
 
Use the following methods to instantiate new consumers/producers.

#### KafkaConsumer Direct API
```java
Properties config = new Properties();
config.load(new FileInputStream(new File("server.properties")));
KafkaConsumer consumer = new SimpleKafkaConsumer(config).getConsumer(); 
```

#### KafkaProducer Direct API
```java
Properties config = new Properties();
config.load(new FileInputStream(new File("server.properties")));
KafkaProducer producer = new SimpleKafkaProducer(config).getProducer(); 
```

#### ConsumerRunnable
```java
Properties config = new Properties();
config.load(new FileInputStream(new File("server.properties")));
int poolSize = 4;
Executor executor = Executors.newFixedThreadPool(poolSize);
ConsumerRunnable consumers = new ConsumerRunnable[poolSize];
for (int i = 0; i < poolSize; i++) {
    consumers[i] = new ConsumerRunnable(config, queue);
    executor.execute(consumers[i]);
}
```

#### ProducerRunnable
```java
Properties config = new Properties();
config.load(new FileInputStream(new File("server.properties")));
int poolSize = 4;
Executor executor = Executors.newFixedThreadPool(poolSize);
ProducerRunnable producers = new ProducerRunnable[poolSize];
for (int i = 0; i < poolSize; i++) {
    producers[i] = new ProducerRunnable(config, queue);
    executor.execute(producers[i]);
}
```

### Batch Producer
In order to enable the batch producer, provide a non-negative integer for _*producer.batch.size*_ in the properties
 file.
 
## Dev
A Kafka and Zookeeper docker config is provided for dev and test. Note that the unit tests depend on there being a
 running Kafka cluster.

#### docker-compose 
Use:
```bash
docker-compose up --build
```

A topic named _test_ will be automatically generated in the new cluster. 

New topics can be created with the following:
```bash
docker exec kafka-dev create-topic [topic_name]
``` 

Data can be sent to the desired topic with the following:
```bash
./bin/send_data.sh [filepath] [topic]
```