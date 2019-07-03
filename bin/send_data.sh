#!/bin/bash
if [ $# = 0 ]; then
    echo "Please provide a data file path..."
else
    cat $1 | docker exec -t kafka-dev /usr/local/kafka/kafka_console_