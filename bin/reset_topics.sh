#!/bin/bash
docker exec -ti kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --delete --topic ratings
docker exec -ti kafka-dev /usr/local/kafka/bin/kafka-topics.sh --zookeeper zk-dev:2181 --delete --topic metadata
docker exec -ti redis-dev redis-cli flushall
docker exec -ti redis-dev redis-cli memory purge
