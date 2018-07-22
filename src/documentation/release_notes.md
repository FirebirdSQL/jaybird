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

Jaybird @VERSION@ was tested against Firebird 2.5.7, 3.0.2, and a recent 
Firebird 4 snapshot build, but should also support other Firebird versions from 
2.5 and up.

Formal support for Firebird 2.0 and 2.1 has been dropped (although in general we 
expect the driver to work). The Type 2 and embedded server JDBC drivers use JNA to
access the Firebird client or embedded library.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

### Notes on Firebird 3 support

Jaybird 4 does not (yet) support the Firebird 3 zlib compression.

Supported Java versions
-----------------------

Jaybird 4 supports Java 7 (JDBC 4.1), Java 8 (JDBC 4.2), and Java 9 - 10 (JDBC 
4.3). Support for earlier Java versions has been dropped.

For the time being, there will be no Java 9+ specific builds, the Java 8 builds 
have the same source and all JDBC 4.3 related functionality.

Given the limited support period for Java 9 and higher versions, we may limit
support on those versions.

Jaybird 4 is not modularized, but all versions declare the automatic module name 
`org.firebirdsql.jaybird`.

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

Getting Jaybird 4
=================

Jaybird @VERSION@
-------------------

### Maven ###

Jaybird @VERSION@ is available from Maven central: 

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird-jdkXX` (where `XX` is `17` or `18`).\
Version: `@VERSION@`

NOTE: SNAPSHOT releases are only available from the Sonatype snapshot 
repository, <https://oss.sonatype.org/content/repositories/snapshots>

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
explicitly include JNA as a dependency, as we only depend on it as an
optional dependency:

~~~ {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
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

During tests we have have observed that using Jaybird 4 with Firebird 4 may
cause connection hangs when the connection is encrypted (the connection is 
blocked in a read from the socket). The cause seems related to the Java 
version (the problem disappeared with Java 9 Update 4), or possibly the 
`TcpRemoteBufferSize` setting in Firebird. The workaround is to disable 
wire encryption in Firebird or for the specific connection (see 
[Wire encryption support]).

If you find a problem while upgrading, or other bugs: please report it 
on <http://tracker.firebirdsql.org/brows/JDBC>.

For known issues, consult [Known Issues].

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

Jaybird currently does not fully support Java 9 and higher (JDBC 4.3), although 
most of the JDBC 4.3 features have been implemented (in as far as they are 
supported by Firebird).

For compatibility with Java 9 modules, versions 2.2.14 and 3.0.3 introduced the 
automatic module name `org.firebirdsql.jaybird`. This guarantees a stable module 
name for Jaybird, and allows for future modularization of Jaybird.

You can use the Java 8 driver under Java 9+. We recommend to only use the Java 8 
version of Jaybird with Java 9+, and not use the Java 7 version of Jaybird. The 
Java 7 version doesn't implement all of the JDBC 4.3 features that are 
implemented in the Java 8 version. In addition, since Jaybird 3.0.4, the Java 7 
version of Jaybird needs the `java.xml.bind` module, where the Java 8 version 
doesn't need that module.   

Firebird support
----------------

Support for Firebird 2.0 and 2.1 has been dropped. See [Firebird 2.0 and 2.1 no
longer supported] for details.

Firebird versions 2.5, 3.0 and (upcoming) 4.0 are supported.

JDBC RowId support
------------------

Columns of type `RDB$DB_KEY` are now identified as `java.sql.Types.ROWID`,
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

Jaybird 4 adds support for the Firebird 3 ARC4 wire encryption. This feature has
been back-ported to Jaybird 3.0.4. The encryption is configured using the 
connection property `wireCrypt`, with the following (case-insensitive) values:

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
     
Database encryption support
---------------------------

Jaybird 4 (and 3.0.4) adds support for Firebird 3 database encryption callbacks 
in the pure Java implementation of the version 13 protocol. This feature was 
sponsored by IBPhoenix.

The current implementation is simple and only supports replying with a static 
value from a connection property. Be aware that a static value response for 
database encryption is not very secure as it can easily lead to replay attacks 
or unintended key exposure. 

Future versions of Jaybird (likely 5, maybe 4) will introduce plugin support for 
database encryption plugins that require a more complex callback.

The static response value of the encryption callback can be set through the 
`dbCryptConfig` connection property. `DataSource` and `ServiceManager` 
implementations have an equivalent property with the same name. This 
property can be set as follows:

-   Absent or empty value: empty response to callback (depending on the database 
    encryption plugin this may just work or yield an error later)
-   Strings prefixed with `base64:`: rest of the string is decoded as base64 to 
    bytes. The `=` padding characters are optional, but when present they must
    be valid (that is: if you use padding, you must use the right number of 
    padding characters for the length)
-   Plain string value: string is encoded to bytes using UTF-8, and these bytes
    are used as the response
    
Because of the limitation of connection URL parsing, we strongly suggest to
avoid plain string values with `&` or `;`. Likewise, avoid `:` so that we can
support other prefixes similar to `base64:` in the future. If you need these 
characters, consider using a base64 encoded value instead.

For service operations, as implemented in the `org.firebirdsql.management` 
package, Firebird requires the `KeyHolderPlugin` configuration to be globally 
defined in `firebird.conf`. Database-specific configuration in `databases.conf` 
will be ignored for service operations. Be aware that some service operations on 
encrypted databases are not supported by Firebird 3 (eg `gstat` equivalents 
other than `gstat -h` or `gstat -e`).

Other warnings and limitations

-   Database encryption callback support is only available in the pure Java
    implementation. Support for native and embedded connections will be added
    in a future version.
-   The database encryption callback does not require an encrypted connection, 
    so the key can be exchanged unencrypted if wire protocol encryption has been 
    disabled client-side or server-side, or if legacy authentication is used. 
    Consider setting connection property `wireCrypt=REQUIRED` to force 
    encryption (caveat: see the next point).
-   Firebird may ask for the database encryption key before the connection has
    been encrypted (for example if the encrypted database itself is used as the
    security database). _This applies to v15 protocol support, which is not yet
    available._
-   The improvements of the versions 14 and 15 wire protocol are not
    implemented, and as a result encrypted security databases (external or 
    security database hosted in the database itself) will not work unless the
    encryption plugin does not require a callback. Support for the version 15 
    wire protocol will be added in a future version.
-   We cannot guarantee that the `dbCryptConfig` value cannot be obtained by 
    someone with access to your application or the machine hosting your 
    application (although that in itself would already imply a severe security 
    breach).
    
Authentication plugin improvements
----------------------------------

Jaybird 4 has added support for the new `SrpNNN` (with NNN is 224, 256, 384 
and 512) authentication plugins added in Firebird 4 (backported to Firebird 
3.0.4).

The original `Srp` plugin uses SHA-1, the new Srp-variants use SHA-224, SHA-256,
SHA-384 and SHA-512 respectively[^srpHash].

[^srpHash]: Internally `SrpNNN` continues to uses SHA-1, only the client-proof 
applies the SHA-NNN hash. See also [CORE-5788](http://tracker.firebirdsql.org/browse/CORE-5788)).

Be aware, support for these plugins depends on support of these hash algorithms 
in the JVM. For example, SHA-224 is not supported in Oracle Java 7 by default 
and may require additional JCE libraries.

### Default authentication plugins ###

The default plugins applied by Jaybird are now - in order - `Srp256`, `Srp`. 
This applies only for the pure Java protocol and only when connecting to 
Firebird 3 or higher. The native implementation will use its own default or the 
value configured through its `firebird.conf`. 

When connecting to Firebird 3 versions earlier than 3.0.4, or if `Srp256` has 
been removed from the `AuthServer` setting in Firebird, this might result in 
slower authentication because more roundtrips to the server are needed. After 
the attempt to use `Srp256` fails, authentication continues with `Srp`.

To avoid this, consider explicitly configuring the authentication plugins to 
use, see [Configure authentication plugins] for details.

When connecting to Firebird 3 or higher, the pure Java protocol in Jaybird will 
no longer try the `Legacy_Auth` plugin by default as it is an unsafe 
authentication mechanism. We strongly suggest to use SRP users only, but if you 
really need to use legacy authentication, you can specify connection property 
`authPlugins=Legacy_Auth`, see [Configure authentication plugins] for details.

Firebird 2.5 and earlier are not affected and will always use legacy 
authentication.

### Configure authentication plugins ###

Jaybird 4 introduces the connection property `authPlugins` (alias 
`auth_plugin_list`) to specify the authentication plugins to try when 
connecting. The value of this property is a comma-separated[^authPluginSeparator] 
list with the plugin names.

[^authPluginSeparator]: The `authPlugins` values can be separated by comma, 
space, tab, or semi-colon. The semi-colon should not be used in a JDBC URL as 
there the semi-colon is a separator between connection properties.

Unknown or unsupported plugins will be logged and skipped. When no known plugins
are specified, Jaybird will throw an exception with:

-   For pure Java

    _Cannot authenticate. No known authentication plugins, requested plugins: \[&lt;plugin-names&gt;] \[SQLState:28000, ISC error code:337248287]_

-   For native

    _Error occurred during login, please check server firebird.log for details \[SQLState:08006, ISC error code:335545106]_

The `authPlugins` property only affects connecting to Firebird 3 or later. It 
will be ignored when connecting to Firebird 2.5 or earlier. The setting will
also be ignored for native connections when using a fbclient library of 
version 2.5 or earlier.

Examples:

-   JDBC URL to connect using `Srp256` only:

        jdbc:firebirdsql://localhost/employee?authPlugins=Srp256
        
-   JDBC URL to connect using `Legacy_Auth` only (this is unsafe!)

        jdbc:firebirdsql://localhost/employee?authPlugins=Legacy_Auth

-   JDBC URL to try `Legacy_Auth` before `Srp512` (this order is unsafe!)

        jdbc:firebirdsql://localhost/employee?authPlugins=Legacy_Auth,Srp512
        
The property is also supported by the data sources, service managers and event 
manager.

### External authentication plugin support (experimental) ###

If you develop your own Firebird authentication plugin (or use a third-party 
authentication plugin), it is possible - for pure Java only - to add your own 
authentication plugin by implementing the interfaces 
 
-   `org.firebirdsql.gds.ng.wire.auth.AuthenticationPluginSpi`
-   `org.firebirdsql.gds.ng.wire.auth.AuthenticationPlugin`

The SPI implementation needs to be listed in `META-INF/services/org.firebirdsql.gds.ng.wire.auth.AuthenticationPluginSpi`
in your jar.

This support is experimental and comes with a number of caveats:

-   We haven't tested this extensively (except for loading Jaybird's own 
    plugins internally)
-   The authentication plugin (and provider) interfaces should be considered 
    unstable; they may change with point-releases (although we will try to avoid 
    that) 
-   For now it will be necessary for the jar containing the authentication 
    plugin to be loaded by the same class loader as Jaybird itself

If you implement a custom authentication plugin and run into problems, contact 
us on the Firebird-Java mailing list.

If you use a native connection, check the Firebird documentation how to add
third-party authentication plugins to fbclient.

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
- `double` (valid range -1 * Double.MAX_VALUE to Double.MAX_VALUE; see notes 6-9)
- `boolean` (see notes 10, 11)
- `java.lang.String` (see notes 12-14)
- `java.math.BigInteger` (see notes 15, 16)
- `org.firebirdsql.extern.decimal.Decimal32/64/128` (see notes 17, 18)

The `DECFLOAT` type is not yet defined in the JDBC specification. For the time
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
digits, and an exponent [^decimalFormat] between -398 and 369 (`DECFLOAT(16)`), or 
between -6176 and 6111 (`DECFLOAT(34)`), so the minimum and maximum values are:

[^decimalFormat]: The `DECFLOAT` decimal format stores values as sign, integral 
number with 16 or 34 digits, and an exponent. This is similar to  
`java.math.BigDecimal`, but instead of an exponent, that uses the concept `scale`, 
where `scale = -1 * exponent`.

| Type           | Min/max value               | Smallest (non-zero) value   |
|----------------|-----------------------------|-----------------------------|
| `DECFLOAT(16)` | +/-9.9..9E+384 (16 digits)  | +/-1E-398 (1 digit)         | 
| `DECFLOAT(34)` | +/-9.9..9E+6144 (34 digits) | +/-1E-6176 (1 digit)        |

When converting values from Java types to `DECFLOAT` and retrieving
`DECFLOAT` values as `Decimal32` or `Decimal64`, the following rules are 
applied:

-   Zero values can have a non-zero exponent, and if the exponent is out of 
range, the exponent value is 'clamped' to the minimum or maximum exponent
supported. This behavior is subject to change, and future releases may
'round' to exact `0` (or `0E0`)

-   Values with a precision larger than the target precision are rounded to the 
target precision using `RoundingMode.HALF_EVEN`

-   If the magnitude (or exponent) is too low, then the following steps are 
applied:
 
    1.  Precision is reduced applying `RoundingMode.HALF_EVEN`, increasing the
    exponent by the reduction of precision.
    
        An example: a `DECFLOAT(16)` stores values as an integral coefficient of 
    16 digits and an exponent between `-398` and `+369`. The value 
    `1.234567890123456E-394` or `1234567890123456E-409` is coefficient 
    `1234567890123456` and exponent `-409`. The coefficient is 16 digits, but
    the exponent is too low by 11.
    
        If we sacrifice least-significant digits, we can increase the exponent,
    this is achieved by dividing the coefficient by 10<sup>11</sup> (and 
    rounding) and increasing the exponent by 11. We get 
    exponent = round(1234567890123456 / 10<sup>11</sup>) = 12346 and 
    exponent = -409 + 11 = -398.
    
        The resulting value is now `12346E-398` or `1.2346E-394`, or in other 
    words, we sacrificed precision to make the value fit.
    
    2.  If after the previous step, the magnitude is still too low, we have what
    is called an underflow, and the value is truncated to 0 with the minimum 
    exponent and preserving sign, eg for `DECFLOAT(16)`, the value will become 
    +0E+398 or -0E-398 (see note 19). Technically, this is just a special case 
    of the previous step.
    
-   If the magnitude (or exponent) is too high, then the following steps are 
applied:

    1.  If the precision is less than maximum precision, and the difference 
    between maximum precision and actual precision is larger than or equal to 
    the difference between the actual exponent and the maximum exponent, then
    the precision is increased by adding zeroes as least-significant digits
    and decreasing the exponent by the number of zeroes added.
    
        An example: a `DECFLOAT(16)` stores values as an integral coefficient of 
    16 digits and an exponent between `-398` and `+369`. The value `1E+384` is 
    coefficient `1` with exponent `384`. This is too large for the maximum 
    exponent, however, we have a value with a single digit, leaving us with
    15 'unused' most-significant digits. 
    
        If we multiply the coefficient by 10<sup>15</sup> and subtract 15 from 
    the exponent we get: coefficient = 1 * 10<sup>15</sup> = 1000000000000000 
    and exponent = 384 - 15 = 369. And these values for coefficient and exponent
    are in range of the storage requirements.
    
        The resulting value is now `1000000000000000E+369` or `1.000000000000000E+384`,
    or in other words, we 'increased' precision by adding zeroes as 
    least-significant digits to make the value fit.
    
    2.  Otherwise, we have what is called an overflow, and an `SQLException` is 
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
`+sNaN`, `-sNaN`, `+Infinity` and `-Infinity` (case insensitive). Other 
non-numerical strings throw an `SQLException` with a `NumberFormatException` as 
cause. Out of range values are handled as described in [Precision and range].

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

16. Setting as `BigInteger` will lose precision for values with more digits than
the target type. It applies the rules described in [Precision and range].

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

Firebird 4 extended numeric precision support
---------------------------------------------

Added support for the extended precision for `NUMERIC` and `DECIMAL` introduced 
in Firebird 4, increasing the maximum precision to 34. Technically, this
extended precision is supported by a IEEE-754 Decimal128 which is also used for
`DECFLOAT` support.

Any `NUMERIC` or `DECIMAL` with a precision between 19 and 34 will allow storage
up to a precision of 34. 

Values set on a field or parameter will be rounded using `RoundingMode.HALF_EVEN` 
to the target scale of the field. Values exceeding a precision of 34 will be
rejected with a `TypeConversionException`. 

Improved JDBC function escape support
-------------------------------------

Revised support for JDBC function escapes with optional parameters, and added 
support for a number of previously unsupported functions or options.

If you only target Firebird, then we suggest you do not use JDBC function 
escapes.

### New JDBC function escapes ###

-   `DEGREES(number)` - Degrees in _number_ radians; implemented as 
`((number)*180.0/PI())`
-   `RADIANS(number)` - Radians in _number_ degrees; implemented as 
`((number)*PI()/180.0)`
-   `QUARTER(date)` - Quarter of year for date; implemented as 
`(1+(EXTRACT(MONTH FROM date)-1)/3)`
-   `TIMESTAMPADD(interval, count, timestamp)` - Implemented using `DATEADD` 
with the following caveats:

    -   _interval_ `SQL_TSI_FRAC_SECOND` unit is nanoseconds and will be 
    simulated by using `MILLISECOND` and the count multiplied by `1.0e-6` to 
    convert the value to milliseconds.
    -   _interval_ `SQL_TSI_QUARTER` will be simulated by using `MONTH` and 
    _count_ multiplied by 3.
    -   _interval_ values that are not specified in JDBC will be passed as is, 
    resulting in an error from the Firebird engine if it is an invalid interval 
    name for `DATEADD`.
-   `TIMESTAMPDIFF(interval, timestamp1, timestamp2)` - Implemented using 
`DATEDIFF` with the following caveats:

    -   _interval_  `SQL_TSI_FRAC_SECOND` unit is nanoseconds and will be 
    simulated by using `MILLISECOND` and the result multiplied by `1.0e6` and 
    cast to `BIGINT` to convert the value to nanoseconds.
    -   Value `SQL_TSI_QUARTER` will be simulated by using `MONTH` and the 
    result divided by 3.
    -   Contrary to specified in the JDBC specification, the resulting value 
    will be `BIGINT`, not `INTEGER`.
    -   _interval_ values that are not specified in JDBC will be passed as is, 
    resulting in an error from the Firebird engine if it is an invalid interval 
    name for `DATEDIFF`.

### Improved JDBC function escapes ###

-   `CHAR_LENGTH(string[, CHARACTERS|OCTETS])` - The optional second parameter 
with `CHARACTERS` or `OCTETS` is now supported.
    
    Absence of second parameter, or `CHARACTERS` maps to `CHAR_LENGTH`, `OCTETS` maps to
`OCTET_LENGTH`
-   `CHARACTER_LENGTH(string[, CHARACTERS|OCTETS])` - see `CHAR_LENGTH`
-   `CONCAT(string1, string2)` - Added parentheses around expression to prevent 
ambiguity or incorrect evaluation order.
-  `LENGTH(string[, CHARACTERS|OCTETS])` - The optional second parameter with 
`CHARACTERS` or `OCTETS` is now supported.

    The JDBC specification specifies _Number of characters in string, excluding 
trailing blanks_, we right-trim (`TRIM(TRAILING FROM value)`) the string before 
passing the value to either `CHAR_LENGTH` or `OCTETS_LENGTH`. As a result, the 
interpretation of what is a blank depends on the type of _value_. Is the value a 
normal `(VAR)CHAR` (non-octets), then the blank is space (0x20), for a 
`VAR(CHAR)CHARACTER SET OCTETS / (VAR)BINARY` the blank is NUL (0x00). This means 
that the optional `CHARACTERS|OCTETS` parameter has no influence on which blanks 
are trimmed, but only whether we count characters or bytes after trimming.
-   `LOCATE(string1, string2[, start])` - The third parameter _start_ is now 
optional.
-   `POSITION(substring IN string[, CHARACTERS|OCTETS])` - The optional second 
parameter is now supported if `CHARACTERS`. `OCTETS` is not supported.
-   `CONVERT(value, SQLtype)` - See [Improved CONVERT support].

### Improved CONVERT support ###

In Jaybird 3, `CONVERT(value, SQLtype)` would map directly to 
`CAST(value as SQLtype)`, we have improved support to better conform to the JDBC 
requirements, with some caveats:

-   Both the `SQL_<datatype>` and `<datatype>` mapping is now supported
-   Contrary to the specification, we allow explicit length or precision and 
scale parameters
-   `(SQL_)VARCHAR`, `(SQL_)NVARCHAR` (and _value_ not a parameter (`?`)) 
without explicit length is converted using `TRIM(TRAILING FROM value)`, which 
means the result is `VARCHAR` except for blobs where this will result in a blob; 
national character set will be lost. If _value_ is a parameter (`?`), and no 
length is specified, then a length of 50 will be applied (cast to 
`(N)VARCHAR(50)`).
-   `(SQL_)CHAR`, `(SQL_)NCHAR` without explicit length will be cast to 
`(N)CHAR(50)`
-   `(SQL_)BINARY`, and `(SQL_)VARBINARY` without explicit length will be cast 
to `(VAR)CHAR(50) CHARACTER SET OCTETS`. With explicit length, 
`CHARACTER SET OCTETS` is appended.
-   `(SQL_)LONGVARCHAR`, `(SQL_)LONGNVARCHAR`, `(SQL_)CLOB`, `(SQL_)NCLOB` will 
be cast to `BLOB SUB_TYPE TEXT`, national character set will be lost
-   `(SQL_)LONGVARBINARY`, `(SQL_)BLOB` will be cast to `BLOB SUB_TYPE BINARY`
-   `(SQL_)TINYINT` is mapped to `SMALLINT`
-   `(SQL_)ROWID` is not supported as length of `DB_KEY` values depend on the 
context
-   `(SQL_)DECIMAL` and `(SQL_)NUMERIC` without precision and scale are passed
as is, in current Firebird versions, this means the value will be equivalent to
`DECIMAL(9,0)` (which is equivalent to `INTEGER`)
-   Unsupported/unknown _SQLtype_ values (or invalid length or precision and 
scale) are passed as is to cast, resulting in an error from the Firebird engine 
if the resulting cast is invalid

New JDBC protocol prefix jdbc:firebird:
---------------------------------------

Historically, the JDBC protocols supported by Jaybird have used the prefix 
`jdbc:firebirdsql:`. We have now added support for `jdbc:firebird:` as an 
alternative prefix. This prefix was previously only supported in the 
OpenOffice.org/LibreOffice pure Java variant.

Jaybird now supports the following URL prefixes (or JDBC protocols):

-   Pure Java
    -    `jdbc:firebirdsql:`
    -    `jdbc:firebirdsql:java`
    -    `jdbc:firebird:` (new)
    -    `jdbc:firebird:java:` (new)
-   Native
    -    `jdbc:firebirdsql:native:`
    -    `jdbc:firebird:native:` (new)
-   Embedded
    -    `jdbc:firebirdsql:embedded:`
    -    `jdbc:firebird:embedded:` (new)
-   Local
    -    `jdbc:firebirdsql:local:`
    -    `jdbc:firebird:local:` (new)
-   OpenOffice.org/LibreOffice pure Java variant
    -    `jdbc:firebird:oo:`
    -    `jdbc:firebirdsql:oo:`

Potentially breaking changes
----------------------------

Jaybird 4 contains a number of changes that might break existing applications.

See also [Compatibility changes] for details.

Other fixes and changes
-----------------------

-   The distribution zip no longer includes the jaybird-@VERSION@.rar. This file
was an example JCA Resource Archive. 

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

Removed Legacy_Auth from default authentication plugins
-------------------------------------------------------

The pure Java protocol in Jaybird will - by default - no longer try the 
`Legacy_Auth` plugin when connecting to Jaybird 3 or higher.

See [Default authentication plugins] for more information.

RDB$DB_KEY columns no longer of Types.BINARY
--------------------------------------------

With the introduction of [JDBC RowId support], `RDB$DB_KEY` columns are no 
longer identified as `java.sql.Types.BINARY`, but as `java.sql.Types.ROWID`.
The column will behave in a backwards-compatible manner as a binary field, with
the exception of `getObject`, which will return a `java.sql.RowId` instead.

Unfortunately this does not apply to parameters, see also [JDBC RowId support].

Due to the method of identification, real columns of type `char character set octets` 
with the name `DB_KEY` will also be identified as a `ROWID` column.

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

Breaking changes for Jaybird 5
------------------------------

With Jaybird 5 the following breaking changes will be introduced.

### Dropping support for Java 7 ###

Jaybird 5 will drop support for Java 7.

### Dropping support for Java 8 (tentative) ###

Jaybird 5 may drop support for Java 8, depending on the actual release time line.

This decision is not final yet.

### Dropping or restricting JCA (Java Connector Architecture) support ###

Jaybird is currently built around a JCA (Java Connector Architecture) 
implementation. As such, it is both a JDBC driver and a JCA driver. The current
structure requires a dependency on JCA for non-JCA usage.

We are currently considering removing support for JCA entirely, or restructuring 
Jaybird so the dependency on JCA is only needed when Jaybird is used as a JCA 
driver.

Please let us know on Firebird-Java if you use Jaybird as a JCA driver. 

### Removal of deprecated methods ###

The following methods will be removed in Jaybird 5:

-   `MaintenanceManager.listLimboTransactions()`, use
    `MaintenanceManager.limboTransactionsAsList()` or 
    `MaintenanceManager.getLimboTransactions()` instead.
-   `TraceManager.loadConfigurationFromFile(String)`, use standard Java 
    functionality like `new String(Files.readAllBytes(Paths.get(fileName)), <charset>)`
    
### Removal of deprecated constants ###

The following constants will be removed in Jaybird 5:

-   All `SQL_STATE_*` constants in `FBSQLParseException` will be removed. Use equivalent 
    constants in `org.firebirdsql.jdbc.SQLStateConstants`.
    
Compatibility notes
===================

Type 2 (native) and embedded driver
-----------------------------------

Jaybird uses JNA to access the client library. If you want to use the Type 2 
driver, or Firebird embedded, then you need to include `jna-4.4.0.jar` on the 
classpath.

When using Maven, you need to specify the dependency on JNA yourself, as we 
don't depend on it by default (it is specified as an optional dependency):

``` {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
</dependency>
```

The `fbclient.dll`, `fbembed.dll`, `libfbclient.so`, or `libfbembed.so` need to
be on the path, or the location needs to be specified in the system property 
`jna.library.path` (as an absolute or relative path to the directory/directories
containing the library file(s)).

In the future we will move the Type 2 support to a separate library and provide 
JNA-compatible jars that provide the native libraries of a specific Firebird 
version.
