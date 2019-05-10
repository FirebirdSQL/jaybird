WARNING {-}
=======

Jaybird 4 is still in development. This version is provided for testing
purposes only. We'd appreciate your feedback, but we'd like to emphasize that
this version is **not intended for production**.

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the Firebird-java mailing list or reported on the issue tracker
<http://tracker.firebirdsql.org/browse/JDBC>.

Jaybird 4.0.x changelog
=======================

Changes per Jaybird 4 release. See also [What's new in Jaybird 4]. For known
issues, consult [Known Issues].

Jaybird 4.0.0-beta-2
--------------------

The following has been changed or fixed since Jaybird 4.0.0-beta-1

-   Fixed: Connection property `defaultIsolation`/`isolation` did not work
    through `DriverManager`, but only on `DataSource` implementations. ([JDBC-584](http://tracker.firebirdsql.org/browse/JDBC-584))

General Notes
=============

Jaybird is a JDBC driver suite to connect to Firebird database servers from Java
and other Java Virtual Machine (JVM) languages.

About this version
------------------

Jaybird 4 is - compared to Jaybird 3 - an incremental release that builds on the
foundations of Jaybird 3. The focus of this release has been on further 
improving JDBC support and adding support for the new data types and features of 
Firebird 4.

The main new features are:

- [Wire encryption support] (backported to Jaybird 3.0.4)
- [Database encryption support] (backported to Jaybird 3.0.4)
- [Authentication plugin improvements]
- [Firebird 4 DECFLOAT support]
- [Firebird 4 extended numeric precision support]
- [Firebird 4 time zone support]
- [JDBC RowId support]
- [JDBC DatabaseMetaData.getPseudoColumns implemented]
- [JDBC DatabaseMetaData.getVersionColumns implemented]
- [Improved JDBC function escape support]
- [New JDBC protocol prefix jdbc:firebird:]
- [Generated keys support improvements]

Upgrading from Jaybird 3 to 4 should be simple, but please make sure to read 
[Compatibility changes] before using Jaybird 4. See also 
[Upgrading from Jaybird 3 to Jaybird 4].

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the Firebird-java mailing list or reported on the issue tracker
<http://tracker.firebirdsql.org/browse/JDBC>.

Supported Firebird versions
---------------------------

Jaybird @VERSION@ was tested against Firebird 2.5.8, 3.0.4, and a recent 
Firebird 4 snapshot build, but should also support other Firebird versions from 
2.5 and up.

Formal support for Firebird 2.0 and 2.1 has been dropped (although in general we 
expect the driver to work). The Type 2 and embedded server JDBC drivers use JNA to
access the Firebird client or embedded library.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

### Notes on Firebird 3 support

Jaybird 4 does not (yet) support the Firebird 3 zlib compression.

### Notes on Firebird 4 support

Jaybird 4 does not support the protocol improvements of Firebird 4 like statement 
and session timeouts. Nor does it implement the new batch protocol.

Jaybird time zone support uses functionality added after Firebird 4 beta 1 (4.0.0.1436), 
you will need version 4.0.0.1481 or later for the `timeZoneBind` connection 
property.

Supported Java versions
-----------------------

Jaybird 4 supports Java 7 (JDBC 4.1), Java 8 (JDBC 4.2), and Java 9 and higher 
(JDBC 4.3). Support for earlier Java versions has been dropped.

Given the limited support period for Java 9 and higher versions, we will limit
support on those versions to the most recent LTS version and the latest release.
Currently that means we support Java 11 and Java 12.

Jaybird 4 provides libraries for Java 7, Java 8 and Java 11. The Java 8 builds 
have the same source and all JDBC 4.3 related functionality and can be used on
Java 9 and higher as well.

Jaybird 4 is not modularized, but all versions declare the automatic module name 
`org.firebirdsql.jaybird`.

See als [Java support] in [What's new in Jaybird 4].

Specification support
---------------------

Jaybird supports the following specifications:

|Specification|Notes
|-------------|----------------------------------------------------------------
| JDBC 4.3    | All JDBC 4.3 methods for features supported by Firebird; Java 9 and higher supported using the Java 8 driver.
| JDBC 4.2    | All JDBC 4.2 methods for features supported by Firebird.
| JDBC 4.1    | All JDBC 4.1 methods for features supported by Firebird.
| JTA 1.0.1   | Implementation of `javax.transaction.xa.XAResource` interface via `XADataSource` implementation.

Getting Jaybird 4
=================

Jaybird @VERSION@
-------------------

### Maven ###

Jaybird @VERSION@ is available from Maven central: 

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird-XX` (where `XX` is `jdk17`, `jdk18` or `java11`).\
Version: `@VERSION@`

For ease of use, we also provide a Maven relocation artifact with artifact id
`jaybird`. For Jaybird 4 this relocation artifact points to `jaybird-jdk18`.

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

At minimum Jaybird 4 requires `jaybird-XX-@VERSION@.jar` (where `XX` is `jdk17`, 
`jdk18` or `java11`) and `connector-api-1.5.jar`. You can also use 
`jaybird-full-XX-@VERSION@.jar` which includes the connector-api files.

If you deploy your application to a Java EE application server, then you must 
use `jaybird-XX-@VERSION@.jar` (not `-full`!), and **not** include 
`connector-api-1.5.jar` as this dependency will be provided by your application 
server.

For `getGeneratedKeys` support you will need to include 
`antlr-runtime-4.7.2.jar` on your classpath.

For native, local or embedded support, you will need to include `jna-5.3.0.jar` 
on your classpath. See also [Type 2 (native) and embedded driver].

Upgrading from Jaybird 3 to Jaybird 4
=====================================

Please make sure to read [Compatibility changes] and 
[Changes in artifact and library names] before upgrading to Jaybird 4.

Maven
-----

Upgrade the version of the dependency to @VERSION@. If you use native or 
embedded verify that you upgrade JNA (`net.java.dev.jna:jna`) from 4.4.0 to 
5.3.0.

For more detailed instructions, see also the information on Maven in
[Getting Jaybird 4]. 

Manual install
--------------

If you manage your dependencies manually, you need to do the following:

1.  Replace the Jaybird 3 library with the Jaybird 4 version
    - `jaybird-3.0.x.jar` with `jaybird-XX-@VERSION@.jar` (where `XX` is 
      `jdk17`, `jdk18` or `java11`) 
    - `jaybird-full-3.0.x.jar` with `jaybird-full-XX-@VERSION@.jar`
    
2.  If installed, remove `antlr-runtime-4.7.jar` and replace it with 
    `antlr-runtime-4.7.2.jar`. This library is necessary for `getGeneratedKeys`
    support.
    
3.  If installed, remove `jna-4.4.0.jar` and replace it with `jna-5.3.0.jar`.
    This library is only necessary for native, local or embedded connections.
    If you use pure-java connections (the default), you don't need JNA.
    
Gotcha's
--------

During tests we have have observed that using Jaybird 4 with Firebird 4 may
cause connection hangs when the connection is encrypted (the connection is 
blocked in a read from the socket). The cause seems related to the 
`TcpRemoteBufferSize` setting in Firebird. The workaround is to change the value
to a different value (it seems multiples of 8 or 16 prevent the problem) or to 
disable wire encryption in Firebird or for the specific connection (see 
[Wire encryption support]).

If you find a problem while upgrading, or other bugs: please report it 
on <http://tracker.firebirdsql.org/brows/JDBC>.

For known issues, consult [Known Issues].

What's new in Jaybird 4
=======================

For a full list of changes, see [Firebird tracker for Jaybird 4](http://tracker.firebirdsql.org/secure/ReleaseNote.jspa?projectId=10002&styleName=Text&version=10441).

Changes in artifact and library names
-------------------------------------

Historically, the naming of Jaybird artifacts and libraries has been a bit
inconsistent. With the rapid release cycle of Java, naming collisions are
imminent with the old naming convention. For example, the Maven artifact 
`jaybird-jdk15` was used for Java 1.5 and with this naming convention, this 
would be reused for Java 15.

Forced by this issue, we have overhauled the naming convention entirely to bring
more consistency between Maven artifacts and the Jaybird zip distribution. The
full naming convention is documented in [jdp-2019-02](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2019-02-version-number-and-naming-scheme.md).

This new naming convention has the following effects:

-   Java 9 and higher use suffix `javaXX` (eg `java11` for Java 11)
-   Java 8 and earlier will use suffix `jdkXX` (eg `jdk18` for Java 1.8)
    -   Previous Jaybird versions used suffix `jdkXX` for Maven, and `JDK_x.y` 
        for zip artifacts; these will now all use `javaXX` (or `jdkxx` for 
        Java 8 and earlier)
-   Names of libraries are now consistent with the Maven naming convention

As a result of these new naming conventions, the following has been changed (for
Java 11, read `java11` instead of `jdk18`)

-   Distribution zip: `jaybird-jdk18-4.0.0.zip` (was `Jaybird-3.0.5_JDK1.8.zip`)
-   Jaybird: `jaybird-jdk18-4.0.0.jar` (was `jaybird-3.0.5.jar` in zip 
    distribution)
-   Jaybird (full): `jaybird-full-jdk18-4.0.0.jar` (was 
    `jaybird-full-3.0.5.jar`)
-   Jaybird sources: `jaybird-jdk18-4.0.0-sources.jar` (was 
    `jaybird-3.0.5-sources.jar` in zip distribution)
-   Jaybird javadoc: `jaybird-jdk18-4.0.0-javadoc.jar` (was
    `jaybird-3.0.5-javadoc.jar` in zip distribution)
    
Furthermore, the client name reported to Firebird 2.5 and higher has been 
changed from `Jaybird 3.0.5-JDK_1.8` to `Jaybird jaybird-jdk17-4.0.0` 

Java support
------------

### Java 7 ###

The driver supports Java 7 with caveats.
 
-   Firebird 4 time zone types are not supported under Java 7, see also
    [Firebird 4 time zone support].

-   Under Java 7, Jaybird requires JAXB (`javax.xml.bind`), this will work in
    standard Java, but may require additional configuration in certain 
    environments, for example JBoss/Wildfly.
    
-   Some of the libraries used for testing Jaybird have upped there minimum 
    version to Java 8, while we need those library versions to test - for 
    example - Java 11. When we can no longer work around these issues, we will 
    sacrifice Java 7 test coverage in order to maintain Java 7 support. 

### Java 8 ###

The driver supports Java 8.

### Java 9 and higher ###

Jaybird 4 supports Java 9 and higher (JDBC 4.3) with the Java 8 and 11 version 
of the driver. Most of the JDBC 4.3 features have been implemented (in as far 
as they are supported by Firebird).

You can use the Java 8 driver under Java 9 and higher. For Java 11 or higher we 
recommend to use the Java 11 driver, though its sources are identical to the 
Java 8 driver. 

We recommend not to use the Java 7 version of Jaybird with Java 9 or higher. The 
Java 7 version doesn't implement all of the JDBC 4.3 features that are 
implemented in the Java 8 version. In addition, since Jaybird 3.0.4, the Java 7 
version of Jaybird needs the `java.xml.bind` module, where the Java 8 and higher 
versions do not need that module.

Given the limited support period for Java 9 and higher versions, we limit 
support on those versions to the most recent LTS version and the latest release.
Currently that means we support Java 7, 8, 11 and 12.

For compatibility with Java 9 modules, Jaybird defines the automatic module name 
`org.firebirdsql.jaybird`. This guarantees a stable module name for Jaybird, and 
allows for future modularization of Jaybird.

Firebird support
----------------

Support for Firebird 2.0 and 2.1 has been dropped. See [Firebird 2.0 and 2.1 no
longer supported] for details.

Firebird versions 2.5, 3.0 and (upcoming) 4.0 are supported.

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
 -   the encryption cipher uses ARCFOUR with a 160 bits key, this means that the 
     unlimited Cryptographic Jurisdiction Policy needs to be used (or at minimum 
     a custom policy that allows ARCFOUR with 160 bits keys). See also FAQ entry 
     [Encryption key did not meet algorithm requirements of Symmetric/Arc4 (337248282)](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#encryption-key-did-not-meet-algorithm-requirements-of-symmetricarc4-337248282) 
     
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

*TODO*: Document decfloat bind/traps/round connection property.
     
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

13. Getting values as `String` will be equivalent to `BigDecimal.toString()`,
with extra support for the special values mentioned in the previous note.

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
in Firebird 4, increasing the maximum precision to 34. In the implementation in 
Firebird, this extended precision is backed by a IEEE-754 Decimal128 which is 
also used for `DECFLOAT` support.

Any `NUMERIC` or `DECIMAL` with a precision between 19 and 34 will allow storage
up to a precision of 34. 

Values set on a field or parameter will be rounded to the target scale of the 
field using `RoundingMode.HALF_EVEN`. Values exceeding a precision of 34 after 
rounding will be rejected with a `TypeConversionException`.

Firebird 4 time zone support
----------------------------

Added support for the Firebird 4 `TIME WITH TIME ZONE` and `TIMESTAMP WITH TIME
ZONE` types. See the Firebird 4 release notes and `doc/sql.extensions/README.time_zone.md`
in the Firebird installation for details on these types.

The time zone types are supported under Java 8 and higher, using the Java 8 (or 
higher) version of Jaybird. 

Time zone types are not supported under Java 7, you will need to enable legacy 
time zone bind. With legacy time zone bind, Firebird will convert to the 
equivalent `TIME` and `TIMESTAMP` (`WITHOUT TIME ZONE`) types using the session 
time zone. Time zone bind can be configured with connection property 
`timeZoneBind`, for more information see [Time zone bind configuration].

See also [jdp-2019-03 Time Zone Support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2019-03-time-zone-support.md)  

NOTE: documentation below reflects state as currently implemented

### Scope of time zone support ###

JDBC 4.2 introduced support for time zones, and maps these types to 
`java.time.OffsetTime` and `java.time.OffsetDateTime`. JDBC does not define
explicit setters for these types. Use `setObject(index, value)`,
`updateObject(index, value)`, `getObject(index/name)` or 
`getObject(index/name, classType)`

Firebird 4 supports both offset and named time zones. Given the definition in
JDBC, Jaybird only supports offset time zones. On retrieval of a value with a
named zone, Jaybird will make a best effort to convert to the equivalent offset
using Java's time zone information. If no mapping is available the time will be 
returned at UTC (offset zero).

Jaybird 4 supports the following Java types on fields of time zone types (those
marked with * are not defined in JDBC)

`TIME WITH TIME ZONE`:

- `java.time.OffsetTime` (default for `getObject`)
  - On get, if the value is a named zone, it will derive the offset using the 
  current date
- `java.time.OffsetDateTime`
  - On get the current date is added
  - On set the date information is removed
- `java.lang.String`
  - On get applies `OffsetTime.toString()` (eg `13:25:13.1+01:00`)
  - On set tries the default parse format of either `OffsetTime` or 
  `OffsetDateTime` (eg `13:25:13.1+01:00` or `2019-03-10T13:25:13+01:00`)
  and then sets as that type
- `java.sql.Time` (\*)
  - On get obtains `java.time.OffsetDateTime`, converts this to epoch 
  milliseconds and uses `new java.sql.Time(millis)`
  - On set applies `toLocalTime()`, combines this with `LocalDate.now()`
  and then derives the offset time for the default JVM time zone
- `java.sql.Timestamp` (\*)
  - On get obtains `java.time.OffsetDateTime`, converts this to epoch 
  milliseconds and uses `new java.sql.Timestamp(millis)`
  - On set applies `toLocalDateTime()` and derives the offset time for the 
  default JVM time zone
  
`TIMESTAMP WITH TIME ZONE`:

- `java.time.OffsetDateTime` (default for `getObject`)
- `java.time.OffsetTime` (\*)
  - On get, the date information is removed
  - On set, the current date is added
- `java.lang.String`
  - On get applies `OffsetDateTime.toString()` (eg `2019-03-10T13:25:13.1+01:00`)
  - On set tries the default parse format of either `OffsetTime` or 
  `OffsetDateTime` (eg `13:25:13.1+01:00` or `2019-03-10T13:25:13+01:00`)
  and then sets as that type
- `java.sql.Time` (\*)
  - On get obtains `java.time.OffsetDateTime`, converts this to epoch 
  milliseconds and uses `new java.sql.Time(millis)`
  - On set applies `toLocalTime()`, combines this with `LocalDate.now()`
  and then derives the offset date time for the default JVM time zone
- `java.sql.Timestamp` (\*)
  - On get obtains `java.time.OffsetDateTime`, converts this to epoch 
  milliseconds and uses `new java.sql.Timestamp(millis)`
  - On set applies `toLocalDateTime()` and derives the offset date time for the 
  default JVM time zone
- `java.sql.Date` (\*)
  - On get obtains `java.time.OffsetDateTime`, converts this to epoch 
  milliseconds and uses `new java.sql.Date(millis)`
  - On set applies `toLocalDate()` at start of day and derives the offset date 
  time for the default JVM time zone

#### Support for legacy JDBC date/time types ####

For the `WITH TIME ZONE` types, JDBC does not define support for the legacy JDBC 
types (`java.sql.Time`, `java.sql.Timestamp` and `java.sql.Date`). To ease the 
transition and potential compatibility with tools and libraries, Jaybird does
provide support. However, we strongly recommend to avoid using these types. 

Compared to the `WITHOUT TIME ZONE` types, there may be small discrepancies in 
values as Jaybird uses 1970-01-01 for `WITHOUT TIME ZONE`, while for `WITH TIME
ZONE` it uses the current date. If this is problematic, then either apply the 
necessary conversions yourself, enable legacy time zone bind, or define or cast 
your columns to `TIME` or `TIMESTAMP`.

#### No support for other java.time types ####
  
The types `java.time.LocalTime`, `java.time.LocalDateTime` and 
`java.time.LocalDate` are not supported. Supporting these types would be 
ambiguous. If you need to use these, then either apply the necessary conversions 
yourself, enable legacy time zone bind, or define or cast your columns as `TIME` 
or `TIMESTAMP`. 
**NOTE: This is not final yet**

Jaybird also does not support non-standard extensions like `java.time.Instant`,
or `java.time.ZonedDateTime`. If there is interest, we may add them in the 
future.

### Time zone bind configuration ###

The connection property `timeZoneBind` (alias `time_zone_bind`) is a connection 
property to configure the time zone bind (see also `SET TIME ZONE BIND` in the 
Firebird 4 release notes).

The primary purpose of this property is to set the legacy time zone bind. This
needs to be explicitly set if you are using Java 7 and need to handle the 
`WITH TIME ZONE` types. It can also be used for tools or applications that
expect `java.sql.Time`/`Timestamp` types and cannot use the 
`java.time.OffsetTime`/`OffsetDateTime` types returned for the `WITH TIME ZONE` 
types.

Possible values (case insensitive):

-   `legacy`
    
    Firebird will convert a `WITH TIME ZONE` type to the equivalent `WITHOUT
    TIME ZONE` type using the session time zone to derive the value.
    
    Result set columns and parameters on prepared statements will behave as the
    equivalent `WITHOUT TIME ZONE` types. This conversion is not applied to the
    database metadata which will always report `WITH TIME ZONE` information.
    
-   `native`

    Behaves as default (`WITH TIME ZONE` types supported), but value will be 
    explicitly set.

Any other value will result in error `isc_time_zone_bind` (code 335545255, 
message _"Invalid time zone bind mode &lt;value&gt;"_) on connect.

**Important**: this feature requires Firebird 4 beta 2 or higher (or a snapshot 
build version 4.0.0.1481 or later). It will be ignored in earlier builds as the
necessary database parameter buffer item does not exist in earlier versions.

### Connection property sessionTimeZone ###

The connection property `sessionTimeZone` (alias `session_time_zone`) does two
things: 

1.  specifies the Firebird 4 session time zone (see Firebird 4 documentation),
2.  specifies the time zone to use when converting values to the legacy JDBC 
    datetime types (all Firebird version).  

By default, Jaybird will use the JVM default time zone as reported by 
`java.util.TimeZone.getDefault().getID()`. Using the JVM default time zone as 
the default is the best option in the light of JDBC requirements with regard to 
`java.sql.Time` and `java.sql.Timestamp` using the JVM default time zone.

Valid values are time zone names known by Firebird, we recommend to use the long
names (eg `Europe/Amsterdam`) and not the ambiguous short IDs (eg `CET`). 
Although not required, we recommend to use time zone names that are known by 
Firebird and Java (see [Session time zone for conversion] for caveats).

To use the default server time zone and the old behaviour to use the JVM default 
time zone, set the connection property to `server`. This will result in the 
conversion behaviour of Jaybird 3 and earlier. Be aware that this is 
inconsistent if Firebird and Java are in different time zones.

#### Firebird 4 session time zone ####

The session time zone is used for conversion between `WITH TIME ZONE` values 
and `WITHOUT TIME ZONE` values (ie using cast or with legacy time zone bind), 
and for the value of `LOCALTIME`, `LOCALTIMESTAMP`, `CURRENT_TIME` and 
`CURRENT_TIMESTAMP`, and other uses of the session time zone as documented in 
the Firebird 4 documentation.

The value of `sessionTimeZone` must be supported by Firebird 4. It is possible 
that time zone identifiers used by Java are not supported by Firebird. If 
Firebird does not know the session time zone, error (`Invalid time zone region:
<zone name>`) is reported on connect. 

The use of the JVM default time zone as the default session time zone will
result in subtly different behaviour compared to previous versions of Jaybird
and - even with Jaybird 4 - Firebird 3 or earlier, as current time values like 
`LOCALTIMESTAMP` (etc) will now reflect the time in the JVM time zone, and not 
the server time zone rebased on the JVM default time zone. 

As an example, with a Firebird in Europe/London and a Java application in 
Europe/Amsterdam with Firebird time 12:00, in Jaybird 3, the Java application
will report this time as 12:00, in Jaybird 4 with Firebird 4, this will now 
report 13:00, as that is the time in Amsterdam if it is 12:00 in London 
(ignoring potential DST start/end differences).

Other examples include values generated in triggers and default value clauses.

#### Session time zone for conversion ####

For `WITHOUT TIME ZONE` types, the session time zone will be used to derive the 
`java.sql.Time`, `java.sql.Timestamp` and `java.sql.Date` values. This is also 
done for Firebird 3 and earlier.

If Java does not know the session time zone, no error is reported, but when 
retrieving `java.sql.Time`, `java.sql.Timestamp` or `java.sql.Date` a warning is 
logged and conversion will happen in GMT, which might yield unexpected values.

We strongly suggest that you use `java.time.LocalTime`, 
`java.time.LocalDateTime` and `java.time.LocalDate` types instead of these 
legacy datetime types.

For `WITH TIME ZONE` types, the session time zone has no effect on the conversion
to the legacy JDBC date/time types: to offset date/time is converted to epoch
milliseconds and used to construct these legacy types directly.

Executing `SET TIME ZONE <zone name>` statements after connect will change the 
session time zone on the server, but Jaybird will continue to use the session
time zone set in the connection property for these conversions. 

### Time zone support for CONVERT ###

Although not defined in JDBC (or ODBC), Jaybird has added a non-standard 
extension to the `CONVERT` JDBC escape to allow conversion to the time zone 
types. 

In addition to the standard-defined types, it also supports the type names 
`TIME_WITH_TIME_ZONE`, `TIME_WITH_TIMEZONE`, `TIMESTAMP_WITH_TIME_ZONE` and 
`TIMESTAMP_WITH_TIMEZONE` (and the same with the `SQL_` prefix). 

### Caveats for time zone types ###

-   Time zone fields do not support `java.time.LocalDate`, `java.time.LocalTime`, 
    `java.time.LocalDateTime`. 
    **NOTE: This is not final yet**
    
-   Firebird 4 redefines `CURRENT_TIME` and `CURRENT_TIMESTAMP` to return a 
    `WITH TIME ZONE` type. Use `LOCALTIME` and `LOCALTIMESTAMP` (introduced in 
    Firebird 3.0.4) if you want to ensure a `WITHOUT TIME ZONE` type is used.
    
-   The database metadata will always return JDBC 4.2 compatible information on 
    time zone types, even on Java 7, and even when legacy time zone bind is set. 
    For Java 7 compatibility the JDBC 4.2 `java.sql.Types` constants 
    `TIME_WITH_TIMEZONE` and `TIMESTAMP_WITH_TIMEZONE` are also defined in 
    `org.firebirdsql.jdbc.JaybirdTypeCodes`.
    
-   The default `sessionTimeZone` is set to the JVM default time zone, this may
    result in different application behavior for `DATE`, `TIME` and `TIMESTAMP`, 
    including values generated in triggers and default value clauses. To prevent 
    this, either switch those types to a `WITH TIME ZONE` type, or set the 
    `sessionTimeZone` to `server` or to the actual time zone of the Firebird 
    server. 

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

In result sets, Jaybird will now also automatically map request for columns by 
name `RDB$DB_KEY` (case insensitive) to `DB_KEY` as Firebird automatically 
applies this alias for the `RDB$DB_KEY` column(s) in a select-list.

Be aware that result set metadata will still report `DB_KEY` as the column name 
and label.

JDBC DatabaseMetaData.getPseudoColumns implemented
--------------------------------------------------

The `DatabaseMetaData.getPseudoColumns` method (introduced in JDBC 4.1) has now
been implemented.

The JDBC API specifies this method as:

> Retrieves a description of the pseudo or hidden columns available in a given 
> table within the specified catalog and schema. Pseudo or hidden columns may 
> not always be stored within a table and are not visible in a `ResultSet` 
> unless they are specified in the query's outermost `SELECT` list. Pseudo or 
> hidden columns may not necessarily be able to be modified. If there are no 
> pseudo or hidden columns, an empty `ResultSet` is returned. 

For Firebird 2.5 and earlier it will only report on `RDB$DB_KEY`, for Firebird 3
and higher it will also report on `RDB$RECORD_VERSION`.

The pseudo-column `RDB$RECORD_VERSION` was introduced in Firebird 3, its value
is the transaction that last updated the row.

JDBC DatabaseMetaData.getVersionColumns implemented
---------------------------------------------------

The `DatabaseMetaData.getVersionColumns` method has now been implemented.

The JDBC API specifies this method as:

> Retrieves a description of a table's columns that are automatically updated 
> when any value in a row is updated. They are unordered. 

For Firebird 2.5 and earlier it will only report on `RDB$DB_KEY`, for Firebird 3
and higher it will also report on `RDB$RECORD_VERSION`.

The pseudo-column `RDB$RECORD_VERSION` was introduced in Firebird 3, its value
is the transaction that last updated the row.

Jaybird only returns pseudo-column as version columns, so 'last updated' columns 
updated by a trigger, calculated columns, or other forms of change tracking are 
not reported by this method.

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
    
Generated keys support improvements
-----------------------------------

Support for generated keys generation was improved with the following changes.

### Configuration of generated keys behaviour ###

A new connection property `generatedKeysEnabled` (alias `generated_keys_enabled`)
has been added that allows the behaviour of generated keys support to be 
configured. Also available on data sources.

This property supports the following values (case insensitive):

- `default`: default behaviour to enable generated keys for statement types with 
`RETURNING` clause in the connected Firebird version (absence of this property, 
`null` or empty string implies `default`). This corresponds to the existing 
behaviour.
- `disabled`: disable support. Attempts to use generated keys methods other than 
using `Statement.NO_GENERATED_KEYS` will throw a `SQLFeatureNotSupportedException`.
- `ignored`: ignore generated keys support. Attempts to use generated keys methods
will not attempt to detect generated keys support and execute as if the statement
generates no keys. The `Statement.getGeneratedKeys()` method will always return 
an empty result set. This behaviour is equivalent to using the non-generated 
keys methods.
- A comma-separated list of statement types to enable.

For `disabled` and `ignored`, `DatabaseMetaData.supportsGetGeneratedKeys` will 
report `false`.

Because of the behaviour specified in the next section, typos in property values
will behave as `ignored` (eg using `generatedKeysEnabled=disable` instead of 
`disabled` will behave as `ignored`).

#### Selectively enable statement types ####

This last option allows you to selectively enable support for generated keys.
For example, `generatedKeysEnabled=insert` will only enable it for `insert` 
while ignoring it for all other statement types. Statement types that are not 
enabled will behave as if they generate no keys and will execute normally. For 
these statement types, `Statement.getGeneratedKeys()` will return an empty 
result set.

Possible statement type values (case insensitive) are:

- `insert`
- `update`
- `delete`
- `update_or_insert`
- `merge`

Invalid values will be ignored. If none of he specified statement types are 
supported by Firebird, it will behave as `ignored`[^generated15].

[^generated15]: This is not the case for the unsupported Firebird 1.0 and 1.5
versions. There this will behave similar to `disabled`, and you will need to
explicitly specify `ignored` instead to get this behaviour.

Some examples:

- `jdbc:firebird://localhost/testdb?generatedKeysEnabled=insert` will only 
enable insert support
- `jdbc:firebird://localhost/testdb?generatedKeysEnabled=merge` will only 
enable merge support. But only on Firebird 3 and higher, for Firebird 2.5 this 
will behave as `ignored` given the lack of `RETURNING` support for merge.
- `jdbc:firebird://localhost/testdb?generatedKeysEnabled=insert,update` will 
only enable insert and update support

This feature can be used to circumvent issues with frameworks or tools that 
always use generated keys methods for prepare or execution. For example with 
`UPDATE` statements that touch multiple records and - given the Firebird 
limitations for `RETURNING` - produce the error _"multiple rows in singleton 
select"_.

### Support for MERGE ###

Firebird 3 added `RETURNING` support for `MERGE`, this support is now available
in Jaybird.

### Support for Firebird 4 RETURNING * ###

Firebird 4 added a `RETURNING *` ('returning all') clause which returns all
columns of the row affected by a DML statement. When connected to Firebird 4, 
the `Statement.RETURN_GENERATED_KEYS` methods will no longer query the database 
metadata for the column names, but instead append a `RETURNING *` clause.

Artificial testing by repeatedly executing the same insert statement using 
`Statement.execute(insert, Statement.RETURN_GENERATED_KEYS)` shows a performance 
improvement of roughly 200% (even 400% on localhost).

### Generated keys grammar simplification ###

The grammar used by the generated keys support has been simplified to avoid
issues with complex statements not being identified as types that generate keys, 
and to reduce maintenance.

The downside is that this may introduce different behaviour, as statements
previously not identified as generated keys types, now could be identified as
generated keys. Especially with DML other than `INSERT`, or 
`INSERT .. SELECT ..` this could result in error _"multiple rows in singleton 
select"_ as the `RETURNING` clause is currently only supported for statements 
that modify a single row.

You will either need to change the execution of these statements to use the 
normal execute/prepare or use `Statement.NO_GENERATED_KEYS`. Alternatively 
ignore or only selectively enable generated keys support, see 
[Configuration of generated keys behaviour] above.

### Other behavioural changes to generated keys ###

See [Changes to behaviour of generated keys] in [Stricter JDBC compliance]. 

Potentially breaking changes
----------------------------

Jaybird 4 contains a number of changes that might break existing applications.

See also [Compatibility changes] for details.

Other fixes and changes
-----------------------

-   The distribution zip no longer includes the jaybird-@VERSION@.rar. This file
was an example JCA Resource Archive.

    We currently plan to remove JCA support entirely in Jaybird 5. See also
[Dropping JCA (Java Connector Architecture) support].

-   Added support for Firebird 4 page size 32768 (32KB) in `FBManager` and backup 
managers (backported to Jaybird 3.0.5) ([JDBC-468](http://tracker.firebirdsql.org/browse/JDBC-468))

-   Changed: The value returned by `ResultSetMetaData.getColumnDisplaySize` was 
revised for `REAL`/`FLOAT` and `DOUBLE PRECISION` to take scientific notation 
into account ([JDBC-514](http://tracker.firebirdsql.org/browse/JDBC-514))

-   Fixed: Database metadata pattern parameters now allow the pattern escape 
character (`\`) to occur unescaped, this means that patterns `A\B` and `A\\B` 
will both match a value of `A\B`. This complies with the (undocumented) JDBC 
expectation that patterns follow the ODBC requirements for pattern value 
arguments ([JDBC-562](http://tracker.firebirdsql.org/browse/JDBC-562))

-   Upgraded antlr-runtime used for generated keys support from 4.7 to 4.7.2.
  
    The grammar generated for version 4.7.2 should still run on 4.7, but we
suggest that you upgrade this dependency.

-   Improvement: Added `FBManager.setDefaultCharacterSet` to set default 
database character set during database creation ([JDBC-541](http://tracker.firebirdsql.org/browse/JDBC-541))

-   New Feature: Support for Firebird 3 'explained' (detailed) execution plan ([JDBC-574](http://tracker.firebirdsql.org/browse/JDBC-574))
  
    Adds `FirebirdStatement.getLastExplainedExecutionPlan()`, 
`FirebirdPreparedStatement.getExplainedExecutionPlan()`, and 
`Firebird ResultSet.getExplainedExecutionPlan()`.
   
    This feature was contributed by [Vasiliy Yashkov](https://github.com/vasiliy-yashkov).
    
-   Upgraded jna library used for native/embedded from 4.4 to 5.3 ([JDBC-509](http://tracker.firebirdsql.org/browse/JDBC-509)

    The pull request to upgrade (from 4.4 to 5.2) was contributed by [Julien Nabet](https://github.com/serval2412).
    
-   Native libraries will now be disposed on application exit ([JDBC-519](http://tracker.firebirdsql.org/browse/JDBC-519))

    On JVM exit or - if deployed inside a WAR - servlet context destroy (tested 
on Tomcat), Jaybird will call `fb_shutdown` on any loaded native libraries and 
dispose the JNA handle to the native library. This should prevent crashes (eg 
access violation / 0xc0000005 error on Windows) on library unload if there were 
still embedded connections open.

    Given the potential for bugs or timing issues with this feature, it can be 
disabled with system property `org.firebirdsql.nativeResourceShutdownDisabled` 
set to `true`. This property must be set before Jaybird is loaded, preferably
on the Java command line.

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

As a result of changes in `FBDatabaseMetaData`, most result set producing 
methods will no longer work with Firebird 1.5 or earlier (unsupported since 
Jaybird 3).

Removed Legacy_Auth from default authentication plugins
-------------------------------------------------------

The pure Java protocol in Jaybird will - by default - no longer try the 
`Legacy_Auth` plugin when connecting to Firebird 3 or higher.

See [Default authentication plugins] for more information.

Time zone behaviour
-------------------

Using Jaybird 4 with Firebird 4 on a machine with a different time zone than the 
JVM, values of `CURRENT_TIMESTAMP`, `LOCALTIMESTAMP`, `CURRENT_TIME` and 
`LOCALTIME` will result in different values compared to Jaybird 4 with 
Firebird 3 (or Jaybird 3 or earlier with Firebird 4). This is caused by
Jaybird 4 setting the session time zone to the JVM default time zone. As a
result values will be in the time zone of the JVM and not the Firebird server
time zone.

To revert to the previous behaviour, explicitly set `sessionTimeZone=server`.
See [Connection property sessionTimeZone] for more information.

RDB$DB_KEY columns no longer of Types.BINARY
--------------------------------------------

With the introduction of [JDBC RowId support], `RDB$DB_KEY` columns are no 
longer identified as `java.sql.Types.BINARY`, but as `java.sql.Types.ROWID`.
The column will behave in a backwards-compatible manner as a binary field, with
the exception of `getObject`, which will return a `java.sql.RowId` instead.

Unfortunately this does not apply to parameters, see also [JDBC RowId support].

Due to the method of identification, real columns of type `char character set octets` 
with the name `DB_KEY` will also be identified as a `ROWID` column.

DatabaseMetaData.getBestRowIdentifier scope handling
----------------------------------------------------

Previously, the Jaybird implementation of `DatabaseMetaData.getBestRowIdentifier`
used the `scope` parameter to populate the `SCOPE` column of its result set, 
instead of using it to filter on the required scope.

This has been changed to instead filter on `scope`. In this implementation,
the columns of the primary key are considered the best row identifier, with
scope `bestRowSession`. It will be returned for all values of `scope`.

If a table does not have a primary key, the `RDB$DB_KEY` is considered the
second-best alternative, with scope `bestRowTransaction`. It will only be 
returned for scopes `bestRowTemporary` and `bestRowTransaction`. See also
[JDBC RowId support].

If you are currently using `DatabaseMetaData.getBestRowIdentifier` with 
`scope` value `DatabaseMetaData.bestRowSession`, consider if you need to
use `bestRowTransaction` instead.

If you are relying on the `SCOPE` column containing the value for the requested
scope, change your logic to remove that dependency.

Stricter JDBC compliance
------------------------

In Jaybird 4 a number of changes were made for stricter compliance to the JDBC
specification.

### Changes to behaviour of generated keys ###

#### Order of columns for columns by position ####

In previous versions of Jaybird, the column indexes (passed to 
`Connection.prepareStatement` and `Statement.executeXXX` methods accepting an 
`int[]`) where sorted. The columns in the generated `RETURNING` clause where
in ascending ordinal order.  

In Jaybird 4 this sort is no longer applied, so columns will be in the order 
specified by the array. If you were previously relying on this behaviour, you 
will need to sort the array yourself or correct the indexes used in 
`ResultSet.getXXX(int)`.

#### Empty or null columnIndexes or columnNames no longer allowed ####

The various generated keys `Connection.prepareStatement` and `Statement.executeXXX` 
methods accepting an `int[]` or `String[]` array no longer accept a null or 
empty array if the statement is a statement that generates keys. Instead an 
exception is thrown with message _"Generated keys array (columnIndexes|columnNames) 
was empty or null. A non-empty array is required."_

This change does not apply for statements that already explicitly include a 
`RETURNING` clause or for non-generated keys statements. In those cases, the
array is ignored.

#### Invalid column index no longer allowed ####

In addition, the methods accepting an `int[]` array no longer ignore invalid 
column indexes and instead throw an exception with message _"Generated keys 
column position &lt;position&gt; does not exist for table &lt;tablename&gt;. 
Check DatabaseMetaData.getColumns (column ORDINAL_POSITION) for valid values."_

If you were previously relying on this behaviour, you will need to remove 
invalid column indexes from the array.

This change does not apply for statements that already explicitly include a 
`RETURNING` clause or for non-generated keys statements. In those cases, the
array is ignored.

#### Unknown table ####

If generated keys methods using `Statement.RETURN_GENERATED_KEYS` or `int[]` 
cannot find any columns for a table, an exception is now thrown with message 
_"No columns were found for table &lt;tablename&gt; to build RETURNING clause. 
The table does not exist."_. Previously this executed as if the statement 
generated no keys and deferred to Firebird to return a _"Table unknown"_ error.

On Firebird 4, using `Statement.RETURN_GENERATED_KEYS` will continue to produce 
a _"Table unknown"_ error as it does not need to query the metadata, and instead 
defers this to Firebird using `RETURNING *`.

This change does not apply for statements that already explicitly include a 
`RETURNING` clause or for non-generated keys statements.

#### Grammar simplification ####

The generated keys grammar was changed, this may in some cases change the 
detection of statement types and execute statements previously generated 
as normal statement to be enhanced with a `RETURNING` clause.

This is probably only a theoretical concern (we don't know of actual cases
where detection changed). 

See also [Generated keys grammar simplification]. 

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

-   `useTranslation`: See [Removal of character mapping]
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

### Dropping JCA (Java Connector Architecture) support ###

Jaybird is currently built around a JCA (Java Connector Architecture) 
implementation. As such, it is both a JDBC driver and a JCA driver. The current
structure requires a dependency on JCA for non-JCA usage.

We will remove support for JCA entirely in Jaybird 5 to simplify the 
implementation. The package `org.firebirdsql.jca` will be removed entirely.

If you are currently using Jaybird as a JCA driver, please let us know on the 
Firebird-Java mailing list. We may reconsider this decision and instead 
restructure Jaybird so the dependency on JCA is only needed when Jaybird is used 
as a JCA driver. 

### Removal of deprecated methods ###

The following methods will be removed in Jaybird 5:

-   `MaintenanceManager.listLimboTransactions()`, use
    `MaintenanceManager.limboTransactionsAsList()` or 
    `MaintenanceManager.getLimboTransactions()` instead.
-   `TraceManager.loadConfigurationFromFile(String)`, use standard Java 
    functionality like `new String(Files.readAllBytes(Paths.get(fileName)), <charset>)`
-   `FBDatabaseMetaData.hasNoWildcards(String pattern)`
-   `FBDatabaseMetaData.stripEscape(String pattern)`
-   `StatementParser.parseInsertStatement(String sql)`, use 
    `StatementParser.parseStatement(String sql)`
    
### Removal of deprecated constants ###

The following constants will be removed in Jaybird 5:

-   All `SQL_STATE_*` constants in `FBSQLParseException` will be removed. Use equivalent 
    constants in `org.firebirdsql.jdbc.SQLStateConstants`.
-   `DatabaseParameterBufferExtension.EXTENSION_PARAMETERS` will be removed. There is no
    official replacement as this should be considered an implementation detail. It is
    possible that `DatabaseParameterBufferExtension` will be removed entirely.
    
Compatibility notes
===================

Type 2 (native) and embedded driver
-----------------------------------

Jaybird uses JNA to access the client library. If you want to use the Type 2 
driver, or Firebird embedded, then you need to include `jna-5.3.0.jar` on the 
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
