#!/bin/bash +x

mvn clean package --no-transfer-progress -DskipTests -Dmaven.source.skip=true -Dmaven.javadoc.skip=true

# transformer-cli

TRANSFORMER_JAR="transformer-cli.jar"

cp onebusaway-gtfs-transformer-cli/target/onebusaway-gtfs-transformer-cli.jar ./${TRANSFORMER_JAR}
wget https://github.com/google/transit/blob/master/gtfs/spec/en/examples/sample-feed-1.zip?raw=true -O gtfs.zip

java -jar ${TRANSFORMER_JAR} --help

java -jar ${TRANSFORMER_JAR} --transform="{'op':'remove','match':{'file':'stops.txt','stop_id':'BEATTY_AIRPORT'}}" gtfs.zip gtfs.transformed.zip
