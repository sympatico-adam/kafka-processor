#!/bin/bash -xe

## run something other than zookeeper and kafka
if [ "$1" != "run" ]; then
  exec "$@"
fi

## run zookeeper
if [ "$2" = "zookeeper" ]; then
  /opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties
  exit $?
fi

if [ "$2" != "kafka" ]; then
  sed -e 's/localhost\:2181/zk-dev\:2181/'
  echo "unknown target to run: $2"
  exit 1
fi

## run kafka

prop_file="/opt/kafka/config/server.properties"

if [ ! -z "$BROKER_ID" ]; then
  echo "broker id: $BROKER_ID"
  sed -r -i "s/^(broker.id)=(.*)/\1=$BROKER_ID/g" $prop_file
fi
PLAINTEXT_PORT=9092
# Set the zookeeper hostname
sed -r -i 's/zookeeper.connect=localhost\:2181/zookeeper.connect=zk-dev\:2181/' $prop_file

sed -r -i "s/#?(advertised.listeners)=(.*)/\1=PLAINTEXT:\/\/kafka-dev:$PLAINTEXT_PORT/g" $prop_file
sed -r -i "s/(listeners)=(.*)/\1=PLAINTEXT:\/\/kafka-dev:$PLAINTEXT_PORT/g" $prop_file
echo -e "\nsasl.enabled.mechanisms=PLAIN" >> $prop_file

/opt/kafka/bin/create_topics.sh 2>&1 &

KAFKA_HEAP_OPTS="-Xmx1G -Xms1G -Djava.security.auth.login.config=/etc/kafka/jaas.conf" \
  /opt/kafka/bin/kafka-server-start.sh $prop_file

