#!/bin/bash
if [[ $# = 0 ]]; then
    echo -e "Please provide a data file path and topic...\n\t./send_data.sh [filepath] [topic]"
else
    filepath=${1}
    topic=${2}
    echo -e "File name: $filepath\nTopic: $topic"
    docker cp ${filepath} kafka-dev:/tmp/test.data
    docker exec -t kafka-dev bash -c "cat /tmp/test.data | /usr/local/kafka/bin/kafka-console-producer.sh --broker-list kafka-dev:9092 --topic ${topic}"
fi
