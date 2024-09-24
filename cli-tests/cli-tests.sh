#!/bin/bash

mvn clean package -DskipTests -Dmaven.source.skip=true -Dmaven.javadoc.skip=true

# transformer-cli

cp onebusaway-gtfs-transformer-cli/target/onebusaway-gtfs-transformer-cli.jar ./transformer-cli.jar
wget https://github.com/google/transit/blob/master/gtfs/spec/en/examples/sample-feed-1.zip?raw=true -O gtfs.zip

java -jar transformer-cli.jar --help

java -jar transformer-cli.jar --transform="{'op':'remove','match':{'file':'stops.txt','stop_id':'BEATTY_AIRPORT'}}" gtfs.zip gtfs.transformed.zip
