When applying GTFS transform operations, often the ultimate value to be matched or to be set cannot be determined at the time the transforms are constructed.  Instead, the value must be resolved when the transform operation is applied.  We refer to this as "deferred" matching and setting.  This package contains classes to assist in these operations.

Take the following example:

```
{"op":"add", "obj":{"file":"routes.txt", "route_id":"r0", "route_type":3}}
{"op":"update", "match":{"file":"trips.txt", "trip_id":"t0"}, "update":{"route_id":"r0"}}
```

The intention seems clear: add a new route and update a trip to reference the route.  Simple enough, right?

Unfortunately, it's a bit trickier than that.  The OneBusAway GTFS Trip object references its Route directly (as opposed to a simple string id).  When modifying the underlying trip, that reference must be updated.  So setting the route_id to "r0" actual means looking up the Route with the specified id and using that as the assignment value.

Considering the route did not even exist until the previous "add" operation was processed, the update operation can only be properly evaluated at the moment of execution.
