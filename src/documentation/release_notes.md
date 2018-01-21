WARNING {-}
=======

Jaybird 4 is still in development. This version is provided for testing
purposes only. We'd appreciate your feedback, but we'd like to emphasize that
this version is **not intended for production**.

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the Firebird-java mailing list or reported on the issue tracker
<http://tracker.firebirdsql.org/browse/JDBC>.

General Notes
=============

Jaybird is a JCA/JDBC driver suite to connect to Firebird database servers. 

This driver is based on both the JCA standard for application server
connections to enterprise information systems and the well-known JDBC standard.
The JCA standard specifies an architecture in which an application server can
cooperate with a driver so that the application server manages transactions,
security, and resource pooling, and the driver supplies only the connection
functionality. While similar to the JDBC XADataSource concept, the JCA
specification is considerably clearer on the division of responsibility between
the application server and driver.

About this version
------------------

*TODO*

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the Firebird-java mailing list or reported on the issue tracker
<http://tracker.firebirdsql.org/browse/JDBC>.

Supported Firebird versions
---------------------------

Jaybird @VERSION@ was tested against Firebird 2.5.7, and 3.0.2, but should also 
support other Firebird versions from 2.5 and up.

Formal support for Firebird 2.0 and 2.1 has been dropped (although in general we 
expect the driver to work). The Type 2 and embedded server JDBC drivers use JNA to
access the Firebird client or embedded library.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

### Notes on Firebird 3 support

Jaybird 4 does not (yet) support the Firebird 3 zlib compression.

Supported Java versions
-----------------------

Jaybird 4 supports Java 7 (JDBC 4.1) and Java 8 (JDBC 4.2). Support for 
earlier Java versions has been dropped.

Rudimentary support for Java 9 (JDBC 4.3) is available using the Java 8 version,
but real module support will not be available until Jaybird 4 (or later).

Jaybird 4 will probably drop support Java 7 later in the development cycle.

Specification support
---------------------

Jaybird supports the following specifications:

|Specification|Notes
|-------------|----------------------------------------------------------------
| JDBC 4.3    | Driver implements all JDBC 4.3 methods for features supported by Firebird; Java 9 supported using the Java 8 driver.
| JDBC 4.2    | Driver implements all JDBC 4.2 methods for features supported by Firebird.
| JDBC 4.1    | Driver implements all JDBC 4.1 methods for features supported by Firebird.
| JDBC 4.0    | Driver implements all JDBC 4.0 interfaces and supports exception chaining.
| JCA 1.0     | Jaybird provides implementation of `javax.resource.spi.ManagedConnectionFactory` and related interfaces. CCI interfaces are not supported. Although Jaybird depends on the JCA 1.5 classes, JCA 1.5 compatibility is currently not guaranteed.
| JTA 1.0.1   | Driver provides an implementation of `javax.transaction.xa.XAResource` interface via JCA framework and `XADataSource` implementation.
| JMX 1.2     | Jaybird provides a MBean to manage Firebird servers and installed databases via JMX agent.

Getting Jaybird 3.0
===================

Jaybird @VERSION@
-------------------

### Maven ###

