# jdp-2020-09 Add ZonedDateTime support

## Status

- Draft
- Proposed for: Jaybird 4.0.1, Jaybird 5
- Updates: jdp-2019-03

## Type

- Feature-Specification

## Context

Firebird 4 introduces `TIMESTAMP WITH TIME ZONE` and `TIME WITH TIME ZONE`
support. This feature was defined in jdp-2019-03 with support for the types
specified by JDBC 4.3 ( `java.time.OffsetDateTime` and `java.time.OffsetTime`).
However, Firebird 4 supports storing both offset as named time zones.

With the changes required for jdp-2020-06, we now decode to and encode from 
`ZonedDateTime` internally, so it makes to expose this in the driver as well.

## Decision

Jaybird will add support for `getObject(.., ZonedDateTime.class)` and 
`setObject(.., <ZonedDateTime-value>)` for `TIME WITH TIME ZONE` and `TIMESTAMP
WITH TIME ZONE`.

This type will not be supported on `TIME (WITHOUT TIME ZONE)` and `TIMESTAMP
(WITHOUT TIME ZONE)`.

## Consequence

Jaybird will support setting and getting `ZonedDateTime` on fields of type `TIME
WITH TIME ZONE` and `TIMESTAMP WITH TIME ZONE`.

The value will be obtained with the named or offset as the `zoneId`. Some time
zone names defined in Firebird do not exist in Java, and in that case the
mapping is to the same zone with a different alias or falls back to the UTC zone.

In reverse, if a Java zone mapping does not exist in Firebird, the value will be
stored with offset 0 (for UTC).

For `TIMESTAMP WITH TIME ZONE`, the retrieved or stored value is 'as-is'. For
`TIME WITH TIME ZONE` this is more complex.

On retrieval, for values with a named zone, the date 2020-01-01 is used as the
base date to derive the correct value in the zone for the UTC value received
from Firebird, the resulting `ZonedDateTime` is then rebased to the current date.

As an example, with a value of '20:58:00 Europe/Amsterdam', Firebird stores the
UTC value '19:58:00'. When retrieving the value at 2020-01-12, the returned
`ZonedDateTime` is '2020-01-12 20:58:00 Europe/Amsterdam' (that is '2020-01-12
19:58:00 UTC'), When retrieving the value at 2020-06-02, the returned `ZonedDateTime`
is '2020-06-02 20:58:00 Europe/Amsterdam' (that is '2020-06-02 18:58:00 UTC').

When storing a value with a named zone, the value is rebased on the date
2020-01-01 to derive a consistent UTC value. This UTC value is then stored
together with the named zone.

As an example, with a value of '2020-06-02 20:58:00 Europe/Amsterdam' (that is
'2020-06-02 18:58:00 UTC'), the value stored is '20:58:00 Europe/Amsterdam'
(that is '19:58:00 UTC').
