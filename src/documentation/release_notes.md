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

Jaybird 3.0 is a big change from Jaybird 2.2 and earlier. The entire low-level 
implementation has been rewritten to be able to support protocol improvements in
newer Firebird versions. We have also made changes with a stricter 
interpretation of the JDBC requirements, and removed some parts that were either 
obsolete or not functioning correctly.

We recommend that you do not consider Jaybird 3.0 a drop-in replacement for 
Jaybird 2.2, and study these release notes carefully. Test your application with
Jaybird 3.0 before using it in production.

Although Jaybird 3 is still maintained, we recommend updating to Jaybird 4.

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the Firebird-java mailing list or reported on the issue tracker
<http://tracker.firebirdsql.org/browse/JDBC>.

Support
=======

If you need support with Jaybird, join the [Firebird-Java Google Group](https://groups.google.com/d/forum/firebird-java)
and mailing list. You can subscribe by sending an email to [firebird-java+subscribe@googlegroups.com](mailto:firebird-java+subscribe@googlegroups.com).

Looking for professional support of Jaybird? Jaybird is now part of the [Tidelift subscription](https://tidelift.com/subscription/pkg/maven-org-firebirdsql-jdbc-jaybird?utm_source=maven-org-firebirdsql-jdbc-jaybird&utm_medium=referral&utm_campaign=docs).

See also [Where to get help](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#where-to-get-help)

Supported Firebird versions
---------------------------

Jaybird 3.0 was tested against Firebird 2.5.9, and 3.0.5, but should also 
support other Firebird versions from 2.0 and up. Firebird 4 is not fully 
supported in Jaybird 3.x.

Formal support for Firebird 1.x has been dropped (although in general we expect
the driver to work). The Type 2 and embedded server JDBC drivers use JNA to
access the Firebird client or embedded library.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

Jaybird 3.0 is the last version to support Firebird 2.0 and 2.1.

### Notes on Firebird 3 support

Jaybird 3.0.4 added support for wire protocol encryption and database encryption.
See [Wire encryption support] and [Database encryption support] for more 
information. 

Jaybird 3.0 does not support the Firebird 3 zlib compression.

### Notes on Firebird 4 support

Jaybird 3.0 can connect and query Firebird 4. Longer object names are supported. 

The new data types introduced in Firebird 4 are not supported. Support for data 
types like `DECFLOAT` and `NUMERIC`/`DECIMAL` with precision higher than 18 will 
be introduced in Jaybird 4.

The Srp256 authentication plugin is supported, but the other SrpNNN plugins are
not supported; support for these plugins has been introduced in Jaybird 4.

Jaybird 3.0 does not support the Firebird 4 zlib compression.

For improved support of Firebird 4, we recommend updating to Jaybird 4.

Supported Java versions
-----------------------

Jaybird 3 supports Java 7 (JDBC 4.1), Java 8 (JDBC 4.2), and Java 9 and 
higher (JDBC 4.3). Support for earlier Java versions has been dropped.

For the time being, there will be no Java 9+ specific builds, the Java 8 builds 
have the same source and all JDBC 4.3 related functionality.

Given the limited support period for Java 9 and higher versions, we limit
support on those versions to the most recent LTS version and the latest release.
As of May 2020, this means Java 11 and Java 14 are supported.

Jaybird 3.0 is not modularized, but since Jaybird 3.0.3, it declares the 
automatic module name `org.firebirdsql.jaybird`.

Specification support
---------------------

Jaybird supports the following specifications:

|Specification|Notes
|-------------|----------------------------------------------------------------
| JDBC 4.3    | Driver implements all JDBC 4.3 methods for features supported by Firebird; Java 9 and higher supported using the Java 8 driver.
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

For Windows and Linux, you can add the `org.firebirdsql.jdbc:fbclient`
dependency on your classpath to provide the native libraries for the `native` 
and `local` protocol. Be aware that this dependency does not support `embedded`.

See also [Type 2 (native) and embedded driver].

### Download ###

You can download the latest versions from <https://www.firebirdsql.org/en/jdbc-driver/>

At minimum Jaybird 3.0 requires `jaybird-@VERSION@.jar` and 
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

Upgrading from Jaybird 2.2 to Jaybird 3.0
=========================================

Maven
-----

Upgrade the version of the dependency to @VERSION@. If you use native or 
embedded, you will no longer need the `jaybird22.dll` or `libjaybird22.so`, see
the next section.

For more detailed instructions, see also the information on Maven in
[Getting Jaybird 3.0]. 

Manual install
--------------

If you manage your dependencies manually, you need to do the following:

1.  Replace the Jaybird library 2.2 with the 3.0
    - `jaybird-2.2.x.jar` with `jaybird-@VERSION@.jar` 
    - `jaybird-full-2.2.x.jar` with `jaybird-full-@VERSION@.jar`

2.  If installed, remove `antlr-runtime-3.4.jar` and replace it with 
    `antlr-runtime-4.7.jar`. This library is necessary for `getGeneratedKeys`
    support.
  
3.  If you use native (or embedded) you can remove the native library: 
    - `jaybird22.dll`, 
    - `libjaybird22.so`, 
    - `jaybird22_x64.jar` or
    - `libjaybird22_x64.so`
    
    Instead you need to add `jna-4.4.0.jar` to the classpath of your 
    application. This library is necessary for native, local and embedded 
    support. For more information, see [Type 2 (native) and embedded driver]
    
Gotcha's
--------

If you find a problem while upgrading, or other bugs: please report it 
on <http://tracker.firebirdsql.org/brows/JDBC>.

Jaybird 3.0.x changelog
=======================

Changes in Jaybird 3.0.10
-------------------------

The following has been changed or fixed since Jaybird 3.0.9:

-  ...

### Known issues in Jaybird 3.0.10

See [Known Issues]

Changes in Jaybird 3.0.9
------------------------

The following has been changed or fixed since Jaybird 3.0.8:

-   Fixed: changes to the transaction configuration (transaction parameter
    buffer configuration) of one connection are no longer propagated to other
    connections with the same connection properties ([JDBC-386](http://tracker.firebirdsql.org/browse/JDBC-386)) \
    This change introduce a binary incompatibility as method 
    `setTransactionParameters(int, TransactionParameterBuffer)` in
    `FBManagedConnection` can now throw `ResourceException` where previously it
    did not. Under the assumption that most users of Jaybird are not directly
    using this class, the change should not break anything.
-   New feature: Firebird 4 data type bind configuration support ([JDBC-603](http://tracker.firebirdsql.org/browse/JDBC-603)) \
    This change also removes the `timeZoneBind` and `decfloatBind` connection
    properties introduced in Jaybird 3.0.6 as the corresponding DPB items were
    removed from Firebird 4. This feature requires
    Firebird 4 beta 2 or snapshot Firebird 4.0.0.1683 or higher. \
    See also [Limited support for new Firebird 4 data types].
-   New feature: Jaybird now supports UTF-8 URL encoding for connection
    properties in the JDBC url. ([JDBC-604](http://tracker.firebirdsql.org/browse/JDBC-604)) \
    This introduce a minor incompatibility, see also 
    [URL encoding in query part of JDBC URL].
-   Fixed: When updating a row through an updatable result set, selected but
    not updated blob fields were set to `NULL` ([JDBC-623](http://tracker.firebirdsql.org/browse/JDBC-623))

Changes in Jaybird 3.0.8
------------------------

The following has been changed or fixed since Jaybird 3.0.7:

-   Fixed: On Firebird 3 and 4 with `WireCrypt = Enabled`, the connection could
    hang or throw exceptions like _"Unsupported or unexpected operation code"_. ([JDBC-599](http://tracker.firebirdsql.org/browse/JDBC-599)) \
    The implementation could read wrong data, followed by either a read blocked
    waiting for more data or an exception. \
    The underlying problem was how buffer padding was skipped using 
    `InputStream.skip`, which in `CipherInputStream` never skips beyond its
    current buffer. If that buffer was at (or 1 or 2 bytes from) the end, 
    Jaybird was reading less bytes than it should. This caused subsequent reads
    to read wrong data, reading too little or too much data.

Changes in Jaybird 3.0.7
------------------------

The following has been changed or fixed since Jaybird 3.0.6

-   Fixed: attempts to use a blob after it was freed or after transaction end
    could throw a `NullPointerException` or just work depending on whether the
    connection had a new transaction. ([JDBC-587](http://tracker.firebirdsql.org/browse/JDBC-587)) \
    The lifetime of a blob is now restricted to the transaction that created it,
    or - for blobs created using `Connection.createBlob()` - to the transaction
    that populated it. Attempts to use the blob after the transaction ends will 
    now throw a `SQLException` with message _"Blob is invalid. Blob was freed, 
    or closed by transaction end."_ This restriction does not apply to cached 
    blobs, see also next item. 
-   Fixed: Instances of `java.sql.Blob` and `java.sql.Clob` obtained from a 
    result set were freed after calls to `ResultSet.next()`. ([JDBC-588](http://tracker.firebirdsql.org/browse/JDBC-588)) \
    Normal blobs will now remain valid until transaction end. You will need to 
    call `Blob.free()` if you want to invalidate them earlier. \
    Cached blobs (in auto-commit, holdable or scrollable result sets) will not
    be automatically freed at transaction end. You will need to explicitly call
    `Blob.free()` or rely on the garbage collector.
-   New feature: added `FBEventManager.createFor(Connection)` to create an
    `EventManager` for an existing connection. Backported from Jaybird 4. ([JDBC-594](http://tracker.firebirdsql.org/browse/JDBC-594)) \
    The created event manager does not allow setting properties (other than
    `waitTimeout`). It is still required to use `connect()` and `disconnect()`,
    to start respectively stop listening for events. \
    Due to implementation limitations, the lifetime is tied to the physical 
    connection. When using a connection pool, this means that the event manager
    works as long as the physical pooled connection remains open, which can be
    (significantly) longer than the logical connection used to create the event
    manager. \
    This feature was contributed by [Vasiliy Yashkov](https://github.com/vasiliy-yashkov).
    
Changes in Jaybird 3.0.6
------------------------

The following has been changed or fixed since Jaybird 3.0.5

-   Fixed: Exceptions during fetch of cached result sets (holdable over commit, 
    scrollable and metadata) prevented prepared statement reuse/re-execute with
    error _"Statement state CURSOR_OPEN only allows next states \[CLOSING,
    PREPARED, ERROR], received EXECUTING"_ ([JDBC-531](http://tracker.firebirdsql.org/browse/JDBC-531))
-   Improvement: Added `FBManager.setDefaultCharacterSet` to set default 
    database character set during database creation ([JDBC-541](http://tracker.firebirdsql.org/browse/JDBC-541))
-   New feature: Support for Firebird 3 case sensitive user names ([JDBC-549](http://tracker.firebirdsql.org/browse/JDBC-549))  
    See [Case sensitive user names] for more information.
-   Fixed: Savepoints did not work in connection dialect 1 as savepoint names
    were always quoted ([JDBC-556](http://tracker.firebirdsql.org/browse/JDBC-556))  
-   Changed: The `DatabaseMetaData` statement cache introduced in Jaybird 3 was
    unlimited, it is now limited to 12 prepared statements; the least recently
    used statement will be closed and removed when a new statement is added ([JDBC-557](http://tracker.firebirdsql.org/browse/JDBC-557))
-   Fixed: `UPDATE OR INSERT` with existing `RETURNING` clause handled 
    incorrectly for generated keys ([JDBC-566](http://tracker.firebirdsql.org/browse/JDBC-566))
-   Fixed: Exceptions during initialization of result sets would not properly
    close the database cursor leading to error _"Current statement state
    (CURSOR_OPEN) does not allow call to prepare"_ on reuse of the statement (or
    errors similar to described for JDBC-531 above). ([JDBC-571](http://tracker.firebirdsql.org/browse/JDBC-571))  
    A stopgap measure has been added to prevent similar problems from occurring.
    This will log its use on debug-level with message _"ensureClosedCursor has
    to close a cursor at"_ and a stacktrace.
-   New feature: boolean connection property `ignoreProcedureType` to disable
    usage of metadata for stored procedure types in `CallableStatement`. When 
    set to `true`, call escapes and `EXECUTE PROCEDURE` will default to use 
    `EXECUTE PROCEDURE` and not switch to `SELECT` for selectable stored 
    procedures. ([JDBC-576](http://tracker.firebirdsql.org/browse/JDBC-576))  
    See [Connection property ignoreProcedureType] for more information.
-   New feature: connection properties `timeZoneBind` and `sessionTimeZone` for 
    limited support for Firebird 4 `TIME(STAMP) WITH TIME ZONE` types, and
    `decfloatBind` for limited support for Firebird 4 `DECFLOAT` types. ([JDBC-583](http://tracker.firebirdsql.org/browse/JDBC-583))  
    See [Limited support for new Firebird 4 data types] for more information.
-   Fixed: Connection property `defaultIsolation`/`isolation` did not work
    through `DriverManager`, but only on `DataSource` implementations. ([JDBC-584](http://tracker.firebirdsql.org/browse/JDBC-584))

Changes in Jaybird 3.0.5
------------------------

The following has been changed or fixed since Jaybird 3.0.4

-   Fixed: `FBManager` does not accept page size of 32768 (Firebird 4 and higher) ([JDBC-468](http://tracker.firebirdsql.org/browse/JDBC-468))
-   Fixed: Jaybird cannot parse Firebird version numbers with revisions ([JDBC-534](http://tracker.firebirdsql.org/browse/JDBC-534))
-   Fixed: Incorrect parsing of Firebird version numbers ([JDBC-535](http://tracker.firebirdsql.org/browse/JDBC-535))
-   New feature: Added support for the Srp256 authentication plugin ([JDBC-536](http://tracker.firebirdsql.org/browse/JDBC-536))  
    Firebird 4 by default will only authenticate with Srp256, and support for 
    Srp256 will be added in Firebird 3.0.4. Support for the other SrpNNN plugins 
    introduced in Firebird 4 will be added in Jaybird 4.  
    The addition of this plugin may lead to slightly slower authentication with 
    Firebird 3 versions that don't support Srp256 or that don't have it in the 
    `AuthServer` setting as additional roundtrips to the server are needed.
-   Fixed: Incorrect warning _"Specified statement was not created by this connection"_
    logged for statements that fail with an exception on prepare ([JDBC-538](http://tracker.firebirdsql.org/browse/JDBC-538))
-   Fixed: Remote close of event channel (eg on Firebird server stop or crash) leads
    to high CPU usage and excessive error logging as socket channel is not 
    removed from selector ([JDBC-542](http://tracker.firebirdsql.org/browse/JDBC-542))
-   Fixed: Properties `wireCrypt` and `dbCryptConfig` not available on
    `FBEventManager` ([JDBC-544](http://tracker.firebirdsql.org/browse/JDBC-544))
-   Documentation: wire protocol encryption requires unlimited strength 
    Cryptographic Jurisdiction Policy (or equivalent), this was previously not
    documented ([JDBC-545](http://tracker.firebirdsql.org/browse/JDBC-545))

Changes in Jaybird 3.0.4
------------------------

The following has been changed or fixed since Jaybird 3.0.3

-   New feature: Back-ported wire encryption support from Jaybird 4 ([JDBC-415](http://tracker.firebirdsql.org/browse/JDBC-415))  
    See [Wire encryption support] for more information.
-   Fixed: Native/embedded (JNA) connections truncate varchars to length 255 on 
    read ([JDBC-518](http://tracker.firebirdsql.org/browse/JDBC-518))  
    This fix was contributed by [Artyom Smirnov](https://github.com/artyom-smirnov)
-   New feature: Database encryption callback support in pure Java protocol ([JDBC-527](http://tracker.firebirdsql.org/browse/JDBC-527))  
    For more information, see [Database encryption support].  
    This feature was sponsored by IBPhoenix.

Changes in Jaybird 3.0.3
------------------------

The following has been changed or fixed since Jaybird 3.0.2

-   Fixed: Some older versions of JBoss would throw an `java.lang.IllegalStateException: 
    Can't overwrite cause` when `FBResourceException` initialised its exception 
    cause. ([JDBC-512](http://tracker.firebirdsql.org/browse/JDBC-512))
-   Improved: Added explicit `Automatic-Module-Name: org.firebirdsql.jaybird` to 
    manifest for forwards compatibility with Java 9 modularization. ([JDBC-511](http://tracker.firebirdsql.org/browse/JDBC-511))

Changes in Jaybird 3.0.2
------------------------

The following has been changed or fixed since Jaybird 3.0.1

-   Fixed: Specifying an unknown Java character set in connection property 
    `charSet` or `localEncoding` was handled as if no connection character
    set had been specified, now we throw an exception that the character set 
    is unknown. ([JDBC-498](http://tracker.firebirdsql.org/browse/JDBC-498))
-   Changed: Specifying a connection character set is no longer required, and
    will now default to `NONE` again, if system property 
    `org.firebirdsql.jdbc.defaultConnectionEncoding` is not specified. ([JDBC-502](http://tracker.firebirdsql.org/browse/JDBC-502))  
    The new requirement turned out to be too restrictive and hindering adoption
    of Jaybird 3. If you do want strict behaviour, you can specify system
    property `org.firebirdsql.jdbc.requireConnectionEncoding` with value `true`.
    See [Connecting without explicit character set] for more information.

Changes in Jaybird 3.0.1
------------------------

The following has been changed or fixed since Jaybird 3.0.0

-   Fixed: `FBTraceManager.loadConfigurationFromFile` strips line breaks ([JDBC-493](http://tracker.firebirdsql.org/browse/JDBC-493))
-   Fixed: `FBDatabaseMetaData.getTables` does not list tables where 
    `rdb$relation_type` is `null` ([JDBC-494](http://tracker.firebirdsql.org/browse/JDBC-494))
-   Improvement: Character sets are now initialized lazily ([JDBC-495](http://tracker.firebirdsql.org/browse/JDBC-495))  
    Under Excelsior Jet, the eager loading of character sets could lead to slow
    initialization if character sets were excluded from the build.
-   Fixed: Memory leak caused by retaining blob handles until connection close ([JDBC-497](http://tracker.firebirdsql.org/browse/JDBC-497))

Changes in Jaybird 3.0.0
------------------------

The following has been changed or fixed since Jaybird 3.0.0-beta-3

-   Changed: Remove automatic retrieval of sql counts in the low-level API on 
    execute or after fetching all rows ([JDBC-482](http://tracker.firebirdsql.org/browse/JDBC-482)  
    This restores the behavior of Jaybird 2.2 and should result in a minor 
    performance improvement when (fully) reading result sets or using
    `execute` instead of `executeUpdate` and **not** obtaining update counts.
-   Fixed: Database connection in NetBeans 8.2 fails ([JDBC-483](http://tracker.firebirdsql.org/browse/JDBC-483))
-   New feature: added method `getDatabaseTransactionInfo` to `StatisticsManager`
    to retrieve oldest, oldest active, oldest snapshot and next transaction, and
    the active transaction count ([JDBC-485](http://tracker.firebirdsql.org/browse/JDBC-485))  
    The active transaction count is only available in Firebird 2 and higher, for
    Firebird 1.5 and earlier this will have value `-1`.  
    A static `FBStatisticsManager.getDatabaseTransactionInfo(Connection connection)` 
    is available to obtain this information using an existing connection.
-   Removed dependency on JAXB ([JDBC-486](http://tracker.firebirdsql.org/browse/JDBC-486))  
    This removes the dependency on module `java.xml.bind` in Java 9, and in 
    Wildfly on module `javax.xml.bind.api`. 
-   Added system property `org.firebirdsql.jna.syncWrapNativeLibrary`. If this 
    system property has a value of `true`, the native library is wrapped in a 
    synchronisation proxy.  
    This synchronisation proxy will serialise all access to the native library.
    In previous versions of Jaybird this was always applied for Embedded on 
    platforms other than Windows.
-   Upgraded `antlr-runtime` dependency from 4.6 to 4.7 ([JDBC-488](http://tracker.firebirdsql.org/browse/JDBC-488))  
    If you tested with previous snapshot or beta versions of Jaybird 3.0, make
    sure to replace `antlr-runtime-4.5.3.jar` or `antlr-runtime-4.6.jar` 
    with `antlr-runtime-4.7.jar`.
-   Upgraded `jna` dependency from 4.2.2 to 4.4.0 ([JDBC-489](http://tracker.firebirdsql.org/browse/JDBC-489))
    If you use native, local or embedded and tested previous snapshot or beta 
    versions of Jaybird 3.0, make sure to replace `jna-4.2.2.jar` with 
    `jna.4.4.0.jar`.
-   Fixed: Presence of multiple copies of Jaybird (or its plugins) in multiple 
    class loader hierarchies could lead to a `ServiceConfigurationError` caused
    by incompatible class hierarchies. ([JDBC-490](http://tracker.firebirdsql.org/browse/JDBC-490))  
    This error was not caught and would bubble up the call chain, stopping 
    Jaybird from loading, and possibly stopping the application.     
    The error itself can still occur (because Jaybird is intentionally broad in
    the class loaders it tries), but the error is now caught and the next plugin 
    is tried.

Changes in Jaybird 3.0.0-beta-3
-------------------------------

The following has been changed or fixed since Jaybird 3.0.0-beta-2

-   Improved: Support for Firebird 4 object name length of 63 characters ([JDBC-467](http://tracker.firebirdsql.org/browse/JDBC-467))
-   Various improvements to thread safety and incomplete object validity checks.
    ([JDBC-469](http://tracker.firebirdsql.org/browse/JDBC-469))
    ([JDBC-470](http://tracker.firebirdsql.org/browse/JDBC-470))  
    Some methods did not throw an `SQLException` when the object (`ResultSet`, 
    `Statement`, `Connection`) was already closed. This sometimes lead to an
    unclear `RuntimeException` at a later point. In other cases an `SQLException`
    was thrown, even though the object was valid.  
-   Fixed: _Unsupported or unexpected operation code 0 in processOperation_ when
    executing stored procedure ([JDBC-472](http://tracker.firebirdsql.org/browse/JDBC-472))
-   Fixed: specifying `org.firebirdsql.jdbc.defaultConnectionEncoding` does not
    set connection character set ([JDBC-473](http://tracker.firebirdsql.org/browse/JDBC-473))  
    As part of this fix we also removed the need to have the system property set
    before Jaybird was loaded. It will now be queried dynamically for each
    connection without a connection character set.
-   Fixed: `ClassCastException` on downgrade of result set concurrency ([JDBC-474](http://tracker.firebirdsql.org/browse/JDBC-474))
-   Improved: `Statement.setFetchDirection` and `ResultSet.setFetchDirection` 
    now allow the values `ResultSet.FETCH_REVERSE` and `ResultSet.FETCH_UNKNOWN`
    ([JDBC-475](http://tracker.firebirdsql.org/browse/JDBC-475))  
    These values are effectively ignored, and result set behavior is the same as
    with the default value of `ResultSet.FETCH_FORWARD`.
-   Updated `DatabaseMetaData.getSqlKeywords` ([JDBC-476](http://tracker.firebirdsql.org/browse/JDBC-476))  
    The database metadata now returns the reserved words specific to the
    connected Firebird version. The reserved words, excluding those defined in
    SQL:2003, for versions 2.0, 2.1, 2.5 and 3.0 are available.
-   Improved: Calling `Blob.setBytes` and `Clob.setString` is now supported for 
    position `1`, on a new blob. ([JDBC-478](http://tracker.firebirdsql.org/browse/JDBC-478))
-   Upgrade `antlr-runtime` dependency from 4.5.3 to 4.6 ([JDBC-480](http://tracker.firebirdsql.org/browse/JDBC-480))  
    If you tested with previous snapshot or beta versions of Jaybird 3.0, make
    sure to replace `antlr-runtime-4.5.3.jar` with `antlr-runtime-4.6.jar`.
-   Fixed: Generated keys query for table with space (or any other character 
    below `\u0022`) in its (quoted) name returns empty generated keys result set ([JDBC-481](http://tracker.firebirdsql.org/browse/JDBC-481))

Changes in Jaybird 3.0.0-beta-2
-------------------------------

The following has been changed or fixed since Jaybird 3.0.0-beta-1

-   Fixed: Authentication with legacy auth users fails when Firebird 3 uses
    `AuthServer = Legacy_Auth` ([JDBC-460](http://tracker.firebirdsql.org/browse/JDBC-460))
-   Fixed: `jna-4.2.2.jar` was not included in the distribution zip ([JDBC-461](http://tracker.firebirdsql.org/browse/JDBC-461))
-   Changed logging of Embedded library to only log on error if none of the
    libraries could be loaded.
-   Fixed: native protocol is 20x-30x slower than Jaybird 2.2 native ([JDBC-463](http://tracker.firebirdsql.org/browse/JDBC-463))
-   Fixed: `ResultSetMetaData.getPrecision` of a numeric column when no 
    transaction is active throws an SQLException ([JDBC-464](http://tracker.firebirdsql.org/browse/JDBC-464))  
    As part of this fix, the handling of queries executed by `FBDatabaseMetaData` 
    has been changed. Most metadata queries are now kept prepared for reuse.

What's new in Jaybird 3.0
=========================

For a full list of changes, see [Firebird tracker for Jaybird 3.0.0](http://tracker.firebirdsql.org/secure/ReleaseNote.jspa?projectId=10002&styleName=Text&version=10440).

Java support
------------

### Java 6 ###

Support for Java 6 has been dropped.

### Java 7 and 8 ###

The driver supports Java 7 and 8 and provides improved support for JDBC 4.1 and
JDBC 4.2 features.

The improved support includes

-   Support for `java.time` in `set/get/updateObject`
-   Support for `java.math.BigInteger` in `set/get/updateObject`  
    Contrary to the support required by JDBC (`BIGINT`, `VARCHAR` and 
    `LONGVARCHAR`), we also support it for `SMALLINT`, `INTEGER`, `NUMERIC` and 
    `DECIMAL`). In the case of numeric and decimal, the value will be rounded
    for non-zero decimal fractions using the logic applied by 
    `BigDecimal.toBigInteger()`.
-   Support for `getObject(int/String, Class<?>)`
-   Support for `setBinaryStream`/`setCharacterStream` with no length or 
    (long) length beyond `Integer.MAX_VALUE`
-   Support for large update counts (but not `setLargeMaxRows`)

### Java 9 and higher ###

Jaybird currently does not fully support Java 9 and higher (JDBC 4.3), although 
most of the JDBC 4.3 features have been implemented (in as far as they are 
supported by Firebird).

For compatibility with Java 9+ modules, versions 2.2.14 and 3.0.3 introduced the 
automatic module name `org.firebirdsql.jaybird`. This guarantees a stable module 
name for Jaybird, and allows for future modularization of Jaybird.  

You can use the Java 8 driver under Java 9+. Contrary to earlier Jaybird 3.0 test 
releases, it is not necessary to add the `java.xml.bind` module using 
`--add-modules java.xml.bind`, as we removed its use (see caveat below for the 
Java 7 version of Jaybird).

Given the limited support period for Java 9 and higher versions, we limit
support on those versions to the most recent LTS version and the latest release.
As of November 2019, this means Java 11 and Java 13 are supported.

We recommend to only use the Java 8 version of Jaybird with Java 9+, and not use 
the Java 7 version of Jaybird. The Java 7 version doesn't implement all of the 
JDBC 4.3 features that are implemented in the Java 8 version. In addition, since 
Jaybird 3.0.4, the Java 7 version of Jaybird needs the `java.xml.bind` module, 
where the Java 8 version doesn't need that module. 

Firebird support
----------------

Support for Firebird 1.0 and 1.5 has been dropped. See [Firebird 1.0 and 1.5 no
longer supported] for details.

Firebird 2.1 support is improved with the implementation of wire protocol
version 11.

Firebird 2.5 support is improved with the implementation of wire protocol
version 12.

Firebird 3.0 support is improved with the (partial) implementation of wire
protocol 13 and support for the _Srp_ and _Srp256_ (Jaybird 3.0.5) 
authentication plugins. Version 13 support provides Firebird 3.0 
wire encryption support since Jaybird 3.0.4. Support for zlib compression is not 
planned yet.

See also [Jaybird and Firebird 3](https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3)
on the wiki.

Support for protocol version 13 and the SRP authentication was contributed
by [Hajime Nakagami](https://github.com/nakagami).

Partial Firebird 4 support:

- Longer metadata names (63 characters) in database metadata
- Authentication plugin _Srp256_ (Jaybird 3.0.5), but not the other _SrpNNN_ 
plugins
- Page size 32kb in management classes (Jaybird 3.0.5)
- Workarounds for new data types. The new data types introduced in Firebird (eg
`TIME WITH TIME ZONE`, `TIMESTAMP WITH TIME ZONE`, `DECFLOAT` and 
`NUMERIC`/`DECIMAL` with precision greater than 18) are **not** supported. See
[Limited support for new Firebird 4 data types] for workarounds for the 
`WITH TIME ZONE` and `DECFLOAT` types.

For improved support of Firebird 4, we recommend updating to Jaybird 4.

### Other Firebird feature support ###

*   Add support for streaming backup and restore ([JDBC-256](http://tracker.firebirdsql.org/browse/JDBC-256))  
    This feature was contributed by [Ivan Arabadzhiev](https://github.com/ls4f)

### Limited support for new Firebird 4 data types ###

For improved support of Firebird 4 datatypes, we recommend updating to Jaybird 4.

Jaybird 3 does not support the new Firebird 4 data types `TIME WITH TIME ZONE`,
`TIMESTAMP WITH TIME ZONE`, `DECFLOAT` and `NUMERIC`/`DECIMAL` with precision 
greater than 18. As an accommodation, Jaybird 3.0.9 and higher has limited
support for the following Firebird connection properties:

- `dataTypeBind` (alias: `set_bind`) which accepts a list of semicolon-separated
  bind definitions. See details below in [Notes on dataTypeBind].
- `sessionTimeZone` (alias: `session_time_zone`) configures the **server-side**
  session time zone used for conversion of `WITH TIME ZONE` to `WITHOUT TIME
  ZONE` and values generated by `CURRENT_TIME`, `LOCALTIME`, etc.  
  Valid values are Firebird time zone names or offsets. See also the Firebird 4 
  documentation. For important caveats, see [Notes on sessionTimeZone].
  
These properties can be used as connection properties with `DriverManager`. When
specified in a JDBC URL, make sure to encode the `;` in a `dataTypeBind` value
with `%3B`. For Jaybird data sources, the properties must be set using
`setNonStandardProperty` as corresponding setters have not been defined.
    
To be able to use `WITH TIME ZONE` types with Jaybird 3, you must use 
`dataTypeBind=time with time zone to legacy;timestamp with time zone to legacy`.
Setting `sessionTimeZone` is optional, see also [Notes on sessionTimeZone]. 

We recommend using `varchar` for `decfloat` and `numeric/decimal(38)` (as shown
in the next section) because that will allow you to use to full range of values
without overflow or loss of precision through `get/setBigDecimal`. Alternatively,
you can use `double precision`, although this may result in overflow for very
large `decfloat(34)` values.

**Important**: These features requires Firebird 4 beta 2 or higher (or a snapshot
build version 4.0.0.1683 or later). It will be ignored in builds before 1481 as
the necessary database parameter buffer item does not exist, and it will raise
an error in versions between 1481 and 1682 as there the DPB item points to the
removed DPB item `isc_time_zone_bind`.

#### Notes on dataTypeBind ####
  
Firebird 4 (build 4.0.0.1683 or later) introduced the `SET BIND` statement and 
`isc_dpb_set_bind` DPB item. This allows you to define data type conversion
rules for compatibility or ease of processing data.

In Jaybird this feature is exposed as connection property `dataTypeBind` (alias
`set_bind`). The value of this connection property is a semicolon-separated list
of data type bind definitions.

A data type bind definition is of the form `<from-type> TO <to-type>`. A
definition is the same as the second half of a `SET BIND` statement after the
`OF`. See the Firebird documentation of `SET BIND` for more information. Invalid
values or impossible mappings will result in an error on connect.

When using the `dataTypeBind` connection property in a JDBC URL, the semicolons
of the list need to be encoded as `%3B`, as semicolons in the JDBC URL are 
an alternative to `&` as the separator between properties.

For example:

```
String jdbcUrl = "jdbc:firebirdsql://localhost/database?charSet=utf-8"
        + "&dataTypeBind=decfloat to varchar%3Btimestamp with time zone to legacy
```

When the property is set through a `Properties` object or a `DataSource`
configuration, encoding the semicolon is not necessary or possible, and will
result in errors.

For example:

```
Properties props = new Properties();
props.setProperty("dataTypeBind", 
        "decfloat to varchar;timestamp with time zone to legacy"
``` 

Values set through this connection property will be the session default
configuration, which means that they are retained (or reverted to) when
executing `ALTER SESSION RESET`.

This feature replaces the connection properties `timeZoneBind` and
`decfloatBind` from earlier Jaybird 4 versions. The `timeZoneBind` and
`decfloatBind` properties are no longer supported.

To remap the Firebird 4 types, you will need to specify:

```
"decfloat to varchar;numeric(38) to varchar;decimal(38) to varchar" +
  ";time with time zone to legacy;timestamp with time zone to legacy"
```

Instead of `legacy`, you can also explicitly specify `time` and `timestamp`
respectively.

#### Notes on sessionTimeZone ####

In Jaybird 3 `sessionTimeZone` will only configure the server-side session time 
zone. Client-side, Jaybird will continue to use the JVM default time zone for 
parsing the value to the `java.sql.Time/Timestamp/Date` types. In Jaybird 4,
this property will also configure client-side parsing of values to these legacy
types.  

When Jaybird 3 and Firebird 4 are hosted on machines with different time zone
settings, setting `sessionTimeZone` can result in changes in current time values
(eg for `CURRENT_TIME`) as Firebird 4 will base these values on the session time
zone.

Setting `sessionTimeZone` to the JVM default time zone will yield the best 
(ie correct) values, but not setting it (and thus using the server default) will
retain behaviour that is backwards compatible with behaviour of previous 
versions of Jaybird. 

When setting `sessionTimeZone`, we recommend to use the long-form time zone
names (eg `Europe/Amsterdam`) and not the short-form ids (eg `CET`).

In general, we recommend not setting this property, or setting it to the default
JVM time zone. If you set it to a different time zone, then we recommend that
you do not use the legacy `java.sql.Time/Timestamp/Date` types, but instead use 
`java.time.LocalTime/LocalDateTime/LocalDate`.

New low-level implementation
----------------------------

Jaybird 3.0 has a substantially rewritten low-level implementation (the wire
protocol and native implementation) and a number of changes for JDBC 
conformance.

The rewrite of the low-level implementation was prompted by the new
authentication (and wire encryption) in Firebird 3.0 (protocol version 13), and
the fact that other improvements in the Firebird wire protocol (versions 11 
and 12) were not yet available in the pure Java implementation in Jaybird. The
old implementation of the wire protocol did not lend itself for - easily - 
supporting multiple protocol versions.

The new low-level implementation also means that the old GDS API 
(`org.firebirdsql.gds.GDS`) has been removed and is no longer available.

For the native, local and embedded support the use of 
`jaybirdxx.dll`/`libjaybirdxx.so` is no longer necessary. For more details, see
[Type 2 (native) and embedded driver].

Support for java.util.logging added
-----------------------------------

We have added support for `java.util.logging`, and made it the default logging
implementation.

We have applied the following mapping for the log levels:

|Jaybird log level   |_jul_ log level  |
|--------------------|-----------------|
| `Logger.trace`     | `Level.FINER`   |
| `Logger.debug`     | `Level.FINE`    |
| `Logger.info`      | `Level.INFO`    |
| `Logger.warn`      | `Level.WARNING` |
| `Logger.error`     | `Level.SEVERE`  |
| `Logger.fatal`     | `Level.SEVERE`  |

We have also added some options to control logging behavior:

*   System property `org.firebirdsql.jdbc.disableLogging` with value `true`
    will disable logging entirely.
*   System property `org.firebirdsql.jdbc.forceConsoleLogger` with value `true`
    will force logging to the `System.out` for info and lower and `System.err` 
    for warn and above (`debug` and `trace` are disabled in the implementation).
*   System property `org.firebirdsql.jdbc.loggerImplementation` to specify an
    alternative implementation of the interface `org.firebirdsql.logging.Logger`.  
    This implementation must be public and must have a public constructor with
    a single `String` argument for the logger name. See also 
    [Support for log4j 1.x removed] for an example.  
    The `org.firebirdsql.logging.Logger` interface is volatile and might change 
    in future minor releases (but not point/bugfix releases).

Support for log4j 1.x removed
-----------------------------

Support for Log4J 1.x has been removed. If you really need it, you can implement
the interface `org.firebirdsql.logging.Logger` with a public constructor 
accepting a single `String` parameter (the logger name).

To instruct Jaybird to use this logger implementation, specify the system 
property `org.firebirdsql.jdbc.loggerImplementation` with the class name.

Say you have created the following implementation

``` {.java}
package org.example.jaybird.logging;

public class Log4jLogger implements org.firebirdsql.logging.Logger {
    public Log4jLogger(String name) {
        // create the logger    
    }
    // implementation of interface
}
```

You will need to specify:

    -Dorg.firebirdsql.jdbc.loggerImplementation=org.example.jaybird.logging.Log4jLogger

IPv6 address literal support in connection string
-------------------------------------------------

Added support for IPv6 address literals in the connection string. This is only
supported for the modern-style connection URLs, using the [RFC2732](http://www.faqs.org/rfcs/rfc2732.html) format:

* `jdbc:firebirdsql://[<ipv6-address>]/<path-or-alias>`
* `jdbc:firebirdsql://[<ipv6-address>]:<port>/<path-or-alias>`

Examples:

```
jdbc:firebirdsql://[::1]/employee
jdbc:firebirdsql://[::192.9.5.5]:3050/employee
jdbc:firebirdsql://[1080::8:800:200C:417A]/employee
```
etc

IPv6 literals are not supported in the legacy URL format (the 
`<host>[/port]:<path-or-alias>` format).

Wire encryption support
-----------------------

Added in 3.0.4, back-ported from Jaybird 4

Jaybird 3.0.4 adds support for the Firebird 3 ARC4 wire encryption. The encryption
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
 -   the encryption cipher uses ARCFOUR with a 160 bits key, this means that the 
     unlimited Cryptographic Jurisdiction Policy needs to be used (or at minimum 
     a custom policy that allows ARCFOUR with 160 bits keys). See also FAQ entry 
     [Encryption key did not meet algorithm requirements of Symmetric/Arc4 (337248282)](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#encryption-key-did-not-meet-algorithm-requirements-of-symmetricarc4-337248282)     
     
Database encryption support
---------------------------

Added in 3.0.4. This feature was sponsored by IBPhoenix.

Jaybird 3.0.4 adds support for Firebird 3 database encryption callbacks in the 
pure Java implementation of the version 13 protocol. 

The current implementation is simple and only supports replying with a static 
value from a connection property. Be aware that a static value response for 
database encryption is not very secure as it can easily lead to replay attacks 
or unintended key exposure. 

Future versions of Jaybird (likely 4, maybe 5) will introduce plugin support for 
database encryption plugins that require a more complex callback.

The static response value of the encryption callback can be set through the 
`dbCryptConfig` connection property. Data sources and `ServiceManager` 
implementations have an equivalent property with the same name. This 
property can be set as follows:

-   Absent or empty value: empty response to callback (depending on the database 
    encryption plugin this may just work or yield an error later).
-   Strings prefixed with `base64:`: rest of the string is decoded as base64 to 
    bytes. The `=` padding characters are optional, but when present they must
    be valid (that is: if you use padding, you must use the right number of 
    padding characters for the length). \
    When the base64 encoded value contains `+`, it must be escaped as `%2B` in
    the JDBC URL. For backwards compatibility with previous versions of
    Jaybird 3, we can't switch to the URL-safe variant of base64.
-   Plain string value: string is encoded to bytes using UTF-8, and these bytes
    are used as the response. Avoid use of `:` in the plain string value.
    
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
    
Case sensitive user names
-------------------------

Jaybird 3.0.6 adds support for case sensitive user names.

Case sensitive user names were introduced in Firebird 3. A case sensitive user 
name must be enclosed in double quotes. Similar to other quoted object names in
Firebird, quoted user names can contain (almost) the full range of `UNICODE_FSS` 
characters, including whitespace, etc. 

For example, to login with the case sensitive user name `CaseSensitive`, use:

``` {.java}
DriverManager.getConnection(url, "\"CaseSensitive\"", password); 
```

Normal, case insensitive, user names can also be enclosed in double quotes. This 
follows the same rules as applied to table, column and other object names in 
Firebird, so the user name must then be in upper case, eg instead of `sysdba`, 
use `"SYSDBA"`. 

If a user name contains double quotes, it must be escaped by another double 
quote. A singular double quote within a user name is a syntax error and will
truncate the user name, leading to a login failure (unless a user exists with
the truncated name and the same password).

Connection property ignoreProcedureType
---------------------------------------
  
Jaybird 3.0.6 adds a boolean connection property `ignoreProcedureType`.

On Firebird 2.1 and higher, Jaybird will use the procedure type information from 
the database metadata to decide how to execute `CallableStatement`. When a 
procedure is selectable, Jaybird will automatically transform a call-escape or 
`EXECUTE PROCEDURE` statement to a `SELECT`.

In some cases this automatic transformation to use a `SELECT` leads to problems.
You can explicitly set `FirebirdCallableStatement.setSelectableProcedure(false)`
to fix most of these issues, but this is not always an option. For example 
spring-data-jpa's `@Procedure` will not work correctly with selectable 
procedures, but you can't call `setSelectableProcedure`.

To disable this automatic usage of procedure type information, set connection 
property `ignoreProcedureType=true`. When necessary you can use 
`FirebirdCallableStatement.setSelectableProcedure(true)` to execute a procedure 
using `SELECT`.

Be aware though, when `EXECUTE PROCEDURE` is used with a selectable procedure,
it is executed only up to the first `SUSPEND`, and the rest of the stored
procedure is not executed.

For Firebird 2.0 and lower this property has no effect, as there the procedure
type information is not available.

URL encoding in query part of JDBC URL
--------------------------------------

Added in Jaybird 3.0.9, backported from Jaybird 4.

Jaybird now supports UTF-8 URL encoded values (and keys) in the query part of
the JDBC URL.

As a result of this change, the following previously unsupported characters can
be used in a connection property value when escaped:

- `;` escaped as `%3B`
- `&` escaped as `%26`

URL encoding can also be used to encode any unicode character in the query
string. Jaybird will always use UTF-8 for decoding.

This change introduces the following backwards incompatibilities:

- `+` in the query part now means _space_ (0x20), so occurrences
of `+` (_plus_) need to be escaped as `%2B`
- `%` in the query part now introduces an escape, so occurrences 
of `%` (_percent_) need to be escaped as `%25`

Invalid URL encoded values will now throw a `SQLNonTransientConnectionException`.

The reason for this changes is that the new `setBind` connection property
requires semicolon-separated values, but Jaybird supports semicolon-separated
key/value connection properties in the query part. To be able to support this
new property in the connection string, we had to introduce URL encoding.

This change only applies to the JDBC URL part after the first `?`. This change
does not apply to connection properties set through `java.util.Properties` or on
a `javax.sql.DataSource`.

Potentially breaking changes
----------------------------

Jaybird 3.0 contains a number of changes that might break existing applications.

See also [Compatibility changes] for details.

### Specifying connection character set is now required ###

Jaybird 3.0.0 and 3.0.1 required you to specify the connection character set by either
specifying `encoding=<Firebird encoding>` or `charSet=<Java encoding>`.

This requirement has been dropped again in 3.0.2.

For more information see: [Connection rejected without an explicit character set]

### ANTLR 4 runtime ###

The generated keys functionality now requires ANTLR 4.7. Make sure to replace
`antlr-runtime-3.4.jar` with the `antlr-runtime-4.7.jar` included in the 
distribution zip. If you use maven this will happen automatically.

As in previous versions: if the ANTLR runtime is not on the classpath, then 
the generated keys functionality will not be available.

### Handling of character set OCTETS ###

Columns of type `CHAR` and `VARCHAR` with character set `OCTETS` are now handled
as JDBC type `BINARY` and `VARBINARY`, respectively ([JDBC-240](http://tracker.firebirdsql.org/browse/JDBC-240))

See also [Character set OCTETS handled as JDBC (VAR)BINARY].

### Changes to character set handling ###

We reimplemented the character set handling in Jaybird which may lead to
different behavior, especially when using `NONE` as the connection character set.

See also [Character set handling].

Other fixes and changes
-----------------------

*   Fix: IP-address is reversed on big-endian platforms ([JDBC-98](http://tracker.firebirdsql.org/browse/JDBC-98))

*   Improved support of JDBC Escape syntax (`{...}`) and supported functions
    ([JDBC-223](http://tracker.firebirdsql.org/browse/JDBC-223))

    The escape parser will now only allow the function names defined in
    Appendix D of the JDBC specification (4.2 for now). For unsupported
    functions or functions not listed in Appendix D a `FBSQLParseException` will
    be thrown.
    
    The database metadata will now correctly report supported functions for
    `getNumericFunctions`, `getStringFunctions`, `getSystemFunctions` and 
    `getTimeDateFunctions`.
    
*   Nested JDBC escapes are now supported ([JDBC-292](http://tracker.firebirdsql.org/browse/JDBC-292))

*   Changed locking to coarser blocks with - as far as possible - a single lock
    object per connection for all connection-derived objects ([JDBC-435](http://tracker.firebirdsql.org/browse/JDBC-435))

    This should prevent deadlocks on concurrent access. In some cases locks
    were obtained in different order (eg (statement, connection), and
    (connection, statement)). The downside is reduced concurrency, but as using
    a connection from multiple threads concurrently is discouraged anyway, that
    is an acceptable price to pay.

*   `DatabaseMetaData.getColumns` will report `YES` for `IS_AUTOINCREMENT` and 
    `IS_GENERATEDCOLUMN` if the column is an identity column ([JDBC-322](http://tracker.firebirdsql.org/browse/JDBC-322))
     
*   Added Jaybird-specific columns `JB_IS_IDENTITY` and `JB_IDENTITY_TYPE` to 
    `DatabaseMetaData.getColumns` to report the type of identity column ([JDBC-322](http://tracker.firebirdsql.org/browse/JDBC-322))
    
    `JB_IS_IDENTITY` has either `YES` or `NO` as possible values and can be used
    to check if the column is really an identity column.

    Possible values for `JB_IDENTITY_TYPE` are:
    
    *   `null` : not an identity column, or unknown identity type, 
    *   `ALWAYS` : a `GENERATED ALWAYS AS IDENTITY` column (introduced in 
        Firebird 4),
    *   `BY DEFAULT` : a `GENERATED BY DEFAULT AS IDENTITY` column (introduced 
        in Firebird 3).
    
    You should always retrieve these columns by name, as their position will 
    change when the JDBC specification adds new columns.

*   Added field index to `DataTruncation` exceptions ([JDBC-405](http://tracker.firebirdsql.org/browse/JDBC-405))

*   Added support for `getGeneratedKeys` for batch execution of prepared 
    statements ([JDBC-452](http://tracker.firebirdsql.org/browse/JDBC-452))

Removal of deprecated classes and packages
------------------------------------------

See [Removal of deprecated classes, packages and methods] in 
[Compatibility changes] for more details.

Known Issues
============

-   Firebird 3.0.1 does not correctly support `BOOLEAN` parameters, see [CORE-5367](http://tracker.firebirdsql.org/browse/CORE-5367)

    Either use Firebird 3.0.0 or 3.0.2 and higher.

-   Using a native connection with a Firebird 3 client library to a Firebird 2.5
    or older server may be slow to connect. The workaround is to specify the
    IPv4 address instead of the host name in the connection string, or to use a
    Firebird 2.5 or earlier `fbclient.dll`.
    
    This is caused by [CORE-4658](http://tracker.firebirdsql.org/browse/CORE-4658)
    
-   When using native or embedded, the default JNA 4.4.0 dependency may not work
    on some versions of Linux as it requires glibc 2.14. Upgrading the 
    dependency to JNA 4.5.x will solve this, as it requires glibc 2.7. See 
    [JDBC-509](http://tracker.firebirdsql.org/browse/JDBC-509).
      
    We decided not to upgrade the dependency in a point release. JNA 4.5.x can
    be specified as a Maven dependency or can be downloaded from 
    <https://github.com/java-native-access/jna#download>
    or from [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22net.java.dev.jna%22%20AND%20a%3A%22jna%22)
    
-   Jaybird 3.0.4 for Java 7 introduced a dependency on JAXB. When using Java 9
    or higher make sure to use the Jaybird 3.0.4 binaries for Java 8. If you use
    Wildfly or JBoss on Java 7, you will need to declare a dependency on JAXB,
    see [FAQ: Compatibility Notes > Wildfly](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#wildfly)
    for details.

Compatibility changes
=====================

Jaybird 3.0 introduces some changes in compatibility and announces future
breaking changes.

The changes due to the new protocol implementation and/or JDBC conformance are
listed below.

**The list might not be complete, if you notice a difference in behavior that is
not listed, please [report it as bug](http://tracker.firebirdsql.org/brows/JDBC).** 
It might have been a change we forgot to document, but it could just as well be 
an implementation bug.

Character set handling
----------------------

### Character set OCTETS handled as JDBC (VAR)BINARY ###

Columns of type `CHAR(n) CHARACTER SET OCTETS` and
`VARCHAR(n) CHARACTER SET OCTETS` are now handled as JDBC type
`java.sql.Types.BINARY` and `java.sql.Types.VARBINARY`, respectively.

The connection property `octetsAsBytes` no longer has any effect, metadata and
usage will always be `(VAR)BINARY`.

With this change the getters (on result set/callable statement) and
setters (prepared/callable statement) and update methods (result set) for columns
of this type have been restricted to:

* `set/get/updateNull`
* `get/set/updateBytes`
* `get/set/updateBinaryStream`
* `get/set/updateAsciiStream`
* `get/set/updateString` (using the default encoding or connection encoding)
* `get/set/updateCharacterStream` (using the default encoding or connection encoding)
* `get/set/updateObject` (with `String`, `byte[]`, `InputStream`, `Reader`)

Other getters/setters/updaters or object types supported for
'normal' `(VAR)CHAR` fields are not available.

### Connection character set NONE ###

Jaybird will now use the `(VAR)CHAR` or `BLOB SUB_TYPE TEXT` character set
information for decoding. This means, when using connection character set 
`NONE`, that columns with an explicit character set will be decoded and
encoded using that character set instead of using the platform default encoding
(or explicitly specified Java character set when specifying both `encoding=NONE` 
**and** `charSet=<some java charset>`).

This may lead to unexpected character conversions if - for example - you have 
always been reading and writing `Cp1251` data from a `WIN1252` column: it will
now be read as `Cp1252`. You will need to convert the column and data to the 
right character set.

### Connection rejected without an explicit character set ###

Jaybird version 3.0.0 and 3.0.1 would not connect if no connection character set
had been specified. This requirement has been dropped again. See 
[Connecting without explicit character set] for more information on the behavior
in Jaybird 3.0.2 and higher. 

### Connecting without explicit character set ###

If no connection character set has been specified (using connection property 
`encoding` or `charSet` or their aliases), then - by default - Jaybird 3.0.2 and 
higher will default to connection character set `NONE`. Be aware that `NONE` in 
Jaybird 3 does not behave the same as in Jaybird 2.2 and earlier, see 
[Connection character set NONE] for information.

Using `NONE` can result in incorrect character set handling when the database is 
used from different locales.

You can explicitly set the connection character set using one of the following 
options:

*   Use connection property `encoding` (alias: `lc_ctype`) with a Firebird character
    set name. 

*   Use connection property `charSet` (alias: `localEncoding`) with a Java character
    set name.
    
*   Use a combination of `encoding` and `charSet`, if you want to reinterpret a 
    Firebird character set in a Java character set other than the default 
    mapping.

To control how Jaybird handles connections without explicit character sets, you
can use the following options:

*   You can configure a default Firebird character set - overriding the default 
    of `NONE` - with system property 
    `org.firebirdsql.jdbc.defaultConnectionEncoding`. Jaybird will apply the
    specified character set as the default when no character set is specified
    in the connection properties.
      
    This property only supports Firebird character set names.
    
*   You can require an explicit character set to be specified with system
    property `org.firebirdsql.jdbc.requireConnectionEncoding` set to `true`.
    With this property specified, a connection character set must have been
    specified using connection properties or using the system property 
    `org.firebirdsql.jdbc.defaultConnectionEncoding`.
    
    This is the behaviour that was the default in Jaybird 3.0.0 and 3.0.1.
  
    This property will cause Jaybird to reject the connection, if no character 
    set has been set, with an `SQLNonTransientConnectionException` with message 
    _"Connection rejected: No connection character set specified (property 
    lc_ctype, encoding, charSet or localEncoding). Please specify a connection 
    character set (eg property charSet=utf-8) or consult the Jaybird 
    documentation for more information."_.

Logging
-------

Support for log4j has been removed, and we now default to `java.util.logging`.
The previous default was no logging.

See also [Support for java.util.logging added] and [Support for log4j 1.x removed].

Exceptions
----------

*   `FBSQLException` and sub-classes replaced with actual `java.sql.*` exceptions.

    Over time the JDBC exception hierarchy has become more complicated with more
    specific exceptions. It was easier to use the `java.sql` exception-
    hierarchy, than to duplicate the hierarchy within Jaybird.

    This change does not mean that there are no Firebird-specific `SQLException`
    sub-classes anymore, but in general we strive to use the standard
    exceptions where possible. 

*   Class `FBSQLWarning` has been removed and replaced with `java.sql.SQLWarning`.

*   Methods with `throws FBSQLException` changed to `throws SQLException`

    As we are preferring the standard exceptions, the throws clause has been
    widened to `SQLException`. Note that most methods already had
    `throws SQLException`, so the impact is limited. This change specifically
    impacts:

    * `org.firebirdsql.jdbc.FirebirdPreparedStatement`
        * `getExecutionPlan()`
        * `getStatementType()`
    * `org.firebirdsql.management.FBServiceManager`
        * `executeServicesOperation`
    * A number of classes/methods internal to the Jaybird implementation

*   `org.firebirdsql.gds.GDSException` has been removed from exception causes.

    The new low-level implementation throws `java.sql.SQLException` classes
    eliminating the need for `GDSException` (which was usually set as the
    `cause` of an `SQLException`). In some cases uses of `GDSException`
    have been replaced by `org.firebirdsql.jdbc.FBSQLExceptionInfo` to
    report exception message elements and their error codes.

*   Exception message format changed:
   
    *   Exception message elements are now separated by semi-colon, not by
        linebreak.

        Errors reported by Firebird can consist of multiple elements. In Jaybird
        2.2 and earlier the final exception message was constructed by
        separating these elements by a linebreak. These elements are now
        separated by a semi-colon and a space.

    *   Exception message now reports SQLState and error code.

    For example, a "Table unknown" (error 335544580) in Jaybird 3.0 has message:

    ~~~
    Dynamic SQL Error; SQL error code = -204; Table unknown; TABLE_NON_EXISTENT; At line 1, column 13 [SQLState:42S02, ISC error code:335544580]
    ~~~

    Jaybird 2.2 and earlier reported this as (`\n` added to show line break):

    ~~~
    Dynamic SQL Error\n
    SQL error code = -204\n
    Table unknown\n
    TABLE_NON_EXISTENT\n
    At line 1, column 13
    ~~~

    The SQLState and error code are only included in the message if the exception
    is constructed using `FbExceptionBuilder` (for example errors received from
    Firebird). Some parts of the code construct an `SQLException` directly,
    those messages do not contain the SQLState and error code in the message. We
    strive to improve that in future versions.

*   More specific error reported by `SQLException.getErrorCode` and
    `SQLException.getSQLState`.

    In previous versions a large class of errors always reported error 335544569
    ("Dynamic SQL Error" or `isc_dsql_error`) with SQLState 42000, Jaybird now
    tries to find a more specific error code (and SQLState) in the status vector.
    
*   Added Jaybird specific error codes for some exceptions. The error code range
    `337248256` - `337264639` has been reserved by the Firebird project for use 
    by Jaybird.
    
    We will migrate more Jaybird-specific exceptions to these error codes in
    future versions.

Firebird 1.0 and 1.5 no longer supported
----------------------------------------

Support for Firebird 1.0 and 1.5 has been dropped in Jaybird 3.0. In general we
expect the driver to remain functional, but chances are certain metadata (eg 
`DatabaseMetaData`) will break if we use features introduced in newer versions.

In general we will no longer fix issues that only occur with Firebird 1.5 or
earlier.

Java 5 and 6 no longer supported
--------------------------------

Support for Java 6 (JDBC 4.0) has been dropped in Jaybird 3.0. The Jaybird 3.0
sources no longer compile with Java 6 due to use of Java 7 language features and
JDBC 4.1 specific features.

Support for Java 5 was already removed with Jaybird 2.2.8.

Stricter JDBC compliance
------------------------

In Jaybird 3.0 a number of changes were made for stricter compliance to the JDBC
specification.

### General ###

Most methods in JDBC objects are required to throw an `SQLException` if 
the object is closed or otherwise invalid. Not all Jaybird methods followed this
requirement. We have improved this in Jaybird 3.0, but there are still some cases
left to fix (which we might do in point releases).

### Statement ###

_Unless explicitly indicated, changes also apply to `PreparedStatement` and
`CallableStatement`_

*   Generated keys `ResultSet` is only available through `getGeneratedKeys`.

    The generated keys `ResultSet` from a statement is no longer available
    through `getResultSet`, but only through `getGeneratedKeys` as the JDBC
    specification does not consider the generated keys `ResultSet` a normal
    `ResultSet`.

    <span id="generated-query-types">This applies to statements executed (or
    prepared) using:</span>

    * `Statement.execute(String, int)`, `Statement.executeUpdate(String, int)`
      or `Statement.executeLargeUpdate(String ,int)` 
      with value `Statement.RETURN_GENERATED_KEYS`,
    * `Statement.execute(String, int[])`,
      `Statement.executeUpdate(String, int[])` or
      `Statement.executeLargeUpdate(String, int[])`,
    * `Statement.execute(String, String[])`m
      `Statement.executeUpdate(String, String[])` or
      `Statement.executeLargeUpdate(String, String[])`,
    * `Connection.prepareStatement(String, int)` with value
      `Statement.RETURN_GENERATED_KEYS`,
    * `Connection.prepareStatement(String, int[])`,
    * `Connection.prepareStatement(String, String[])`.

    This change does not apply to executing `INSERT ... RETURNING ...` as a
    normal statement.

*   Update count immediately available after executing generated keys queries.

    Previously the update count of a generated keys query was only available
    after calling `getMoreResults` followed by a call to `getUpdateCount`. This
    change means that `executeUpdate` will now correctly return the update count
    (usually `1`) instead of `-1`. The same applies to calling `getUpdateCount`
    after `execute` (without the need to call `getMoreResults`).

    For the definition of generated keys queries see [the previous item](#generated-query-types).

*   Use of function escapes (`{fn ...}`) not defined in the JDBC standard will 
    now throw an `FBSQLParseException`, previously the escape was removed and 
    the function was used as is.

### PreparedStatement ###

_Unless explicitly indicated, changes also apply to `CallableStatement`_

*   Method `setUnicodeStream` now always throws
    an `SQLFeatureNotSupportedException`. The previous implementation did not
    conform to the (deprecated) JDBC requirements and instead behaved like
    `setBinaryStream`.

    For the behavior in Jaybird 2.2 and earlier, use `setBinaryStream`.
    Otherwise use `setCharacterStream`.

*   Methods `setNString`, `setNClob`, and `setNCharacterStream` will now behave
    as their counterpart without `N` (ie `setString`, `setClob`, and
    `setCharacterStream`)

    This implementation is not compliant with the JDBC requirements for
    `NVARCHAR/NCHAR/NCLOB` support, it is only provided for compatibility
    purposes.

### CallableStatement ###

*   Methods `getNString`, `getNClob`, and `getNCharacterStream` will now behave
    as their counterpart without `N` (ie `getString`, `getClob`, and
    `getCharacterStream`)

    This implementation is not compliant with the JDBC requirements for
    `NVARCHAR/NCHAR/NCLOB` support, it is only provided for compatibility
    purposes.

### ResultSet ###

*   Method `getUnicodeStream` now always throws
    an `SQLFeatureNotSupportedException`. The previous implementation did not
    conform to the (deprecated) JDBC requirements and instead behaved like
    `getBinaryStream`.

    For the behavior in Jaybird 2.2 and earlier, use `getBinaryStream`.
    Otherwise use `getCharacterStream`.

*   Methods `getNString`, `updateNString`, `getNClob`, `updateNClob`,
    `getNCharacterStream`, and `updateNCharacterStream` will now behave as their
    counterpart without `N` (ie `getString`, `updateString`, `getClob`,
    `updateClob`, `getCharacterStream`, and `updateCharacterStream`)

    This implementation is not compliant with the JDBC requirements for
    `NVARCHAR/NCHAR/NCLOB` support, it is only provided for compatibility
    purposes.

### Clob ###

*   The `Clob` implementations of the driver now also implement `NClob` so they
    can be returned from `getNClob`.

    This implementation is not compliant with the JDBC requirements for
    `NVARCHAR/NCHAR/NCLOB` support, it is only provided for compatibility
    purposes.

### DatabaseMetaData ###

#### Pattern parameters ####

The `java.sql.DatabaseMetaData` implementation has been changed to follow the
JDBC requirements for object name pattern or object name parameters (referred to
as _patterns_ in the rest of this section).

These changes affect all methods that accept one or more pattern or name
parameters and return a `ResultSet`. This includes, but is not limited to,
`getTables` and `getColumns`.

In Firebird unquoted object names are stored upper case, while quoted object
names are stored as is. The JDBC specification states that the pattern must
match the object name as stored in the database. This means the pattern should
be case sensitive.

The changes made are as follows:

*   `null` will always be interpreted as `"%"` (before this rule was applied
    inconsistently)
*   Empty string will no longer match (ie it is no longer interpreted as `"%"`) 
    unless explicitly allowed by the method javadoc (usually only the
    `catalogPattern` and `schemaPattern`, which are always ignored by Jaybird as
    Firebird currently doesn't support this)
*   Double quotes around a pattern will no longer be stripped, and therefor will
    now never match existing object names (unless those double quotes are part
    of the actual object name)
*   The driver will no longer try the uppercase variant of the provided
    pattern(s) if the original value(s) did not yield a result
*   Object name parameters that are not patterns (as indicated by the absence of
    `Pattern` in the parameter name) will no longer have backslashes removed

Review your `DatabaseMetaData` usage and make the following changes:

*   Empty string parameters: replace with `"%"` (or `null`)
*   Double quotes around patterns: remove the double quotes
*   Casing of patterns should be reviewed for correctness
*   Non-pattern parameters containing `\ ` should be reviewed for correctness

Some examples:

~~~ {.sql}
CREATE TABLE tablename ( -- tablename is stored as TABLENAME in metadata
   column1 INTEGER,      -- column1 is stored as COLUMN1 in metadata
   "column2" INTEGER     -- "column2" is stored as column2 in metadata
);
~~~

In Jaybird 2.2 using `getColumns(null, null, "tablename", "column%")` returns
`COLUMN1`(!). In Jaybird 3.0 this produces **no rows** as `tablename` does not
match `TABLENAME`.

Changing the query to `getColumns(null, null, "TABLENAME", "column%")` in
Jaybird 2.2 and 3.0 produces only one row (`column2`), as `COLUMN1` does not
match `column%`.

In Jaybird 2.2 using `getColumns(null, null, "\"TABLENAME\"", "column%")`
returns `column2`, in Jaybird 3.0 this produces **no rows** as quotes are no
longer stripped.

In Jaybird 2.2 using `getColumns(null, null, "TABLENAME", "")` returns all
columns of the table, in Jaybird 3.0 this produces **no rows** as empty string
does not match any column. Instead, you should use
`getColumns(null, null, "TABLENAME", "%")`.

#### getTables ####

Apart from the change described above, the following has changed for `getTables`

*   The result set is now sorted by `TABLE_TYPE` and then by `TABLE_NAME` as
    required by the JDBC API doc, previously we only only sorted on
    `TABLE_NAME`.
*   Support for table type `"GLOBAL TEMPORARY"` added for databases with
    ODS 11.2 or higher (Firebird 2.5 or higher). In previous versions,
    the global temporary tables were reported as normal tables (type `"TABLE"`)

Removal of old GDS API
----------------------

The old GDS API (`org.firebirdsql.gds.GDS`) and its implementations have been
removed. This removal includes a number of related classes and methods. Classes
and interfaces in the `org.firebirdsql.gds` package and sub-packages that still
exist may have moved to other packages, or may have a changed API.

In general we advise you to not use the low-level implementation directly. If
you see something that is only possible through the low-level API, please file
an improvement ticket or create a pull request to add it to the JDBC or
management classes.

Type 2 (native) and embedded driver
-----------------------------------

Jaybird no longer needs a `jaybirdxx.dll` or `libjaybirdxx.so` for the Type 2 
and embedded driver. Jaybird now uses JNA to access the client library.

If you want to use the Type 2 driver, or Firebird embedded, then you need to
include `jna-4.4.0.jar` on the classpath.

When using Maven, you need to specify the dependency on JNA yourself, as we 
don't depend on it by default:

``` {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>4.4.0</version>
</dependency>
```

The `fbclient.dll`, `fbembed.dll`, `libfbclient.so`, or `libfbembed.so` need to
be on the path, or the location needs to be specified in the system property 
`jna.library.path` (as an absolute or relative path to the directory/directories
containing the library file(s)).

For Windows and Linux, you can add the `org.firebirdsql.jdbc:fbclient`
dependency on your classpath to provide the native libraries for the `native` 
and `local` protocol. Be aware that this dependency does not support `embedded`.

``` {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>fbclient</artifactId>
    <version>3.0.4.0</artifactId>
</dependency>
```

In the future we will move the Type 2 support to a separate library and provide 
JNA-compatible jars that provide the embedded libraries of a specific Firebird 
version.

Incompatibilities due to URL encoding in JDBC URL query part
------------------------------------------------------------

With the introduction in Jaybird 3.0.9 of URL encoding for the query part of the
JDBC URL, the use of characters `+` and `%` in the query part of a JDBC URL now
have different meaning and can lead to errors or unexpected results.

See [URL encoding in query part of JDBC URL] for more information.

Removal of deprecated classes, packages and methods
---------------------------------------------------

### DataSource and connection pooling ###

The classes in `org.firebirdsql.pool` and `org.firebirdsql.pool.sun` have been
removed completely, with the exception of 
`org.firebirdsql.pool.FBSimpleDataSource`. This class has been moved to
`org.firebirdsql.ds.FBSimpleDataSource`. A subclass with the same name is kept
in `org.firebirdsql.pool` for backwards compatibility. This subclass will be
removed in Jaybird 4.

With this change, there are no `javax.sql.DataSource` implementations in Jaybird
that provide connection pooling (the `javax.sql.ConnectionPoolDataSource`
implementations are for use by a connection pool and not a connection pool
themselves). Either use the connection pool provided by your application server,
or use a third-party connection pool like [c3p0](http://www.mchange.com/projects/c3p0/), 
[Apache DBCP](https://commons.apache.org/proper/commons-dbcp/) or [HikariCP](https://brettwooldridge.github.io/HikariCP/).

The class `org.firebirdsql.jca.FBXADataSource` has been removed as well. Its
replacement is `org.firebirdsql.ds.FBXADataSource` (which was introduced in
Jaybird 2.2).

### FirebirdSavepoint ###

All method definitions in the interface 
`org.firebirdsql.jdbc.FirebirdSavepoint` were removed, and methods referencing
this interface in `org.firebirdsql.jdbc.FirebirdConnection` have been removed as
the interface duplicated the `java.sql.Savepoint` interface and related methods
in `java.sql.Connection`. The interface itself remains for potential future
Firebird-specific extensions.

### Reducing visibility of implementation ###

The following classes, interfaces and/or methods had their visibility reduced as
they are implementation artifacts, and should not be considered API:

- `org.firebirdsql.jdbc.FBDriverPropertyManager` (to package private)

### FBMaintenanceManager ###

In `FBMaintenanceManager` the following changes have been made:

- `getLimboTransactions()` will return `long[]` instead of `int[]`
- `limboTransactionsAsList()` will return `List<Long>` instead of `List<Integer>`
- `getLimboTransactionsAsLong()` (introduced in 2.2.11) has been removed in 
  favor of `getLimboTransactions()`
- `limboTransactionsAsLongList` (introduced in 2.2.11) has been removed in favor
  of `limboTransactionsAsList`

These methods were previously not defined in the `MaintenanceManager` interface.

Miscellaneous
-------------

-   In some cases `ResultSetMetaData.getPrecision` will estimate the precision.
    In Jaybird 2.2 and earlier the estimate used for `NUMERIC` and `DECIMAL` was
    `19`, this has been revised to the more correct value of `18`.
-   `ResultSetMetaData.getColumnDisplaySize` was revised to take into account 
    space for sign and decimal separator for numeric types.

Breaking changes for Jaybird 3.1
--------------------------------

The version previously announced as 3.1 will be released as Jaybird 4.

Breaking changes for Jaybird 4
------------------------------

With Jaybird 4 the following breaking changes will be introduced.

### Dropping support for Firebird 2.0 and 2.1 ###

Jaybird 4 will drop support for Firebird 2.0 and 2.1. In general we expect the
driver to remain functional, but chances are certain metadata (eg 
`DatabaseMetaData`) will break if we use features introduced in newer versions.

### Dropping support for Java 7 ###

Jaybird 4 will be the last version to support Java 7, Jaybird 5 will drop 
support for Java 7.

### Removal of deprecated methods ###

The following methods will be removed in Jaybird 4:

-   Character set mapping (translation) will be removed entirely. Connection 
    property `useTranslation` (and it's alias `mapping_path`) will no longer be
    available.
    
    Similar effects can be achieved by a custom encoding implementation.
    
    As part of this change the following parts of the implementation will be 
    removed (note that most are internal to Jaybird):
    
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
    
-   The following connection properties will be removed:

    -   `useTranslation`: See previous item
    -   `octetsAsBytes`: Since Jaybird 3 octets is always handled as `BINARY`
    -   `noResultSetTracking`: Option does nothing since Jaybird 3
    -   `paranoia_mode`: Option does nothing since Jaybird 2.2 (maybe earlier)
    
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
-   `FBBlob#copyCharacterStream(Reader reader, long length, String encoding)`
-   `FBBlob#copyCharacterStream(Reader reader, String encoding)`  

### Removal of deprecated constants ###

The following constants will be removed in Jaybird 4:

-   All `SQL_STATE_*` constants in `FBSQLException`,
    `FBResourceTransactionException`, `FBResourceException`, and
    `FBDriverNotCapableException` will be removed. Use equivalent constants in
    `org.firebirdsql.jdbc.SQLStateConstants`.
