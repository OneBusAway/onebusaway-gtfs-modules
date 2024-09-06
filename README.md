# onebusaway-gtfs-modules 

[![CI](https://github.com/OneBusAway/onebusaway-gtfs-modules/actions/workflows/ci.yml/badge.svg)](https://github.com/OneBusAway/onebusaway-gtfs-modules/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.onebusaway/onebusaway-gtfs-modules.svg)](https://mvnrepository.com/artifact/org.onebusaway/onebusaway-gtfs-modules)

A Java library for reading and writing [GTFS](https://developers.google.com/transit/gtfs) feeds, including database support.

See more documentation in the [`docs folder`](./docs).

## Maven usage

In your `pom.xml`, include:

```

<dependency>
  <groupId>org.onebusaway</groupId>
  <artifactId>onebusaway-gtfs</artifactId>
  <version>${VERSION}</version>
</dependency>
```

...where `<version>` contains the latest version number.

## Update on camsys-apps.com repo

In August 2024 @leonardehrenfried took over maintainership and subsequently all artifacts are
now again published to Maven Central. Adding camsys-apps.com to your Maven repo configuration is no
longer necessary when you use version 1.4.18 or newer.