# jdp-2020-01: Extended Time Zone Types Support

## Status

- Published: 2020-03-07
- Implemented in: Jaybird 4
- Updates: jdp-2019-03

## Type

- Feature-Specification

## Context

Firebird 4 snapshot 4.0.0.1795 introduces two 'bind-only' data types, `EXTENDED
TIME WITH ZONE` and `EXTENDED TIMESTAMP WITH TIME ZONE`. These new types have an
extra signed short with the time zone offset in minutes (serialized as 4 extra
bytes in the wire protocol). For offset based types, the offset is present twice,
for named zones, the offset is present in addition to the zone identifier.

These data types are intended to address concerns for clients that do not or
cannot resolve the named zones, but do not want to fall back to UTC time and
instead want to preserve the actual offset.

These types are only surfaced through `SET BIND`/`isc_dpb_set_bind`.

## Decision

Jaybird will add minimal support for these 'extended' time zone types by
handling them exactly the same as the normal time zone types. This effectively
means that the secondary offset information will always be ignored.

### Rejected alternatives

The following options were considered but rejected:

1.  Using the extra offset information to determine the offset.

    As we need to support the normal time zone types as well, this would add
    more complexity to derive the same information.
    
2.  Do option 1, and always map to the 'extended' types using `isc_dpb_set_bind`.

    This would allow us to uniformly handle with time zone types by always using
    the secondary offset information. This would remove the need for handling
    named zones in Jaybird.
    
    As users could manually change the bind to the normal time zone types, we
    would still need some form of support of the normal time zone types, for
    example only supporting the offset types or always falling back to UTC.

Although option 2 could simplify code in Jaybird, considering we'd need some
form of support of the normal time zone types, the support for those normal
types already exists, the core developers consider the 'extended' types a
temporary or transitional feature, and the extra 4 bytes overhead in the wire
protocol, made us decide to reject these options (where we consider option 1 a
subset of option 2).
 
## Consequences

Jaybird will provide minimal support for `EXTENDED TIME(STAMP) WITH TIME ZONE`,
by always handling it is their normal `TIME(STAMP) WITH TIME ZONE` counterpart.

In a situation where Jaybird cannot determine the offset of a named zone, this
means we will fall back to UTC, just like we do for the normal time zone types.
This also preserves consistency between 'extended' and normal types (e.g. in
situations where Jaybird and Firebird might disagree how to derive the offset
for a named zone).

We recommend users not to set their binds to the 'extended' types, but if they
do its behaviour will be identical.
