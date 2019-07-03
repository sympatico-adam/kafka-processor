#!/bin/bash
echo "Creating kafka topic prod-cmpy..."
docker exec kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --create --topic prod-cmpy --partitions 2  --replication-factor 1
echo -e "Done!\n\nCreating kafka topic movie-genre"
docker exec kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --create --topic movie-genre --partitions 2 --replication-factor 1
echo "Done!"
