#!/bin/bash
if [ $# = 0 ]; then
    echo -e "Please provide a data file path and topic...\n\t./send_data.sh /usr/local/projects/kafka-processor/bin/data/the-movies-dataset/movies_metadata.csv movie-genre"
else
    echo -e "File name: $1\nTopic: $2"
    eval $(echo "cat ${1} | docker exec -t kafka-dev /usr/local/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic ${2}")
fi;