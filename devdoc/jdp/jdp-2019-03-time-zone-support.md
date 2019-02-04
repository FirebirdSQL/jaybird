# jdp-2019-03: Time Zone Support

## Status

- Draft
- Proposed for: Jaybird 4

## Type

- Feature-Specification

## Context

Firebird 4 introduces `TIMESTAMP WITH TIME ZONE` and `TIME WITH TIME ZONE`
support. Details of this Firebird feature are documented in 
[README.time_zone.md](https://github.com/FirebirdSQL/firebird/blob/master/doc/sql.extensions/README.time_zone.md).

Relevant highlights:

- Protocol types encode the time in UTC/GMT with a 2-byte unsigned short id 
  encoding either an offset or a named time zone. The id is only relevant for 
  reconstructing the original offset or zone, but is not relevant for decoding 
  the time value as it always represents UTC.
- Encoded offsets range between -23:59 and +23:59 (encoded as id 0 - 2878)
- Named zones end (or 'start') at id 65535 (0xFFFF) for GMT and are defined in 
  `RDB$TIME_ZONES`; ids should be fixed (not change between releases, maybe new 
  ones added).
- A session time zone can be configured which sets the time zone of 
  `CURRENT_TIME(STAMP)` and the local time reported by `LOCALTIME(STAMP)`.
  This can be done with `set time zone {time zone '<name>' | local}` or through 
  a DPB item `isc_dpb_session_time_zone`.
- It is possible to transform the `WITH TIME ZONE` types to `WITHOUT TIME ZONE`
  types (ie `TIME` and `TIMESTAMP`) by setting `set time zone bind legacy`.
  
JDBC 4.2 (Java 8) introduced support for `TIMESTAMP WITH TIME ZONE` (`java.sql.Types.TIMESTAMP_WITH_TIMEZONE`) 
and `TIME WITH TIME ZONE` (`java.sql.Types.TIME_WITH_TIMEZONE`) mapping to 
`java.time.OffsetDateTime` and `java.time.OffsetTime`.

JDBC 4.2 (and 4.3) does not define a mapping for `java.sql.Time`, 
`java.sql.Timestamp`, `java.time.LocalDateTime`, `java.time.LocalTime` or 
`java.time.LocalDate` to the `WITH TIME ZONE` types. Nor does it define a 
mapping for `java.time.OffsetDateTime` and `java.time.OffsetTime` to the 
`WITHOUT TIME ZONE` types. See also JDBC 4.3 Appendix B.

The other `java.time` types (eg `ZonedDateTime` or `Instant`) are not mentioned 
in the JDBC specification.

For `WITHOUT TIME ZONE` types, JDBC requires default interpretation in the 
current JVM time zone.

Jaybird 4 will support Java 7 and higher, and Java 7 does not include `java.time`.

## Decision

**no final decisions yet**

1.  When persisting, only persists offsets, no named zones.

    Matches with JDBC requirement to support `java.time.OffsetTime`/`OffsetDateTime`.
    See also rejected option 2.
    
2.  On retrieval of `TIMESTAMP WITH TIME ZONE` value with a named zone, convert 
    to offset based on Java zone information.
    
    Requires mapping the Firebird named zone ids to Java time zone names.
    
    -   Rejected alternative: use offset 0 when retrieving named zone (see also 
        rejected option 1).

3.  On retrieval of `TIME WITH TIME ZONE` value with a named zone, convert to 
    offset based on the **current date** and the Java zone information, then
    remove date information.
    
    This can lead to inconsistent values depending on date, but aligns closer 
    with the transformations suggested in the SQL standard (section 4.6 in 
    ISO-9075-2:2016) - although the SQL standard itself only defines 
    offset-based zones - and it matches the behaviour used by Firebird when 
    applying `timetzvalue at time zone '...'` or 
    `extract(timezone_hour from timetzvalue)`.
    
    -   Rejected alternative: handle as 1970-01-01 => consistent value: result
        would be inconsistent with value obtained from 
        `cast(timetzvalue as timestamp with time zone)` and 
        `extract(timezone_hour from timetzvalue)` (and `timezone_minute`)
    -   Rejected alternative: handle as offset 0 => consistent value, no 
        conversion necessary: loss of information (see also rejected option 1).
        
4.  Support `java.time.OffsetDateTime` for `TIME WITH TIME ZONE`.

    Functionality is required by JDBC spec (see appendix B.5 in JDBC 4.3).

    Based on the SQL standard (section 4.6 in ISO-9075-2:2016) as _"Copy date 
    fields from CURRENT_DATE and time and time zone fields from SV \[Source 
    Value]"_. In the case of the named zone, we'll first add the current date,
    and then derive the offset (as done in decision 2, and similar in effect as 
    decision 3).
    
    In reverse, we'll convert to `OffsetTime` before persisting. 

5.  Support `java.time.OffsetTime` for `TIMESTAMP WITH TIME ZONE`.

    Functionality is not specified by JDBC, but this mirrors the `OffsetDateTime`
    support on `TIME WITH TIME ZONE` (see decision 4).
    
    Based on the SQL standard (section 4.6 in ISO-9075-2:2016) as _"Copy time 
    and time zone fields from SV \[Source Value]"_. In the case of a named zone,
    we'll first derive the offset (see decision 2), and then remove the date 
    information.
    
    In reverse, we'll convert to `OffsetDateTime` by adding the current date 
    before persisting.

6.  Only support `WITH TIME ZONE` on Java 8 and higher. Support will not be
    implemented for Java 7.

    This will require setting `set time zone bind legacy` for support on Java 7 
    (either by user or by Jaybird, see open options).
    
    Simplifies implementation in some parts, avoids some ambiguity in Java code 
    with mapping to `java.sql.Timestamp`/`java.sql.Time`.

### Open options or questions

-   How to handle setting time zone (connection property)? Especially effect on
    determining `java.sql.Time`/`java.sql.Timestamp`/`java.sql.Date` for 
    `WITHOUT TIME ZONE` types (and `WITH TIME ZONE` when legacy binding is set) 
    might be problematic.
    
-   Automatically set session time zone to JVM default, or leave unset?
 
    Leaving unset will retain backwards compatible behaviour for 
    `WITHOUT TIME ZONE` types. But setting it will lead to more correct 
    behaviour between values set from Jaybird and values assigned using 
    `LOCALTIMESTAMP` and other conversions.
    
    For example consider Firebird server at UTC+2, and Java application at 
    UTC+1, in Jaybird 3, use of `LOCALTIMESTAMP` returning a value of 13:00 (at 
    +02:00) will yield a time value of 13:00 (at +01:00) in Java. Setting the 
    session time zone would result in `LOCALTIMESTAMP` returning a value of 
    12:00 (at +01:00) and a time value of 12:00 (at +01:00) in Java.
    
-   Automatically set `set time zone bind legacy` from Java 7.

    This will automatically provide support, but having to execute Java 
    version dependent statements on connect will introduce some complexity.
    
-   Alternative to previous point: provide connection property to perform set 
    legacy binding on connect. 
    
### Rejected options

Time zone support in Jaybird will not include the following:

1.  Ignore time zone information and always provide `OffsetDateTime` or 
    `OffsetTime` at offset 0 (UTC/GMT), and set value at offset 0.
    
    This is similar to what the PostgreSQL JDBC driver does.
    
    Although this would be simpler, we have the information available to 
    preserve the offset when storing or retrieving (or derive it for a named
    zone). Using offset 0 (and/or id 65535 for GMT) should only be a fallback in 
    case of problems like unknown or invalid time zone ids.

2.  Support `java.time.ZonedDateTime`.

    Firebird supports both offset and named zones, while JDBC only specifies
    support for `OffsetDateTime` (and `OffsetTime`). Supporting `ZonedDateTime` 
    would allow full preservation of time zone information (assuming the Java 
    time zone can be mapped to a Firebird time zone).
    
    Defer to a future version as a possible improvement.

3.  Support `java.time.LocalDate`, `LocalTime` and `LocalDateTime` for 
    `WITH TIME ZONE` types.
    
    Not specified by JDBC. Possible confusion/ambiguity (eg is local in the 
    JVM zone or at the original offset?). See also other items. See also section 
    4.6 in ISO-9075-2:2016.
    
    If there is demand, we can always add it in later.
    
4.  Support `java.sql.Time` and `java.sql.Timestamp` for `WITH TIME ZONE` types.

    Although not specified by JDBC, this option allows for some flexibility for
    applications or libraries still expecting `java.sql.*` types. It will also
    ease migration between Java 7 and 8 or higher.
    
    Decided to prefer to abandon support for the legacy types. If people really
    need to use legacy types, they can use `set time zone bind legacy`.
    
5.  Support `java.sql.Date` for `TIMESTAMP WITH TIME ZONE` type.

    Although not specified by JDBC, this option allows for some flexibility for
    applications or libraries still expecting `java.sql.*` types. It will also
    ease migration between Java 7 and 8 or higher.
    
    Possible confusion/ambiguity. See also section 4.6 in ISO-9075-2:2016.
    
    Decided to prefer to abandon support for the legacy types, see also rejected
    option 4.

6.  Support `OffsetTime`/`OffsetDateTime` on `WITHOUT TIME ZONE` types.

    Not specified by JDBC. See also section 4.6 in ISO-9075-2:2016.
    
    If there is demand, we can always add it in later.
    
7.  Support for `java.time.Instant` on `TIMESTAMP WITH TIME ZONE`.
    
    On retrieval, do the necessary conversions and obtain `Instant`, on storage
    convert to `OffsetDateTime` with offset 0 and persist.

    Not specified by JDBC, could improve interoperability (a number of other
    JDBC drivers do this as well).
    
    If there is demand, we can always add it in later.

8.  Support for `java.time.Instant` on `TIME WITH TIME ZONE`.

    Not specified by JDBC, would mirror decision 4, but possibly more ambiguous.
    
    If there is demand, we can always add it in later.

## Consequences

*todo*
    