Jaybird @VERSION@ is available from Maven central:

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird-jdkXX` (where `XX` is `17` or `18`).\
Version: `@VERSION@`

For example:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>@VERSION@</version>
</dependency>
~~~

If your application is deployed to a Java EE application server, you will need to
exclude the `javax.resource:connector-api` dependency, and add it as a provided 
dependency:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>@VERSION@</version>
    <exclusions>
        <exclusion>
            <groupId>javax.resource</groupId>
            <artifactId>connector-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>javax.resource</groupId>
    <artifactId>connector-api</artifactId>
    <version>1.5</version>
    <scope>provided</scope>
</dependency>
~~~

If you want to use Type 2 support (native, local or embedded), you need to 
explicitly include JNA 4.4.0 as a dependency:

~~~ {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>4.4.0</version>
</dependency>
~~~

We plan to make native and embedded support a separate library in future 
releases, and provide Firebird client libraries as Maven dependencies as well.

See also [Type 2 (native) and embedded driver].

### Download ###

You can download the latest versions from <https://www.firebirdsql.org/en/jdbc-driver/>

At minimum Jaybird 4 requires `jaybird-@VERSION@.jar` and 
`connector-api-1.5.jar`. You can also use `jaybird-full-@VERSION@.jar` which
includes the connector-api files.

If you deploy your application to a Java EE application server, then you must 
use `jaybird-@VERSION@.jar` (not `-full`!), and **not** include 
`connector-api-1.5.jar` as this dependency will be provided by your application 
server.

For `getGeneratedKeys` support you will need to include 
`antlr-runtime-4.7.jar` on your classpath.

For native, local or embedded support, you will need to include `jna-4.4.0.jar` 
on your classpath. See also [Type 2 (native) and embedded driver].

Upgrading from Jaybird 3 to Jaybird 4
=====================================

Maven
-----

Upgrade the version of the dependency to @VERSION@. If you use native or 
embedded.

For more detailed instructions, see also the information on Maven in
[Getting Jaybird 4]. 

Manual install
--------------

If you manage your dependencies manually, you need to do the following:

1.  Replace the Jaybird 3 library with the Jaybird 4 version
    - `jaybird-3.0.x.jar` with `jaybird-@VERSION@.jar` 
    - `jaybird-full-3.0.x.jar` with `jaybird-full-@VERSION@.jar`
    
Gotcha's
--------

No known gotcha's at this time. If you find a problem: please report it on
http://tracker.firebirdsql.org/brows/JDBC

Jaybird 4.0.x changelog
=======================

...

What's new in Jaybird 4
=======================

For a full list of changes, see [Firebird tracker for Jaybird 4](http://tracker.firebirdsql.org/secure/ReleaseNote.jspa?projectId=10002&styleName=Text&version=10441).

Java support
------------

### Java 7 ###

The driver supports Java 7 for now.

Jaybird 4 will very likely drop support for Java 7 (this decision is not final yet).

### Java 8 ###

The driver supports Java 8.

### Java 9 ###

Jaybird currently does not formally support Java 9 (JDBC 4.3), although most of
the JDBC 4.3 features have been implemented (in as far as they are supported by 
Firebird).

For compatibility with Java 9 modules, versions 2.2.14 and 3.0.3 introduced the 
automatic module name `org.firebirdsql.jaybird`. This guarantees a stable module 
name for Jaybird, and allows for future modularization of Jaybird.  

Firebird support
----------------

Support for Firebird 2.0 and 2.1 has been dropped. See [Firebird 2.0 and 2.1 no
longer supported] for details.

Firebird versions 2.5, 3.0 and (upcoming) 4.0 are supported.

JDBC RowId support
------------------

Columns of type `RDB$DBK_KEY` are now identified as `java.sql.Types.ROWID`,
and `getObject` on these columns will now return a `java.sql.RowId`.

The `getObject(int/String, Class)` methods support retrieval as 
`java.sql.RowId` and `org.firebirdsql.jdbc.FBRowId`; the object returned is the
same type (`org.firebirdsql.jdbc.FBRowId`) in both cases.

Updating row ids is not possible, so attempts to call `updateRowId` or 
`updateObject` on a `RDB$DB_KEY` in an updatable result set will throw an 
`SQLFeatureNotSupportedException`.

Unfortunately, this support does not extend to parameters, as parameters (eg in
`where RDB$DB_KEY = ?`) cannot be distinguished from parameters of a normal 
binary field (`char character set octets`). To address this, binary fields
now also accept values of type `java.sql.RowId` on `setRowId` and `setObject`.

Support has also been added to `DatabaseMetaData`:

-   `getBestRowIdentifier` returns `RDB$DB_KEY` if there is no primary key (existing
    functionality)
-   `getRowIdLifetime` now returns `RowIdLifetime.ROWID_VALID_TRANSACTION` (even
    if `dbkey_scope=1` has been specified!)
-   `getPseudoColumns` now returns `RDB$DB_KEY`

Other database metadata (eg `getColumns`) will **not** list the `RDB$DB_KEY` 
column, as it is a pseudo-column.

Wire encryption support
-----------------------

Jaybird 4 adds support for the Firebird 3 ARC4 wire encryption. The encryption
is configured using the connection property `wireCrypt`, with the following
(case-insensitive) values:

 -  `DEFAULT`: default (value used when `wireCrypt` is not specified; you'd 
    normally not specify this explicitly)
 -  `ENABLED`: enable, but not require, wire encryption
 -  `REQUIRED`: require wire encryption (only if Firebird version is 3.0 or higher)
 -  `DISABLED`: disable wire encryption 
 
The default value acts as `ENABLED` for pure Java connections, for JNA (native) 
connections this wil use the fbclient default (either `Enabled` or the 
configured value of `WireCrypt` from a `firebird.conf` read by the native 
library).

Connection property `wireCrypt=REQUIRED` will **not** reject unencrypted 
connections when connecting to Firebird 2.5 or lower. This behavior matches the 
Firebird 3 client library behavior. The value will also be ignored when using
native connections with a Firebird 2.5 client library.

Using `wireCrypt=DISABLED` when Firebird 3 or higher uses setting 
`WireCrypt = Required` (or vice versa) will yield error _"Incompatible wire 
encryption levels requested on client and server"_ (error: 
_isc_wirecrypt_incompatible / 335545064_).

The same error is raised when connecting to Firebird 3 and higher with a legacy
authentication user with connection property `wireCrypt=REQUIRED`. 

Alternative wire encryption plugins are currently not supported, although we 
made some preparations to support this. If you want to develop such a plugin, 
contact us on the Firebird-Java mailing list so we can work out the details of 
adding plugin support.

**WARNING**

The implementation comes with a number of caveats:
 
 -   we cannot guarantee that the session key cannot be obtained by someone with 
     access to your application or the machine hosting your application
     (although that in itself would already imply a severe security breach)
 -   the ARC4 encryption - the default provided by Firebird - is considered to 
     be a weak (maybe even broken) cipher these days
     
Firebird 4 DECFLOAT support
---------------------------

Firebird 4 introduces the SQL:2016 `DECFLOAT` datatype, a decimal floating point 
with a precision of 16 or 34 digits (backed by an IEEE-754 Decimal64 or 
Decimal128). See the Firebird 4 release notes for details on this datatype.

Jaybird 4 adds support for this datatype. The 'default' object type for `DECFLOAT`
is a `java.math.BigDecimal`, but conversion from and to the following datatypes
is supported:

- `java.math.BigDecimal` (see note 1)
- `byte` (valid range -128 to 127(!); see notes 2, 3)
- `short` (valid range -32768 to 32767; see note 3)
- `int` (valid range -2<sup>31</sup> to 2<sup>31</sup>-1; see note 3)
- `long` (valid range -2<sup>63</sup> to 2<sup>63</sup>-1; see notes 3, 4)
- `float` (valid range -1 * Float.MAX_VALUE to Float.MAX_VALUE; see notes 5-9)
- `double` (valid range -1 * Float.MAX_VALUE to Float.MAX_VALUE; see notes 6-9)
- `boolean` (see notes 10, 11)
- `java.lang.String` (see notes 12-14)
- `java.math.BigInteger` (see notes 15, 16)
- `org.firebirdsql.extern.decimal.Decimal32/64/128` (see notes 17, 18)

The `DECFLOAT` type is not yet defined in the JDBC specification, for the time
being, we have defined a Jaybird specific type code with value `-6001`. This
value is available through constant `org.firebirdsql.jdbc.JaybirdTypeCodes.DECFLOAT`,
or - for JDBC 4.2 and higher - `org.firebirdsql.jdbc.JaybirdType.DECFLOAT`, which
is an enum implementing `java.sql.SQLType`.

If you need to use the type code, we suggest you use these constants. If a 
`DECFLOAT` type constant gets added to the JDBC standard, we will update the
value. The enum value will be deprecated when that version of JDBC has been
released.

Jaybird uses a local copy of the [FirebirdSQL/decimal-java](https://github.com/FirebirdSQL/decimal-java)
library, with a custom package `org.firebirdsql.extern.decimal`. This to avoid 
additional dependencies. 

### Precision and range ###

The `DECFLOAT` datatype supports values with a precision of 16 or 34 decimal 
digits, and a scale - as used in `java.math.BigDecimal` - between -369 and 398 
(`DECFLOAT(16)`) or between -6111 and 6176 (`DECFLOAT(16)`), so the minimum and 
maximum values are:

| Type           | Min/max value               | Smallest (non-zero) value   |
|----------------|-----------------------------|-----------------------------|
| `DECFLOAT(16)` | +/-9.9..9E+384 (16 digits)  | +/-1E-398 (1 digit)         | 
| `DECFLOAT(34)` | +/-9.9..9E+6144 (34 digits) | +/-1E-6176 (1 digit)        |

When converting values from Java types to `DECFLOAT` and retrieving
`DECFLOAT` values as `Decimal32` or `Decimal64`, the following rules are 
applied:

-   Zero values can have a non-zero exponent, and if the exponent is out of 
range, the exponent value is 'clamped' to the minimum or maximum exponent
supported. This behavior is subject to change, and future release may
'round' to exact `0` (or `0E0`)

-   Values with a precision larger than the target precision are rounded to the 
target precision using `RoundingMode.HALF_EVEN`

-   If the magnitude (or exponent) is too low (or in `BigDecimal` terms, the scale 
too high), then the following steps are applied:
 
    1. Precision is reduced applying `RoundingMode.HALF_EVEN`, increasing the
    exponent by the reduction of precision.
    
    An example: a `DECFLOAT(16)` stores values as an integral coefficient of 16 
    digits and an exponent between `-398` and `+369`. The value 
    `1.234567890123456E-394` or `1234567890123456E-409` is coefficient 
    `1234567890123456` and exponent `-409`. The coefficient is 16 digits, but
    the exponent is too low by 11.
    
    If we sacrifice least-significant digits, we can increase the exponent,
    this is achieved by dividing the coefficient by 10<sup>11</sup> (and 
    rounding) and increasing the exponent by 11. We get 
    exponent = round(1234567890123456 / 10<sup>11</sup>) = 12346 and 
    exponent = -409 + 11 = -398.
    
    The resulting value is now `12346E-398` or `1.2346E-394`, or in other words, 
    we sacrificed precision to make the value fit.
    
    2. If after the previous step, the magnitude is still too low, we have what
    is called an underflow, and the value is truncated to 0 with the minimum 
    exponent and preserving sign, eg for `DECFLOAT(16)`, the value will become 
    +0E+398 or -0E-398 (see note 19). Technically, this is just a special case 
    of the previous step.
    
-   If the magnitude (or exponent) is too high (or in `BigDecimal` terms, the 
scale too low), then the following steps are applied:

    1. If the precision is less than maximum precision, and the difference 
    between maximum precision and actual precision is larger than or equal to 
    the difference between the actual exponent and the maximum exponent, then
    the precision is increased by adding zeroes as least-significant digits
    and decreasing the exponent by the number of zeroes added.
    
    An example: a `DECFLOAT(16)` stores values as an integral coefficient of 16 
    digits and an exponent between `-398` and `+369`. The value `1E+384` is 
    coefficient `1` with exponent `384`. This is too large for the maximum 
    exponent, however, we have a value with a single digit, leaving us with
    15 'unused' most-significant digits. 
    
    If we multiply the coefficient by 10<sup>15</sup> and subtract 15 from the 
    exponent we get: coefficient = 1 * 10<sup>15</sup> = 1000000000000000 and
    exponent = 384 - 15 = 369. And these values for coefficient and exponent
    are in range of the storage requirements.
    
    The resulting value is now `1000000000000000E+369` or `1.000000000000000E+384`,
    or in other words, we 'increased' precision by adding zeroes as 
    least-significant digits to make the value fit.
    
    2. Otherwise, we have what is called an overflow, and an `SQLException` is 
    thrown as the value is out of range.
    
If you need other rounding and overflow behavior, make sure you round the values
appropriately before you set them.
     
### Notes ###

1.  `java.math.BigDecimal` is capable of representing numbers with larger 
precisions than `DECFLOAT`, and numbers that are out of range (too large or too 
small). When performing calculations in Java, use `MathContext.DECIMAL64` (for 
`DECFLOAT(16)`) or `MathContext.DECIMAL128` (for `DECFLOAT(34)`) to achieve 
similar results in calculations as in Firebird. Be aware there might still be 
differences in rounding, and the result of calculations may be out of range.

    a.  Firebird 4 snapshots currently allow storing NaN and Infinity values,
    retrieval of these values will result in a `SQLException`, with a 
    `DecimalInconvertibleException` cause with details on the special. The 
    support for these special values is currently under discussion and
    may be removed in future Firebird 4 snapshots.

2.  `byte` in Java is signed, and historically Jaybird has preserved sign when
storing byte values, and it considers values outside -128 and +127 out of range.

3.  All integral values are - if within range - first converted to `long` 
using `BigDecimal.longValue`, which discards any fractional parts (rounding by
truncation).

4.  When storing a `long` in `DECFLOAT(16)`, rounding will be applied using
`RoundingMode.HALF_EVEN` for values larger than `9999999999999999L` or smaller 
than `-9999999999999999L`.

5.  `float` values are first converted to (or from) double, this may lead to 
small rounding differences

6.  `float` and `double` can be fully stored in `DECFLOAT(16)` and 
`DECLOAT(34)`, with minor rounding differences.
   
7.  When reading `DECFLOAT` values as `double` or `float`, rounding will be 
applied as binary floating point types are inexact, and have a smaller 
precision.
 
8.  If the magnitude of the `DECFLOAT` value is too great to be represented in 
`float` or `double`, +Infinity or -Infinity may be returned (see 
`BigDecimal.doubleValue()`). This behavior is subject to change, future releases 
may throw a `SQLException` instead, see also related note 8.
 
9.  Storing and retrieving values NaN, +Infinity and -Infinity are currently 
supported, but this may change as this doesn't seem to be allowed by the 
SQL:2016 standard.
  
    It is possible that Jaybird or Firebird will disallow storing and retrieving 
NaN and Infinity values in future releases, causing Jaybird to throw an 
`SQLException` instead. We strongly suggest not to rely on this support for
special values.

    a.  Firebird `DECFLOAT` currently discerns four different NaNs (+/-NaN and 
    +/-signaling-NaN). These are all mapped to `Double.NaN` (or `Float.NaN`),
    Java NaN values are mapped to +NaN in Firebird.

10. Setting `boolean` values will set `0` (or `0E+0`) for `false` and `1` (or 
`1E+0`) for `true`.

11. Retrieving as `boolean` will return `true` for `1` (exactly `1E+0`) and
`false` for **all other values**. Be aware that this means that `1.0E+0` (or 
`10E-1`) etc will be **`false`** (_this may change before Jaybird 4 final to 
`getLong() == 1L` or similar, which truncates the value_). 

    This behavior may change in the future and only allow `0` for `false` and 
exactly `1` for `true` and throw an `SQLException` for all other values, or 
maybe `true` for everything other than `0`. In general we advise to not use 
numerical types for boolean values, and especially not to retrieve the result of 
a calculation as a boolean value. Instead, use a real `BOOLEAN`.

12. Setting values as `String` is supported following the format rules of 
`new BigDecimal(String)`, with extra support for special values `+NaN`, `-NaN`, 
`+sNaN`, `-sNaN`, `+Infinity` and `-Infinity` (case insensitive). Other non-numerical 
strings throw an `SQLException`. Out of range values are handled as described in 
[Precision and range].

13. Getting values as `String` will equivalent to `BigDecimal.toString`, with
extra support for the special values mentioned in the previous note.

14. As mentioned in earlier notes, support for the special values is under
discussion, and may be removed in the final Jaybird 4 or Firebird 4 release,
or might change in future versions.

15. Getting as `BigInteger` will behave as `BigDecimal.toBigInteger()`, which
discards the fractional part (rounding by truncation), and may add 
`(-1 * scale - precision)` least-significant zeroes if the scale exceeds
precision. Be aware that use of `BigInteger` for large values may result in 
significant memory consumption. 

16. Setting as `BigInteger` may lose precision, as it applies the rules 
described in [Precision and range].

17. Values can also be set and retrieved as types `Decimal32`, `Decimal64` and 
`Decimal128` from the `org.firebirdsql.extern.decimal` package. Where `Decimal64`
exactly matches the `DECFLOAT(16)` protocol format, and `Decimal128` the 
`DECFLOAT(34)` protocol format. Be aware that this is an implementation detail
that might change in future Jaybird versions (both in terms of support for these 
types, and in terms of the interface (API) of these types).

18. Setting a `Decimal128` on a `DECFLOAT(16)`, or a `Decimal32` on a 
`DECFLOAT(16)` or `DECFLOAT(34)`, or retrieving a `Decimal32` from
a `DECFLOAT(16)` or `DECFLOAT(34)`, or a `Decimal64` from a `DECFLOAT(34)`
will apply the rules described in [Precision and range].

19. Zero values can have a sign (eg `-0` vs `0` (`+0`)), this can only be 
set or retrieved using `String` or the `DecimalXX` types, or the result of 
rounding. This behaviour is subject to change, and future releases may 'round' 
to `0` (aka `+0`).

Potentially breaking changes
----------------------------

Jaybird 4 contains a number of changes that might break existing applications.

See also [Compatibility changes] for details.

Other fixes and changes
-----------------------

...

Removal of deprecated classes and packages
------------------------------------------

See [Removal of deprecated classes, packages and methods] in 
[Compatibility changes] for more details.

Known Issues
============

-   Using a native connection with a Firebird 3 client library to a Firebird 2.5
    or older server may be slow to connect. The workaround is to specify the
    IPv4 address instead of the host name in the connection string, or to use a
    Firebird 2.5 or earlier `fbclient.dll`.
    
    This is caused by [CORE-4658](http://tracker.firebirdsql.org/browse/CORE-4658)

Compatibility changes
=====================

Jaybird 4 introduces some changes in compatibility and announces future
breaking changes.

**The list might not be complete, if you notice a difference in behavior that is
not listed, please report it as bug.** It might have been a change we forgot to
document, but it could just as well be an implementation bug.

Firebird 2.0 and 2.1 no longer supported
----------------------------------------

Support for Firebird 2.0 and 2.1 has been dropped in Jaybird 4. In general we
expect the driver to remain functional, but chances are certain metadata (eg 
`DatabaseMetaData`) will break if we use features introduced in newer versions.

In general we will no longer fix issues that only occur with Firebird 2.1 or
earlier.

RDB$DB_KEY columns no longer of Types.BINARY
--------------------------------------------

With the introduction of [JDBC RowId support], `RDB$DB_KEY` columns are no 
longer identified as `java.sql.Types.BINARY`, but as `java.sql.Types.ROWID`.
The column will behave in a backwards-compatible manner as a binary field, with
the exception of `getObject`, which will return a `java.sql.RowId` instead.

Unfortunately this does not apply to parameters, see also [JDBC RowId support].

Due to the method of identification, real columns of type `char character set 
octets` with the name `DB_KEY` will also be identified as a `ROWID` column.

Removal of character mapping
----------------------------

Character mapping has been removed. Character mapping (also known as
translation) was a feature that allowed to remap certain characters when 
encoding/decoding strings. This could be used to address incompatibilities or
incorrect mappings in the character set implementation of certain platforms 
(apparently HPUX was affected).

If you do need this feature, then you will need to use a custom encoding 
implementation.

Connection property `useTranslation` (and it's alias `mapping_path`) will no 
longer be available. As part of this change the following parts of the 
implementation have been removed (note that most are internal to Jaybird):

-   `org.firebirdsql.encodings.CharacterTranslator` will be removed entirely
-   `DatatypeCoder#encodeString(String value, String javaEncoding, String mappingPath)`
-   `DatatypeCoder#encodeString(String value, Encoding encoding, String mappingPath)`
-   `DatatypeCoder#decodeString(byte[] value, String javaEncoding, String mappingPath)`
-   `DatatypeCoder#decodeString(byte[] value, Encoding encoding, String mappingPath)`
-   `Encoding#withTranslation(CharacterTranslator translator)`
-   `EncodingFactory#getEncoding(String encoding, String mappingPath)`
-   `EncodingFactory#getEncoding(Charset charset, String mappingPath)`
-   `FirebirdConnectionProperties#setUseTranslation(String translationPath)` (and on data sources)
-   `FirebirdConnectionProperties#getUseTranslation` (and on data sources)
-   `IEncodingFactory#getCharacterTranslator(String mappingPath)`
    
