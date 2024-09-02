 ------
GTFS Hibernate Command-Line Utility
 ------
 ------
 ------

Introduction

  The `onebusaway-gtfs-hibernate-cli` command-line utility is a simple command-line tool for loading a
{{{https://developers.google.com/transit/gtfs}GTFS}} feed into a database.

Getting the Application

  You can download the application here:

#if( $currentVersion.endsWith("-SNAPSHOT"))
  #set( $repository = 'snapshots' )
#else
  #set( $repository = 'releases' )
#end

#set( $url = 'https://repo.camsys-apps.com/' + $repository + '/org/onebusaway/onebusaway-gtfs-hibernate-cli/' + ${currentVersion} + '/onebusaway-gtfs-hibernate-cli-' + ${currentVersion} + '.jar' )

  {{{${url}}onebusaway-gtfs-hibernate-cli-${currentVersion}.jar}}
  
Using the Application

  You'll need a Java 11 runtime installed to run the client.  To run the application:

```
java -classpath onebusaway-gtfs-hiberante-cli.jar:your-database-jdbc.jar \
 org.onebusaway.gtfs.GtfsDatabaseLoaderMain \
 --driverClass=... \
 --url=... \
 --username=... \
 --password=... \
 gtfs_path
```

  Note that the utility doesn't include any JDBC client jars for any databases by default.  You will need
to download an appropriate JDBC client for your database and include it on the classpath when running
the utilty.  You will also need to specify the appropriate JDBC driver class and url for your database
using the command-line arguments specified below.

* Arguments

  * `--driverClass=...` : JDBC driver class for your JDBC provider (eg. "org.hsqldb.jdbcDriver")
  
  * `--url=...` : JDBC connection url for your database (eg. "jdbc:hsqldb:mem:temp_db")
  
  * `--username=...` : JDBC connection username
  
  * `--password=...` : JDBC connection password
  
  []

     