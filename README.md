# onebusaway-gtfs-modules [![CI](https://github.com/OneBusAway/onebusaway-gtfs-modules/actions/workflows/ci.yml/badge.svg)](https://github.com/OneBusAway/onebusaway-gtfs-modules/actions/workflows/ci.yml)

A Java library for reading and writing [GTFS](https://developers.google.com/transit/gtfs) feeds, including database support.

See more documentation [on the wiki](https://github.com/OneBusAway/onebusaway-gtfs-modules/wiki).

## Maven usage

In your `pom.xml`, include:

~~~
<repositories>
	<repository>
		<id>public.onebusaway.org</id>
		<url>https://repo.camsys-apps.com/releases/</url>
	</repository>
</repositories>
~~~

... and inside `<dependencies>`:

~~~
<dependency>
	<groupId>org.onebusaway</groupId>
	<artifactId>onebusaway-gtfs</artifactId>
	<version>1.3.88</version>
</dependency>
~~~

...where `<version>` contains the latest version number.
