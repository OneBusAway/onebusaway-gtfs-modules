# onebusaway-gtfs-modules

A Java library for reading and writing [GTFS](https://developers.google.com/transit/gtfs) feeds, including database support.

See more documentation [on the wiki](https://github.com/OneBusAway/onebusaway-gtfs-modules/wiki).

## Maven usage

In your `pom.xml`, include:

~~~
<repositories>
	<repository>
		<id>public.onebusaway.org</id>
		<url>http://nexus.onebusaway.org/content/groups/public/</url>
	</repository>
</repositories>
~~~

... and inside `<dependencies>`:

~~~
<dependency>
	<groupId>org.onebusaway</groupId>
	<artifactId>onebusaway-gtfs</artifactId>
	<version>1.3.9</version>
</dependency>
~~~

...where `<version>` contains the latest version number.
