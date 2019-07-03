#!/bin/bash
topic=${1}
docker exec -ti kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --delete --topic ${topic}

