# jdp-2020-08 Int128 support

## Status

- Published: 2020-07-02
- Implemented in: Jaybird 4.0.1, Jaybird 5

## Type

- Feature-Specification

## Context

Initially Firebird 4 introduced higher precision numerics (precision > 18)
backed by a Decimal128 (precision = 34), this was later changed to an Int128
(precision = 38, max 39 partial range). This meant that internally, Firebird had
an Int128 type, but this was not exposed as a datatype on its own, only as
`NUMERIC` (subtype 1) and `DECIMAL` (subtype 2).

This has recently changed by exposing the Int128 type as SQL type `INT128`
(subtype 0, no scale). If Firebird hadn't defined `BIGINT` as Int64, `BIGINT`
would have been an appropriate name. However, now it is the bigger brother of
`BIGINT`, and the standard defines no name for this type.

In like, JDBC also does not define a type for `INT128`. The most suitable type
in Java to map an `Int128` is `java.math.BigInteger`. The JDBC 4.3 specification
does not define a mapping for `BigInteger` in tables B.1 _JDBC Types Mapped to
Java Types_, B.2 _Java Types Mapped to JDBC Types_ or B.3 _JDBC Types Mapped to
Java Object Types_. Table B.4 _Java Object Types Mapped to JDBC Types_
establishes a mapping that setting with `java.math.BigInteger` maps to `BIGINT`.
Table B.5 _Conversions by setObject and setNull from Java Object
Types to JDBC Types_ establishes a mapping with types `BIGINT`, `CHAR`,
`VARCHAR`, `LONGVARCHAR`.

It would then seem natural to map `INT128` to `BIGINT` as well, however JDBC
establishes `long` as the 'standard' type for `BIGINT`, resulting in a common
expectation that a `BIGINT` is always 64-bit.

On the other hand, given the close connection with the SQL types `NUMERIC` and
`DECIMAL`, mapping to a `java.math.BigDecimal` and identifying as JDBC `NUMERIC`
or `DECIMAL` will work in all cases. We would then consider `INT128` as a
special case of `NUMERIC(38)` or `NUMERIC(38,0)` (or `DECIMAL(38)` etc.).

When mapping as JDBC type `NUMERIC` or `DECIMAL`, it is possible that tools can
misinterpret the mapping (for example when reverse-engineering database schemas),
and incorrectly identify the column as - for example - `NUMERIC(38)`. However,
even a column (or cast) of that type would still be able to hold the full range
of `INT128`.  

## Decision

1.  Jaybird will identify an `INT128` as JDBC type `NUMERIC` with precision 38.

    This mapping offers the least compatibility problems, relying on tool and
    library support for `NUMERIC` and `java.math.BigDecimal` handling to work
    correctly for the full range of values.
    
    It also fits with the current behaviour of Jaybird to map subtype 0, but
    with a negative scale to `NUMERIC`.
    
    The choice of precision 38 (instead of 39), allows for compatibility with
    tools or libraries that will generate a `NUMERIC(p)` instead of `INT128` in
    casts or column definitions.
    
2.  The following mappings to/and from `INT128` will be supported:

    - `java.math.BigDecimal` - silent rounding, exception when out of range on
      set
    - `java.math.BigInteger` - exception when out of range on set
    - `String` - conversion through `java.math.BigDecimal`
    - `long/Long` - exception when out of range on get
    - `int/Integer` - exception when out of range on get
    - `short/Short` - exception when of range on get
    - `byte/Byte` - exception when out of range (using range -128 ... 127)
    - `double/Double` - silent rounding, exception when out of range on set
    - `float/Float` - silent rounding, exception when out of range on set
    
    The following mapping exists, but should be considered unsupported. We may
    remove or change it in the future:
    
    - `boolean/Boolean` - on set `true` is `1` and `false` is `0`, on get `1` is
      `true`, other values `false` or exception when out of byte range (this
      support for `boolean` and the current oddities are for historic reasons of
      the `BigDecimal` implementation in Jaybird, and might change in the future)

### Rejected options

The following options have been considered and rejected:

1.  Mapping `INT128` to JDBC type `BIGINT`.

    We expect that the close tie of `BIGINT` with a 64-bit `long` results in
    ambiguity, and failure of tools and libraries to correctly handle values
    that exceed the 64-bit range.
    
    1.  Mapping `INT128` to JDBC type `BIGINT`, but defaulting `getObject` to
        `java.math.BigInteger`.
        
        This would violate the JDBC specification for the default `getObject`
        mapping of JDBC type `BIGINT`, and we're unsure if this would be
        correctly handled by tools and libraries.

2.  Define a custom JDBC type (similar to what we did for `DECFLOAT`).

    In the case of `DECFLOAT`, the floating-point aspect was so different from
    the fixed-point aspect of `NUMERIC`/`DECIMAL` that we decided a separate
    type was the better option, especially given `DECFLOAT` is defined in the
    SQL standard, and we have the expectation it will eventually be defined by
    JDBC.
    
    That is not the case here, and we consider handling it as just a `NUMERIC`
    will yield fewer problems.

3.  Map to JDBC type `OTHER`.

    JDBC type `OTHER` is so unspecific we think this is even less attractive
    than defining a custom type.
    
4.  Map to JDBC type `DECIMAL`.

    Given the implementation in Jaybird, the choice between `DECIMAL` and
    `NUMERIC` has little consequence. However, historically, Jaybird has mapped
    subtype 0 with a negative scale to `NUMERIC`, so we follow that behaviour. 

## Consequences

Jaybird will identify `INT128` as a JDBC type `NUMERIC`. Its handling will be
identical to the current extended precision numeric support, but - where
possible - Jaybird will identify the SQL column type as `INT128`.

The existing behaviour to return JDBC type `DECIMAL` for Int128 with unknown
subtypes or subtype 0 with scale >= 0 will be changed to return JDBC type
`NUMERIC`.
