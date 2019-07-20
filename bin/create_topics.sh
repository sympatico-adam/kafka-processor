#!/bin/bash
echo "Creating kafka topic metadata..."
docker exec kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --create --topic metadata \
   --partitions 16  --replication-factor 1 --config compression.type=lz4 --config min.insync.replicas=1
echo "Done!"

echo "Creating kafka topic ratings..."
docker exec kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --create --topic ratings \
   --partitions 16  --replication-factor 1 --config compression.type=lz4 --config min.insync.replicas=1
echo "Done!"
