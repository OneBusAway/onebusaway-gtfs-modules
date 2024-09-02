 ------
GTFS Transformation Command-Line Application
 ------
Brian Ferris
 ------
2011-08-17
 ------

## Introduction

The `onebusaway-gtfs-transformer-cli` command-line application is a simple command-line tool for transforming
[GTFS](https://developers.google.com/transit/gtfs) feeds.

### Requirements
  
  * Java 11 or greater

### Getting the Application
You can download the application here:

  
### Using the Application

To run the application:

```
java -jar onebusaway-gtfs-transformer-cli.jar [-args] input_gtfs_path ... output_gtfs_path
```

`input_gtfs_path` and `output_gtfs_path` can be either a directory containing a GTFS feed or a .zip file.

  <<Note>>: Transforming large GTFS feeds is processor and memory intensive.  You'll likely need to increase the
max amount of memory allocated to Java with an option like `-Xmx1G` or greater.  Adding the `-server` argument 
if you are running the Oracle or OpenJDK can also increase performance. 

### Arguments

  * `--transform=...` : specify a transformation to apply to the input GTFS feed (see syntax below)
  * `--agencyId=id` : specify a default agency id for the input GTFS feed
  * `--overwriteDuplicates` : specify that duplicate GTFS entities should overwrite each other when read
  
  
### Transform Syntax

Transforms are specified as snippets of example.  A simple example to remove a stop might look like:
  
```
{"op":"remove","match":{"file":"stops.txt","stop_name":"Stop NameP"}}
```

You can pass those snippets to the application in a couple of ways.  The simplest is directly on the command line.

```
--transform='{...}'
```

You can have multiple `--transform` arguments to specify multiple transformations.  However, if you have a LOT of
transformations that you wish to apply, it can be easier to put them in a file, with a JSON snippet per line.  Then
specify the file on the command-line:

```  
  --transform=path/to/local-file
```

You can even specify a URL where the transformations will be read:
  
```  
--transform=http://server/path
```

### Matching 

We provide a number of configurable transformations out-of-the-box that can do simple operations like adding,
updating, retaining, and removing GTFS entities.  Many of the transforms accept a "`match`" term that controls how the
rule matches against entities:

```
{"op":..., "match":{"file":"routes.txt", "route_short_name":"574"}}
```

Here, the match snippet at minimum requires a `file` property that specifies the type of GTFS entity to match.
Any file name defined in the [GTFS specification](https://developers.google.com/transit/gtfs/reference#FeedFiles)
can be used.

You can specify additional properties and values to match against as needed.  Again, use the field names defined for
each file name in the GTFS specification.  For example, the snippet above will match any entry in `routes.txt` with a
`route_short_name` value of `574`.

### Regular Expressions

  Property matching also supports regular expressions that allow you to match  property values conforming to a regexp pattern. For example, the snippet below will match any entry in `stops.txt` with a `stop_id` starting with `de:08`.

```
{"op":..., "match":{"file":"stops.txt", "stop_id":"m/^de:08.*/"}}
```

### Compound Property Expressions

  Property matching also supports compound property expressions that allow you to match across GTFS relational
references.  Let's look at a simple example:

```
{"op":..., "match":{"file":"trips.txt", "route.route_short_name":"10"}}
```

Here the special `routes` property references the route entry associated with each trip, allowing you to match
the properties of the route.  You can even chain references, like `route.agency` to match against the agency
associated  with the trip.  Here is the full list of supported compound property references:

```
{"op":..., "match":{"file":"routes.txt", "agency.name":"Metro"}}
{"op":..., "match":{"file":"trips.txt", "route.route_short_name":"10"}}
{"op":..., "match":{"file":"stop_times.txt", "stop.stop_id":"153"}}
{"op":..., "match":{"file":"stop_times.txt", "trip.route.route_type":3}}
{"op":..., "match":{"file":"frequencies.txt", "trip.service_id":"WEEKDAY"}}
{"op":..., "match":{"file":"transfers.txt", "fromStop.stop_id":"173"}}
{"op":..., "match":{"file":"transfers.txt", "toStop.stop_id":"173"}}
{"op":..., "match":{"file":"fare_rules.txt", "fare.currencyType":"USD"}}
{"op":..., "match":{"file":"fare_rules.txt", "route.route_short_name":"10"}}
```

### Multi-Value Matches

  The compound property expressions shown above are all for 1-to-1 relations, but matching also supports a limited
form of multi-value matching for 1-to-N relations.  Let's look at a simple example:

```
{"op":..., "match":{"file":"routes.txt", "any(trips.trip_headsign)":"Downtown"}}
```

Notice the addition of `any(...)` around the property name.  Here we are using a special `trips` property that
expands to include all trips associated with each route.  Now, if *any* trip belonging to the route has the specified
`trip_headsign` value, then the route matches.  Here is the full list of supported multi-value property matches:

```
{"op":..., "match":{"file":"agency.txt", "any(routes.X)":"Y"}}
{"op":..., "match":{"file":"routes.txt", "any(trips.X)":"Y"}}
{"op":..., "match":{"file":"trips.txt", "any(stop_times.X)":"Y"}}
```

### Collection-Like Entities

There are a number of GTFS entites that are more effectively collections identified by a common key.  For example,
shape points in `shapes.txt` linked by a common `shape_id` value or `calendar.txt` and `calendar_dates.txt` entries
linked by a common `service_id` value.  You can use a special `collection `match clause to match against the entire
collection.

```
{"op":..., "match":{"collection":"shape", "shape_id":"XYZ"}}
{"op":..., "match":{"collection":"calendar", "service_id":"XYZ"}}
```

You can use the calendar collection matches, for example, to retain a calendar, including all `calendar.txt`,
`calendar_dates.txt`, and `trip.txt` entries that reference the specified `service_id` value.  This convenient
short-hand is easier than writing the equivalent expression using references to the three file types separately.    

### Types of Transforms

#### Add an Entity

Create and add a new entity to the feed.

```
{"op":"add","obj":{"file":"agency.txt", "agency_id":"ST", "agency_name":"Sound Transit",
"agency_url":"http://www.soundtransit.org", "agency_timezone":"America/Los_Angeles"}}
```

#### Update an Entity

You can update arbitrary fields of a GTFS entity.

```
{"op":"update", "match":{"file":"routes.txt", "route_short_name":"574"}, "update":{"agency_id":"ST"}}
```

Normally, update values are used as-is.  However, we support a number of
special update operations:
  
### Find/Replace

```
{"op":"update", "match":{"file":"trips.txt"}, "update":{"trip_short_name":"s/North/N/"}}
```

By using `s/.../.../` syntax in the update value, the upda```te will perform
a find-replace operation on the specified property value.  Consider the
following example:

```
{"op":"update", "match":{"file":"trips.txt"}, "update":{"trip_short_name":"s/North/N/"}}
```
  
Here, a trip with a headsign of `North Seattle` will be updated to `N Seattle`.
  
### Path Expressions 

By using `path(...)` syntax in the update value, the expression will be
treated as a compound Java bean properties path expression.  This path
expression will be evaluated against the target entity to produce the update
value.  Consider the following example:
  
```
{"op":"update", "match":{"file":"trips.txt"}, "update":{"trip_short_name":"path(route.longName)"}}
```

Here, the `trip_short_name` field is updated for each trip in the feed.
The value will be copied from the `route_long_name` field of each trip's
associated route.

### Retain an Entity

We also provide a powerful mechanism for selecting just a sub-set of a feed.  
You can apply retain operations to entities you wish to keep and all the supporting entities referenced 
by the retained entity will be retained as well.  Unreferenced entities will be pruned.
 
In the following example, only route B15 will be retained, along with all the stops, trips, stop times, shapes, and agencies linked to directly by that route.

```
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"B15"}}
```

By default, we retain across [block_id](https://developers.google.com/transit/gtfs/reference#trips_block_id_field) values
specified in trips.txt.  That means if a particular trip is retained (perhaps because its parent route is retained),
and the trip specifies a block_id, then all the trips referencing that block_id will be retained as well, along with
their own routes, stop times, and shapes.  This can potentially lead to unexpected results if you retain one route and
suddenly see other routes included because they are linked by block_id.

You can disable this feature by specifying `retainBlocks: false` in the JSON transformer snippet.  Here is an
example:

```
{"op":"retain","match":{"file":"routes.txt", "route_short_name":"B15"}, "retainBlocks":false}
```

### Remove an Entity

You can remove a specific entity from a feed.

```
{"op":"remove", "match":{"file":"stops.txt", "stop_name":"Stop Name"}}
```

Note that removing an entity has a cascading effect.  If you remove a trip, all the stop times that depend on that
trip will also be removed.  If you remove a route, all the trips and stop times for that route will be removed.
  
### Trim a Trip

You can remove stop times from the beginning or end of a trip using the "trim_trip" operation.  Example:
  
```
{"op":"trim_trip", "match":{"file":"trips.txt", "route_id":"R10"}, "from_stop_id":"138S"}
```

For any trip belonging to the specified route and passing through the specified stop, all stop times from the specified
stop onward will be removed from the trip.  You can also remove stop times from the beginning of the trip as well:

```
{"op":"trim_trip", "match":{"file":"trips.txt", "route_id":"R10"}, "to_stop_id":"138S"}
```

Or both:

```
{"op":"trim_trip", "match":{"file":"trips.txt", "route_id":"R10"}, "to_stop_id":"125S", "from_stop_id":"138S"}
```

### Generate Stop Times

You can generate stop time entries for a trip.  Example:
  
```
{"op":"stop_times_factory", "trip_id":"TRIP01", "start_time":"06:00:00", "end_time":"06:20:00", "stop_ids":["S01", "S02", "S03"]}
```

A series of entries in `stop_times.txt` will be generated for the specified trip, traveling along the specified sequence of
stops.  The departure time for the first stop will be set from the `start_time` field, the arrival time for the last stop will
be set from the `end_time` field, and the times for intermediate stops will be interpolated based on their distance along the
trip.

### Extend Service Calendars

Sometimes you need to extend the service dates in a GTFS feed, perhaps in order to temporarily extend an expired feed.  Extending
the feed by hand can be a tedious task, especially when the feed uses a complex combination of `calendar.txt` and `calendar_dates.txt`
entries.  Fortunately, the GTFS tranformer tool supports a `calendar_extension` operation that can help simplify the work.  Example:

```
{"op":"calendar_extension", "end_date":"20130331"}
```

The operation requires just one argument by default: `end_date` to specify the new end-date for the feed.  The operation does
its best to intelligently extend each service calendar, as identified by a `service_id` in `calendar.txt` or `calendar_dates.txt`.
There are a few wrinkles to be aware of, however.

Extending a `calendar.txt` entry is usually just a matter of setting a new `end_date` value in the feed.  Extending a service
calendar represented only through `calendar_dates.txt` entries is a bit more complex.  For such a service calendar, we attempt to
determine which days of the week are typically active for the calendar and extend only those.  For example, is the calendar is
always active on Saturday but has one or two Sunday entries, we will only add entries for Saturday when extending the calendar.

Also note that we will not extend "inactive" service calendars.  A service calendar is considered inactive if its last active
service date is already in the past.  By default, any calendar that's been expired for more than two weeks is considered inactive.
This helps handle feeds that have merged two service periods in one feed.  For example, one calendar active from June 1 - July 31
and a second calendar active from August 1 to September 31.  If it's the last week of September and you are extending the feed,
you typically only mean to extend the second service calendar.  You can control this inactive calendar cutoff with an optional
argument:

```
{"op":"calendar_extension", "end_date":"20130331", "inactive_calendar_cutoff":"20121031"}
```

Calendars that have expired before the specified date will be considered inactive and won't be extended.

_Note_: We don't make any effort to extend canceled service dates, as specified in `calendar_dates.txt` for holidays and
other special events.  It's too tricky to automatically determine how they should be handled.  You may need to still handle
those manually. 

### Deduplicate Calendar Entries

Finds GTFS service_ids that have the exact same set of active days and consolidates each set of duplicated
ids to a single service_id entry.

```
{"op":"deduplicate_service_ids"}
```
  
### Merge Trips and Simplify Calendar Entries

Some agencies model their transit schedule favoring multiple entries in calendar_dates.txt as opposed to a more concise
entry in calendar.txt.  A smaller number of agencies take this scheme even further, creating trips.txt entries for each
service date, even when the underlying trips are exactly the same.  This can cause the size of the GTFS to grow dramatically
as trips and stop times are duplicated.

We provide a simple transformer that can attempt to detect these duplicate trips, remove them, and simplify the underlying
calendar entries to match.  To run it, apply the following transform:

```
{"op":"calendar_simplification"}
```

The transform takes additional optional arguments to control its behavior:
  
  * min_number_of_weeks_for_calendar_entry - how many weeks does a service id need to
    span before it gets its own entry in calendar.txt (default=3)

  * day_of_the_week_inclusion_ratio - if a service id is being modeled with a
    calendar.txt entry, how frequent does a day of the week need to before it's
    modeled positively in calendar.txt with any negative exceptions noted in
    calendar_dates.txt, vs making no entry for that day of the week in
    calendar.txt and instead noting any positive exceptions in
    calendar_dates.txt.  This is useful for filtering out a calendar that is
    always active on Sunday, but has one or two Mondays for a holiday.
    Frequency is defined as how often the target day of the week occurs vs the
    count for day of the week appearing MOST frequently for the service id
    (default=0.5)
  
  * undo_google_transit_data_feed_merge_tool - set to true to indicate that merged trip ids,
    as produced by the [GoogleTransitDataFeedMergeTool](http://code.google.com/p/googletransitdatafeed/wiki/Merge),
    should be un-mangled where possible.  Merged trip ids will often have the form
    `OriginalTripId_merged_1234567`.  We attempt to set the trip id back to `OrginalTripId`
    where appropriate.
  
    
### Shift Negative Stop Times

Some agencies have trips that they model as starting BEFORE midnight on a given service date.  For these agencies, it
would be convenient to represent these trips with negative arrival and departure times in stop_times.txt.  The GTFS spec and
many GTFS consumers do not support negative stop times, however.

To help these agencies, we provide a transform to "fix" GTFS feeds with negative stop times by identifying such trips,
shifting the arrival and departure times to make them positive, and updating the service calendar entries for these trips
such that the resulting schedule is semantically the same.

To run it, apply the following transform:

```
{"op":"shift_negative_stop_times"}
```
    
_A note on negative stop times:_ When writing negative stop times, the negative value ONLY applies to the hour portion
  of the time.  Here are a few examples:
  
  * "-01:45:00" => "23:45:00" on the previous day
  
  * "-05:13:32" => "19:13:32" on the previous day 

* Remove non-revenue stops

  Stop_times which do not allow pick up or drop off are also known as non-revenue stops. Some GTFS consumers display 
  these stops as if they were stops that passengers can use, at which point it is helpful to remove them. 
  
  To remove them, apply the following transform:
  
```
{"op":"remove_non_revenue_stops"}
```

  Terminals (the first and last stop_time of a trip) can be excluded from removal with the following transform:

```
{"op":"remove_non_revenue_stops_excluding_terminals"}}
```

* Replacing trip_headsign with the last stop

  Certain feeds contain unhelpful or incorrect trip_headsign. They can be replaced with the last stop's stop_name.

```
{"op":"last_stop_to_headsign"}
```

### Arbitrary Transform

We also allow you to specify arbitrary transformations as well.  Here, you specify your transformation class and we will
automatically instantiate it for use in the transform pipeline.

```
{"op":"transform", "class":"some.class.implementing.GtfsTranformStrategy"}
```

We additionally provide a mechanism for setting additional properties of the transform.  For all additional properties
specified in the JSON snippet, we will attempt to set that Java bean property value on the instantiated transformation object.
See for example:

```
{"op":"transform", "class":"org.onebusaway.gtfs_transformer.updates.ShapeTransformStrategy", "shape_id":"6010031", \
"shape":"wjb~G|abmVpAz]v_@@?wNE_GDaFs@?@dFX`GGjN__@A"}
```

Here, we set additional properties on the `ShapeTransformStrategy`, making it possible to reuse and configure a generic
transformer to your needs.

Additional Examples

### How to Reduce your GTFS

We can apply a modification that retains certain GTFS entities and all other entities required directly or indirectly by
those entities.  For example, create a file with the following contents (call it modifications.txt, as an example):

```
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"B15"}}
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"B62"}}
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"B63"}}
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"BX19"}}
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"Q54"}}
{"op":"retain", "match":{"file":"routes.txt", "route_short_name":"S53"}}
```

Then run:

```
java -jar onebusaway-gtfs-transformer-cli.jar --transform=modifications.txt source-gtfs.zip target-gtfs.zip
```

The resulting GTFS will have the retained only the routes with the matching short names and all other entities required
to support those routes.

* Add a Full Schedule to an Existing Feed

Consider an existing feed with a number of routes and stops.  We can add an entirely new route, with trips and stop-times
and frequency-based service, using the transform.  This can be handy to add temporary service to an existing feed.
  
```
{"op":"add", "obj":{"file":"routes.txt", "route_id":"r0", "route_long_name":"Temporary Shuttle", "route_type":3}}

