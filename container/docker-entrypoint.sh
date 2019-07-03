#!/bin/bash -xe

## run something other than zookeeper and kafka
if [ "$1" != "run" ]; then
  echo "Executing custom command - ${1}"
  exec "$@"
fi

## run zookeeper
if [ "$2" = "zookeeper" ] || [ $HOST_ROLE = "zookeeper" ]; then
  echo "Starting Zookeeper server..."
  /usr/local/kafka/bin/zookeeper-server-start.sh /usr/local/kafka/config/zookeeper.properties
  exit $?
fi

if [ "$2" != "kafka" ]; then
  #sed -e 's/localhost\:2181/zk-dev\:2181/'
  echo "No valid run types found - ${2}"
  exit 1
fi

echo "Starting Kafka server..."
prop_file="/usr/local/kafka/config/server.properties"

#/usr/local/bin/create_topics.sh 2>&1 &

KAFKA_HEAP_OPTS="-Xmx1G -Xms1G" /usr/local/kafka/bin/kafka-server-start.sh $prop_file

## run kafka


# if [ ! -z "$BROKER_ID" ]; then
#   echo "broker id: $BROKER_ID"
#   sed -r -i "s/^(broker.id)=(.*)/\1=$BROKER_ID/g" $prop_file
# fi

# # Set the zookeeper hostname
# sed -r -i 's/zookeeper.connect=localhost\:2181/zookeeper.connect=zk-dev\:2181/' $prop_file

# ipwithnetmask="$(ip -f inet addr show dev eth0 | awk '/inet / { print $2 }')"
# ipaddress="${ipwithnetmask%/*}"

# sed -r -i "s/^(advertised.listeners)=(.*)/\1=PLAINTEXT:\/\/$ipaddress:$PLAINTEXT_PORT,SSL:\/\/$ipaddress:$SSL_PORT,SASL_SSL:\/\/$ipaddress:$SASL_SSL_PORT/g" $prop_file
# sed -r -i "s/^(listeners)=(.*)/\1=PLAINTEXT:\/\/:$PLAINTEXT_PORT,SSL:\/\/:$SSL_PORT,SASL_SSL:\/\/:$SASL_SSL_PORT/g" $prop_file
# echo -e "\nsasl.enabled.mechanisms=PLAIN" >> $prop_file

