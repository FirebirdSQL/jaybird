# jdp-2020-07 Offset conversion for named session zone

## Status

- Draft
- Proposed for: Jaybird 4.0.1, Jaybird 5
- Updates: jdp-2019-03

## Type

- Feature-Specification

## Context

When jdp-2019-03 was proposed and implemented, the behaviour of the displaying
a `TIME WITH TIME ZONE` for a named zone, or converting a `TIME WITH TIME ZONE`
for a named zone to an offset, applied the current date. This led to
inconsistencies, where the same time value would result in two different values
depending on the date.

For example '12:00:00 Europe/Berlin', could be displayed as 
'12:00:00 Europe/Berlin', '11:00:00 Europe/Berlin' or '13:00:00 Europe/Berlin'
depending on whether the value was created in wintertime or summertime, and
whether it was displayed in wintertime or summertime.

Similarly, retrieval in Jaybird could lead to values '12:00:00+02:00', 
'12:00:00+01:00', '11:00:00+01:00' and 13:00:00+01:00.

Just before Firebird 4 beta 2 (snapshot 4.0.0.1954), a change was made to fix
the date of derivation for `TIME WITH TIME ZONE` with a named zone to 2020-01-01.
With this change, the behaviour in Jaybird is now incorrect in summertime. For
example, selecting `CURRENT_TIME` at 2020-06-02 20:58:00 in Europe/Berlin will
result in an `OffsetTime` of '21:58:00+02:00', where the 'correct' value would
be '20:58:00+01:00' as the time is derived at date 2020-01-01.

See also: jdp-2020-06.

This derivation has some questionable qualities, for example when the default
session time zone is applied (in this example Europe/Berlin), the value of
`CURRENT_TIME` will yield '20:58:02 Europe/Berlin' ('19:58:02 UTC), but from the
perspective of Java, in DST, the value of `OffsetTime.now()` will be
'20:58:02+02:00' ('18:58:02 UTC'). So, `CURRENT_TIME` and `OffsetTime.now()`
are off by one hour during DST.

In other words - assuming exact same precision and 'moment in time' - in DST the
following will not produce a 'found' message:

```java
try (var pstmt = connection.prepareStatement(
    "select 1 from rdb$database where current_time = ?")) {
  pstmt.setObject(1, OffsetTime.now());
  try (var rs = pstmt.executeQuery()) {
    if (rs.next()) {
      System.out.println("found");
    }
  }
}
```

## Decision

TODO ...

## Consequences

TODO ...
 