Removal of deprecated classes, packages and methods
---------------------------------------------------

The following connection properties (and equivalent data source properties) have
been removed:

-   `useTranslation`: See previous item
-   `octetsAsBytes`: Since Jaybird 3 octets is always handled as `BINARY`
-   `noResultSetTracking`: Option does nothing since Jaybird 3
-   `paranoia_mode`: Option does nothing since Jaybird 2.2 (maybe earlier)

The following deprecated methods have been removed in Jaybird 4:

-   `GDSHelper.iscVaxInteger(byte[] buffer, int pos, int length)` use
    `VaxEncoding.iscVaxInteger(byte[] buffer, int startPosition, int length)`
    instead.
-   `GDSHelper.iscVaxLong(byte[] buffer, int pos, int length)` use
    `VaxEncoding.iscVaxLong(byte[] buffer, int startPosition, int length)`
    instead.
-   `MaintenanceManager.commitTransaction(int transactionId)`, use
    `MaintenanceManager.commitTransaction(long transactionId)` instead.
-   `MaintenanceManager.rollbackTransaction(int transactionId)`, use
    `MaintenanceManager.rollbackTransaction(long transactionId)` instead.
-   `ServiceRequestBufferImp#ServiceRequestBufferImp()`
-   `ServiceRequestBufferImp#ServiceRequestBufferImp(int taskIdentifier)`
-   `FBBlob#copyCharacterStream(Reader reader, long length, String encoding)`
-   `FBBlob#copyCharacterStream(Reader reader, String encoding)` 
-   See also [Removal of character mapping] for a number of removed methods
    
