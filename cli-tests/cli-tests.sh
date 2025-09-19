#!/bin/bash +ex

mvn clean package --no-transfer-progress -DskipTests -Dmaven.source.skip=true -Dmaven.javadoc.skip=true

EXAMPLE_1="example.gtfs.zip"
EXAMPLE_2="deathvalley.gtfs.zip"

# transformer-cli

TRANSFORMER_JAR="transformer-cli.jar"

cp onebusaway-gtfs-transformer-cli/target/onebusaway-gtfs-transformer-cli.jar ./${TRANSFORMER_JAR}
wget https://github.com/google/transit/blob/master/gtfs/spec/en/examples/sample-feed-1.zip?raw=true -O ${EXAMPLE_1}

java -jar ${TRANSFORMER_JAR} --help

java -jar ${TRANSFORMER_JAR} --transform="{'op':'remove','match':{'file':'stops.txt','stop_id':'BEATTY_AIRPORT'}}" ${EXAMPLE_1} gtfs.transformed.zip

# merge-cli

MERGE_JAR="merge-cli.jar"

cp onebusaway-gtfs-merge-cli/target/onebusaway-gtfs-merge-cli-*.jar ./${MERGE_JAR}
wget "http://data.trilliumtransit.com/gtfs/deathvalley-demo-ca-us/deathvalley-demo-ca-us.zip" -O ${EXAMPLE_2}

java -jar ${MERGE_JAR} --help

java -jar ${MERGE_JAR} ${EXAMPLE_1} ${EXAMPLE_2} gtfs.merged.zip
