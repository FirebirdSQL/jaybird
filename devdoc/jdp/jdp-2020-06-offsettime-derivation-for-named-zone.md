# jdp-2020-06 OffsetTime derivation for named zone

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

## Decision

Jaybird will switch from using the current date to using 2020-01-01 for
conversion of `TIME WITH TIME ZONE` values with a named zone to
`java.time.OffsetTime`.

Given the questionable correctness of `CURRENT_TIME` (eg at 2020-06-02 20:58:00
in Europe/Berlin, the time should be 20:58:00+02:00, but will be derived as
20:58:00+01:00), we should also consider defaulting to, or at minimum supporting,
derivation of an offset-based session time zone from a named zone. We defer that
that decision to jdp-2020-07.

## Consequences

By switching to 2020-01-01 for derivation of `TIME WITH TIME ZONE` values with
named zones, the derivation by Jaybird will be consistent with derivation by
Firebird.

However, there will still be some inconsistencies:

For example - assuming exact same precision and 'moment in time' - in DST the
following will not produce a 'found' message, because `CURRENT_TIME` and
`OffsetTime.now()` will be off by one hour, as `CURRENT_TIME` would be
'20:58:00 Europe/Berlin' (or '19:58:00 UTC'), while `OffsetTime.now()` would be
'20:58:00+02:00' (or '18:58:00 UTC').

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

Possible fixes for this will be considered in jdp-2020-07.
 