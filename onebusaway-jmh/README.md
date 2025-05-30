# onebusaway-jmh
This module includes a few JMH benchmarks. Run

Note: Remember to turn off CPU boost.

> mvn clean package && java -jar target/jmh-benchmarks.jar GtfsBenchmark -rf json

Use a visualizer like [JMH visualizer](https://jmh.morethan.io/) to drill down on the numbers.