# GTFS Merge Command-Line Application

## Introduction

The `onebusaway-gtfs-merge-cli` command-line application is a simple command-line tool for merging
[GTFS](https://developers.google.com/transit/gtfs) feeds. 

## Getting the Application

You can download the application from Maven Central.

Go to https://repo1.maven.org/maven2/org/onebusaway/onebusaway-gtfs-merge-cli/, select the version
you want and get the URL for the largest jar file. An example would be
https://repo1.maven.org/maven2/org/onebusaway/onebusaway-gtfs-merge-cli/3.2.2/onebusaway-gtfs-merge-cli-3.2.2.jar

## Using the Application

You'll need a Java 17 runtime installed to run the client. 

To run the application:

```
java -jar onebusaway-gtfs-merge-cli.jar [--args] input_gtfs_path_a input_gtfs_path_b ... output_gtfs_path
```

**Note**: Merging large GTFS feeds is often processor and memory intensive.  You'll likely need to increase the
max amount of memory allocated to Java with an option like `-Xmx1G` (adjust the limit as needed).  I also recommend
adding the `-server` argument if you are running the Oracle or OpenJDK, as it can really increase performance. 

## Configuring the Application

The merge application supports a number of options and arguments for configuring the application's behavior.  The
general pattern is to specify options for each type of file in a GTFS feed using the `--file` option, specifying
specific options for each file type after the `--file` option.  Here's a quick example:

```
--file=routes.txt --duplicateDetection=identity --file=calendar.txt --logDroppedDuplicates ...
```

  The merge application supports merging the following files:

 - `agency.txt`
 - `stops.txt`
 - `routes.txt`
 - `trips.txt` and `stop_times.txt`
 - `calendar.txt` and `calendar_dates.txt` 
 - `shapes.txt`
 - `fare_attributes.txt`
 - `fare_rules.txt`
 - `frequencies.txt`
 - `transfers.txt`
   
You can specify merge options for each of these files using the `--file=gtfs_file.txt` option.  File types listed
together (eg. `trips.txt>> and `stop_times.txt`) are handled by the same merge strategy, so specifying options for
either will have the same effect.  For details on options you might specify, read on.

## Handling Duplicates

The main issue to considering when merging GTFS feeds is the handling of duplicate entries between the two feeds,
including how to identify duplicates and what to do with duplicates when they are found.

### Identifying Duplicates

We support a couple of methods for determining when entries from two different feeds are actually duplicates.  By default,
the merge tool will attempt to automatically determine the best merge strategy to use.  You can also control the specific
strategy used on a per-file basis using the `--duplicateDetection` argument.  You can specify any of the following
strategies for duplicate detection.
  
 - `--duplicateDetection=identity` - If two entries have the same id (eg. stop id, route id, trip id), then they are
    considered the same.  This is the more strict matching policy.
  
 - `--duplicateDetection=fuzzy` - If two entries have common elements (eg. stop name or location, route short name,
    trip stop sequence), then they are considered the same.  This is the more lenient matching policy, and is highly
    dependent on the type of GTFS entry being matched.
    
 - `--duplicateDetection=none` - Entries between two feeds are never considered to be duplicates, even if they have
    the same id or similar properties.

### Logging Duplicates

Sometimes your feed might have unexpected duplicates.  You can tell the merge tool to log duplicates it finds or even
immediately exit with the following arguments:

 - `--logDroppedDuplicates` - log a message when a duplicate is found
  
 - `--errorOnDroppedDuplicates` - throw an exception when a duplicate is found, stopping the program
     
## Examples

### Handling a Service Change

Agencies often schedule major changes to their system around a particular date, with one GTFS feed for before the
service change and a different GTFS feed for after.  We'd like to be able to merge these disjoint feeds into one
feed with continuous coverage.

In our example, an agency produces two feeds where the entries in `agency.txt` and `stops.txt` are exactly
the same, so the default policy of identifying and dropping duplicates will work fine there.  The `routes.txt` file
is a bit trickier, since the route ids are different between the two feeds but the entries are largely the same.  We
will use fuzzy duplicate detection to match the routes between the two feeds.

The next issue is the `calendar.txt` file.  The agency uses the same `service_id` values in both feeds
(eg. `WEEK`, `SAT`, `SUN`) with different start and end dates in the two feeds.  If the default policy of
dropping duplicate entries was used, we'd lose the dates in one of the service periods.  Instead, we rename duplicates
such that the service ids from the second feed will be renamed to `b-WEEK`, `b-SAT`, etc. and all
`trips.txt` entries in the second feed will be updated appropriately.  The result is that trips from the first
and second feed will both have the proper calendar entries in the merged feed.

Putting it all together, here is what the command-line options for the application would look like:

```
--file=routes.txt --fuzzyDuplicates --file=calendar.txt --renameDuplicates
```     