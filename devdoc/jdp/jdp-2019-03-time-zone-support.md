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
  types (ie `TIME` and `TIMESTAMP`) by setting `set time zone bind legacy` or 
  DPB item `isc_dpb_time_zone_bind`.
  
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
    
2.  On retrieval of a `TIMESTAMP WITH TIME ZONE` value with a named zone, 
    convert to offset based on Java zone information.
    
    Requires mapping the Firebird named zone ids to Java time zone names.
    
    -   Rejected alternative: use offset 0 when retrieving named zone (see also 
        rejected option 1).

3.  On retrieval of a `TIME WITH TIME ZONE` value with a named zone, convert to 
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

    This will require setting `set time zone bind legacy` or `timeZoneBind=legacy` 
    for support on Java 7 (by user, see also point 7 below).
    
    Simplifies implementation in some parts, avoids some ambiguity in Java code 
    with mapping to `java.sql.Timestamp`/`java.sql.Time`.
    
7.  Provide connection property `timeZoneBind` to set the time zone bind (native 
    (default) or legacy). This will map to Firebird DPB item 
    `isc_dpb_time_zone_bind`.
    
    Java 7 users will need to explicitly set this if they want to use `WITH TIME 
    ZONE` types (including `CURRENT_TIME` and `CURRENT_TIMESTAMP`).
    
    See also rejected option 9.
    
8.  Automatically set session time zone to JVM default on connect.
     
    Setting the session time zone to the JVM default will lead to more correct 
    behaviour between values set from Jaybird and values assigned using 
    `LOCALTIMESTAMP` and other conversions. It will also closer align to JDBC 
    expectations surrounding time.
    
    For example consider Firebird server at UTC+2, and Java application at 
    UTC+1, in Jaybird 3, use of `LOCALTIMESTAMP` returning a value of 13:00 (at 
    +02:00) will yield a time value of 13:00 (at +01:00) in Java. Setting the 
    session time zone would result in `LOCALTIMESTAMP` returning a value of 
    12:00 (at +01:00) and a time value of 12:00 (at +01:00) in Java.
    
9.  Provide connection property (`sessionTimeZone`) to explicitly set the
    session time zone.

    The specified (or default) session time zone will define how a value is 
    derived for `java.sql.Time`/`java.sql.Timestamp`/`java.sql.Date` for 
    `WITHOUT TIME ZONE` types (and `WITH TIME ZONE` when time zone bind is set 
    to legacy). 
    
    That means instead of the JVM default, the session time zone will be used
    to derive the value from the date/time information sent by Firebird. This 
    can lead to unexpected behaviour for `java.sql.Date` (ie being one day off 
    in the JVM default) if set to another zone than the JVM default.
    
    We accept this behaviour and encourage people to use the `java.time` types.

10. Provide option as part of connection property (item 9) to unset session time 
    zone of item 8.
     
    Session time zone value `server` will not set the session time zone. The 
    Firebird server will use its default time zone, while the JVM uses the JVM 
    default time zone (these might be different!). This will retain backwards 
    compatible behaviour for `WITHOUT TIME ZONE` types and use JVM default time 
    zone for interpretation for `java.sql.Time`/`java.sql.Timestamp`/`java.sql.Date`.
    
11. The driver-side session time zone for deriving time/timestamp values will
    also be applied when connecting to earlier Firebird versions.
    
12. Support legacy types `java.sql.Time`, `java.sql.Timestamp` for the `WITH 
    TIME ZONE` types.
    
    Although not specified by JDBC, this option allows for some flexibility for
    applications or libraries still expecting `java.sql.*` types. It will also
    ease migration between Java 7 and 8 or higher.
    
    Firebird to Java conversion will derive the `OffsetDateTime` and then convert 
    this to epoch milliseconds, which is then used for constructing 
    `java.sql.Time` or `java.sql.Timestamp`. This will yield slightly different
    results compared to `TIME WITHOUT TIME ZONE` type as there the conversion
    applies 1970-01-01 instead of the current date.
    
    Java to Firebird conversion will derive an `OffsetDateTime` using 
    `toLocalTime()` and the current date, or `toLocalDateTime()`, combined with 
    the default JVM time zone (it will not apply `sessionTimeZone`).
    
    Methods with parameters of type `java.util.Calendar` will ignore the 
    calendar.
    
13. Support legacy types `java.sql.Date` for the `TIMESTAMP WITH TIME ZONE` type.
     
    Although not specified by JDBC, this option allows for some flexibility for
    applications or libraries still expecting `java.sql.*` types. It will also
    ease migration between Java 7 and 8 or higher.
    
    Firebird to Java conversion will derive the `OffsetDateTime` and then convert 
    this to epoch milliseconds, which is then used for constructing 
    `java.sql.Date`.
    
    Java to Firebird conversion will derive an `OffsetDateTime`
    using `toLocalDate()` at start of day combined with the default JVM time 
    zone (it will not apply `sessionTimeZone`).
    
    Methods with parameters of type `java.util.Calendar` will ignore the 
    calendar.

### Open options or questions

_none at the moment_
    
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
    
4.  _removed, see decision 12_
    
5.  _removed, see decision 13_

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
    
9.  Automatically set `set time zone bind legacy` from Java 7.
    
    This would automatically provide support, but having to execute Java 
    version dependent statements on connect will introduce some complexity,
    in addition Java 7 is end-of-life, so we don't want to spend too much time
    on it.
    
    See also accepted option 7.

## Consequences

See [Decision] and [Rejected options].
    