The following classes have been removed in Jaybird 4:

-   `org.firebirdsql.gds.ExceptionListener`, use `org.firebirdsql.gds.ng.listeners.ExceptionListener`
-   `org.firebirdsql.pool.FBSimpleDataSource`, use `org.firebirdsql.ds.FBSimpleDataSource` 

### Removal of deprecated constants ###

The following constants have been removed in Jaybird 4:

-   All `SQL_STATE_*` constants in `FBSQLException`,
    `FBResourceTransactionException`, `FBResourceException`, and
    `FBDriverNotCapableException` will be removed. Use equivalent constants in
    `org.firebirdsql.jdbc.SQLStateConstants`.

Breaking changes for Jaybird 4
------------------------------

*TODO: Section to be removed*

With Jaybird 4 the following breaking changes will be introduced.

### Dropping support for Java 7 ###

Jaybird 4 will very likely drop support for Java 7 (this decision is not final yet).

Breaking changes for Jaybird 5
------------------------------

With Jaybird 5 the following breaking changes will be introduced.

### Removal of deprecated methods ###

The following methods will be removed in Jaybird 5:

-   `MaintenanceManager.listLimboTransactions()`, use
    `MaintenanceManager.limboTransactionsAsList()` or 
    `MaintenanceManager.getLimboTransactions()` instead.
-   `TraceManager.loadConfigurationFromFile(String)`, use standard Java 
    functionality like `new String(Files.readAllBytes(Paths.get(fileName)), <charset>)`