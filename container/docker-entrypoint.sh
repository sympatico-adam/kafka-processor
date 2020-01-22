#!/bin/bash -xe

prop_file="/usr/local/kafka/config/server.properties"

#if [[ ! -z "$BROKER_ID" ]]; then
#  echo "broker id: $BROKER_ID"
#  sed -r -i "s/^(broker.id)=(.*)/\1=$BROKER_ID/g" ${prop_file}
#fi
PLAINTEXT_PORT=9092
# Set the zookeeper hostname
#sed -r -i 's/zookeeper.connect=localhost\:2181/zookeeper.connect=localhost\:2181/' ${prop_file}

#sed -r -i "s/#?(advertised.listeners)=(.*)/\1=PLAINTEXT:\/\/localhost:$PLAINTEXT_PORT/g" ${prop_file}
#sed -r -i "s/(listeners)=(.*)/\1=PLAINTEXT:\/\/localhost:$PLAINTEXT_PORT/g" ${prop_file}
#echo -e "\nsasl.enabled.mechanisms=PLAIN" >> ${prop_file}

KAFKA_HEAP_OPTS="-Xmx1G -Xms1G"

/usr/local/kafka/bin/kafka-server-start.sh ${prop_file} >/var/log/kafka.log &

$@ 2>1&

tail -f /var/log/kafka.log