{"op":"add", "obj":{"file":"calendar.txt", "service_id":"WEEKDAY", "start_date":"20120601", "end_date":"20130630", "monday":1, "tuesday":1, "wednesday":1, "thursday":1, "friday":1}}

{"op":"add", "obj":{"file":"trips.txt", "trip_id":"t0", "route_id":"r0", "service_id":"WEEKDAY", "trip_headsign":"Inbound"}}
{"op":"add", "obj":{"file":"trips.txt", "trip_id":"t1", "route_id":"r0", "service_id":"WEEKDAY", "trip_headsign":"Outbound"}}

{"op":"add","obj":{"file":"frequencies.txt","trip_id":"t0","start_time":"06:00:00","end_time":"22:00:00","headway_secs":900}}
{"op":"add","obj":{"file":"frequencies.txt","trip_id":"t1","start_time":"06:00:00","end_time":"22:00:00","headway_secs":900}}

{"op":"stop_times_factory", "trip_id":"t0", "start_time":"06:00:00", "end_time":"06:20:00", "stop_ids":["s0", "s1", "s2", "s3"]}
{"op":"stop_times_factory", "trip_id":"t1", "start_time":"06:00:00", "end_time":"06:20:00", "stop_ids":["s3", "s2", "s1", "s0"]}
```

     
