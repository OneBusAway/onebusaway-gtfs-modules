# GTFS Merge Command-Line Application

## Introduction

The `onebusaway-gtfs-merge-cli` command-line application is a tool for merging two or more
[GTFS](https://developers.google.com/transit/gtfs) feeds into a single feed.

## Getting the Application

You can download the application from Maven Central.

Go to https://repo1.maven.org/maven2/org/onebusaway/onebusaway-gtfs-merge-cli/, select the version
you want and get the URL for the largest jar file. An example would be
https://repo1.maven.org/maven2/org/onebusaway/onebusaway-gtfs-merge-cli/14.0.0/onebusaway-gtfs-merge-cli-14.0.0.jar

## Using the Application

The current build targets **Java 25**, so you need a Java 25 (or newer) runtime to run a jar built
from this source. (Older released jars were built against older Java versions.)

To run the application:

```
java -jar onebusaway-gtfs-merge-cli.jar [options] input_gtfs_path_a input_gtfs_path_b ... output_gtfs_path
```

The **last** positional argument is the output path; every argument before it is an input feed. Each
path may be a directory containing a GTFS feed or a `.zip` file. At least one input and one output are
required (i.e. two positional arguments minimum).

**Note**: Merging large GTFS feeds can be processor- and memory-intensive. You may need to raise the
JVM heap limit with an option like `-Xmx1G` (adjust as needed).

### How feeds are combined

Input feeds are processed in **reverse command-line order** — entities from the *last* feed listed are
added to the output first, and entities from earlier feeds are merged in afterward. When an entity
from an earlier feed collides with one already in the output and is *not* treated as a duplicate (see
below), its id is automatically renamed by prefixing it (e.g. `a-`, `b-`, …), and all references to it
are rewritten. This automatic renaming replaces the old `--renameDuplicates` flag, which no longer
exists.

## Configuring per-file behavior

Merge behavior is configured per GTFS file with the `--file` option, followed by options that apply to
that file. Options are matched to files **by position**: the *N*-th `--file` is paired with the *N*-th
`--duplicateDetection`. For example:

```
--file=routes.txt --duplicateDetection=fuzzy --file=calendar.txt --duplicateDetection=none
```

`--file` takes a real GTFS file name; it is resolved to an entity type through the GTFS schema, and an
unrecognized name causes the application to exit with an error. Recognized files include:

 - `agency.txt`
 - `stops.txt`
 - `routes.txt`
 - `trips.txt` and `stop_times.txt`
 - `calendar.txt` and `calendar_dates.txt`
 - `shapes.txt`
 - `frequencies.txt`
 - `transfers.txt`
 - `fare_attributes.txt`
 - `fare_rules.txt`
 - `feed_info.txt`

Files listed together (e.g. `trips.txt` and `stop_times.txt`) are handled by the same merge strategy,
so naming either configures both.

## Handling Duplicates

The main issue when merging GTFS feeds is handling duplicate entries between feeds: how to identify
duplicates, and what to do when one is found.

### Identifying Duplicates

By default, each file's merge strategy automatically picks the duplicate-detection approach it
considers best for that entity type. You can override this per file with `--duplicateDetection`:

 - `--duplicateDetection=identity`: two entries are the same if they share an id (e.g. stop id, route
   id, trip id). This is the stricter policy.

 - `--duplicateDetection=fuzzy`: two entries are the same if they share defining properties (e.g. stop
   name/location, route short name, trip stop sequence). This is the more lenient policy and is highly
   dependent on the entity type.

 - `--duplicateDetection=none`: entries are never considered duplicates, even with the same id or
   similar properties. Colliding ids are renamed instead of dropped (see "How feeds are combined").

Values are case-insensitive.

### Logging Duplicates

You can ask the tool to report or reject duplicates it drops:

 - `--logDroppedDuplicates` — log a warning when a duplicate is dropped.

 - `--errorOnDroppedDuplicates` — throw an exception and stop when a duplicate is dropped.

**Important:** these two flags are applied *inside* the per-`--file` configuration loop, so they only
take effect for files you have also named with `--file`. Passing at least one `--file` is required for
them to do anything (and, in the current code, for the merge to run without error).

### Other options

 - `--debug` — print the resolved merge strategies before the merge begins.
 - `--help`, `--version` — standard help/version output.

## Example: handling a service change

Agencies often schedule major changes around a particular date, publishing one feed for before the
change and another for after, and we want to merge them into a single feed with continuous coverage.

Suppose `agency.txt` and `stops.txt` are identical across the two feeds, so the default duplicate
handling drops the duplicates correctly. The `routes.txt` entries describe the same routes but use
different route ids, so we use fuzzy detection to match them. The `calendar.txt` file reuses the same
`service_id` values (e.g. `WEEK`, `SAT`, `SUN`) with different start/end dates in each feed — these are
*not* duplicates, so we force `none` detection, which makes the merger rename the colliding service
ids (e.g. to `a-WEEK`) and rewrite the affected `trips.txt` references. Both feeds' trips then keep
their correct calendars in the merged output.

Putting it together — with `earlier.zip` first and `later.zip` second so the later feed's ids win:

```
java -jar onebusaway-gtfs-merge-cli.jar \
  --file=routes.txt --duplicateDetection=fuzzy \
  --file=calendar.txt --duplicateDetection=none \
  earlier.zip later.zip merged.zip
```
