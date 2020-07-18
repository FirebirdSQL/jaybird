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

A related problem is the derivation of `OffsetDateTime` for a value in a named
zone. Given we 'add' the current date, the derivation either will preserve the
offset and yield the 'wrong' time in DST, or we need to rebase the time to
the current date using the named zone, and only then derive the `OffsetDateTime`.

## Decision

Jaybird will switch from using the current date to using 2020-01-01 for
conversion of `TIME WITH TIME ZONE` values with a named zone to
`java.time.OffsetTime`.

Given the questionable correctness of `CURRENT_TIME` (eg at '2020-06-02 20:58:00'
in Europe/Berlin, the time should be '20:58:00+02:00', but will be derived as
'20:58:00+01:00'), we should also consider defaulting to, or at minimum
supporting, derivation of an offset-based session time zone from a named zone.
We defer that that decision to jdp-2020-07.

For derivation of `OffsetDateTime` for named zones, the named zone should be
preserved by rebasing the time on the current date before deriving
the `OffsetDateTime`.

## Consequences

### `OffsetTime` from `TIME WITH TIME ZONE`

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

### `OffsetDateTime` from `TIME WITH TIME ZONE`

Deriving `OffsetDateTime` for a `TIME WITH TIME ZONE` value (with a named zone)
will first use 2020-01-01 to establish the time in the zone, then the time will
be moved to the right date and then the `OffsetDateTime` is derived. For example,
when the current date is 2020-06-02, from `20:58:00 Europe/Amsterdam` to
`2020-01-01 20:58:00 Europe/Amsterdam` to `2020-06-02 20:58:00 Europe/Amsterdam`,
and finally `2020-06-02 20:58:00+02:00`.

This will make the result of `OffsetTime` and `OffsetDateTime` for the same
value inconsistent during DST, and will disallow roundtripping values, but it
matches what happens when casting a `TIME WITH TIME ZONE` to a `TIMESTAMP WITH
TIME ZONE` and then obtaining the value as an `OffsetDateTime`. It will also
allow a view on both interpretations of the value.

### `OffsetTime` from `TIMESTAMP WITH TIME ZONE`

The `OffsetTime` derived from a `TIMESTAMP WITH TIME ZONE` will simply obtain
the `OffsetDateTime` and then call `getOffsetTime()`.

This will make the values consistent for the same value obtained as an
`OffsetDateTime`, but it is inconsistent with the cast behaviour of Firebird
from `TIMESTAMP WITH TIME ZONE` to `TIME WITH TIME ZONE` and then obtaining the
value as an `OffsetTime`.

We are aware of the inconsistency with our reasoning in _`OffsetDateTime` from
`TIME WITH TIME ZONE`_. In our opinion, having a value that is consistent both
as `OffsetTime` and `OffsetDateTime` is more important here, because the
presence of the date makes the value non-ambiguous.
