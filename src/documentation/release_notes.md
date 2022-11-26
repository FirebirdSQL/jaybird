Jaybird 4.0.x changelog
=======================

Changes per Jaybird 4 release. See also [What's new in Jaybird 4](#whats-new-in-jaybird-4).
For known issues, consult [Known Issues](#known-issues).

Jaybird 4.0.8
-------------

The following has been changed or fixed since Jaybird 4.0.7

- Improvement: Backported new generated keys parser from Jaybird 5 to remove
  dependency on ANTLR ([jaybird#718](https://github.com/FirebirdSQL/jaybird/issues/718)) \
  With this change, Jaybird no longer relies on `antlr-runtime-4.7.2.jar`, if
  you don't need it yourself, you can remove this library from the classpath.
  See [New parser for generated keys handling][#generated-keys-parser-replaced] 
  for more information.

Jaybird 4.0.7
-------------

The following has been changed or fixed since Jaybird 4.0.6

- Improvement: `(VAR)CHAR` is now sent to the server with `blr_varying2` or
  `blr_text2` which includes the character set information ([jaybird#692](https://github.com/FirebirdSQL/jaybird/issues/692))
- Changed: Usages of `String.toUpperCase` and `String.toLowerCase` now use 
  `Locale.ROOT` to prevent locale-sensitivity issues ([jaybird#697](https://github.com/FirebirdSQL/jaybird/issues/697))
- New feature: Support for NBackup "clean history" option ([jaybird#706](https://github.com/FirebirdSQL/jaybird/issues/706)) \
  The `org.firebirdsql.management.NBackupManager` interface has three new 
  methods: `setCleanHistory(boolean)` to enable (or disable) cleaning of history
  during backup, and `setKeepDays(int)` and `setKeepRows(int)` to specify 
  the number of days or rows to keep history. These options require Firebird
  4.0.3 or higher. \
  This feature was backported from Jaybird 5.
- Fixed: Calling `PreparedStatement.setClob` or `PreparedStatement.setBlob` with
  a `null` `Clob`, `Reader`, `Blob`, or `InputStream` would result in 
  a `NullPointerException` ([jaybird#712](https://github.com/FirebirdSQL/jaybird/issues/712)) \
  As part of this change the behaviour of `setClob` methods accepting a `Reader`
  was changed to be identical to `setCharacterStream`, and `setBlob` accepting 
  an `InputStream` to `setBinaryStream`. The end result before and after 
  this change is identical, but it can result in different memory and 
  performance characteristics, as the stream is now consumed on execute, and not
  on set.

Jaybird 4.0.6
-------------

The following has been changed or fixed since Jaybird 4.0.5

- Improvement: `Connection.isValid(int)` now uses the timeout as a network  
  timeout, if possible ([jaybird#685](https://github.com/FirebirdSQL/jaybird/issues/685)) \
  This is only supported for pure Java connections. For native connections,
  the timeout is ignored (and a `SQLWarning` is registered).
- `NativeResourceUnloadWebListener` would fail in Servlet containers with 
  a `NoClassDefFoundError` if JNA wasn't on the classpath ([jaybird#686](https://github.com/FirebirdSQL/jaybird/issues/686))
- Fixed: Calling `isBeforeFirst()`, `isAfterLast()`, `isFirst()`, or `isLast()`
  on a closed result set resulted in a `NullPointerException` instead of a
  `SQLException` ([jaybird#689](https://github.com/FirebirdSQL/jaybird/issues/689))

Jaybird 4.0.5
-------------

The following has been changed or fixed since Jaybird 4.0.4

-   Fixed: `JnaService` implementation call to `isc_service_query` incorrectly
    includes type ([jaybird#678](https://github.com/FirebirdSQL/jaybird/issues/678)) \
    This could lead to service requests not completing successfully for native
    connections.

Jaybird 4.0.4
-------------

The following has been changed or fixed since Jaybird 4.0.3

-   Fixed: `ResultSet.updateRow()` sets fields to `null` in result set only ([jaybird#37](https://github.com/FirebirdSQL/jaybird/issues/37)) \
    The `updateRow()` method did not correctly update local data of the result
    set. As a result, calling `getXXX` for that row after the update would
    return `null` for the updated rows. The update was correctly persisted to
    the database.
-   New feature: Support for NBackup GUID-based backup and in-place restore ([jaybird#672](https://github.com/FirebirdSQL/jaybird/issues/672)) \
    The `org.firebirdsql.management.NBackupManager` interface has two new 
    methods: `setBackupGuid(String)` expecting the brace-enclosed GUID of a
    previous backup to use as the starting point for this backup, and 
    `setInPlaceRestore(boolean)` to enable (or disable) in-place restore. These
    options require Firebird 4.0 or higher. \
    This feature was backported from Jaybird 5.
-   Fixed: Logic error could lead to incorrect logging of _"Specified statement
    was not created by this connection"_ ([jaybird#674](https://github.com/FirebirdSQL/jaybird/issues/674))
-   Changed: Updated Firebird 4.0 reserved words based on 4.0.0.2496 ([jaybird#597](https://github.com/FirebirdSQL/jaybird/issues/597))
-   Fixed: Protocol 15 and 16 had same priority, so Firebird 4.0 might select 
    protocol 15, leading to timeout support not available. ([jaybird#676](https://github.com/FirebirdSQL/jaybird/issues/676))

Jaybird 4.0.3
-------------

The following has been changed or fixed since Jaybird 4.0.2

-   Changed: Closing a statement will now be sent to the server immediately ([JDBC-638](http://tracker.firebirdsql.org/browse/JDBC-638)) \
    For the v11 protocol and up (Firebird 2.1 or higher), Jaybird applied an
    optimization copied from Firebird to not flush 'free' packets that are used
    to close a cursor or close a statement. The packet will then be sent at a
    later time when something else is sent to the server. \  
    Unfortunately, this has the side effect that if the statement close is the
    last thing to happen in a long time (e.g. the connection is idle, or
    returned to a pool), then the statement may retain locks on metadata objects
    longer than necessary, which can prevent DDL from succeeding. \
    This change only affects Jaybird's implementation of the Firebird wire
    protocol; connections with the 'native' protocol will still delay sending
    the 'free' packet.

Jaybird 4.0.2
-------------

The following has been changed or fixed since Jaybird 4.0.1

-   Fixed: First letter of JDBC escape was case-sensitive ([JDBC-632](http://tracker.firebirdsql.org/browse/JDBC-632)) \
    This was a regression compared to 2.2.x. Fix was also back-ported to 3.0.10.
-   Fixed: Some usernames cannot authenticate using SRP ([JDBC-635](http://tracker.firebirdsql.org/browse/JDBC-635)) \
    Fix was also back-ported to 3.0.10.
-   Fixed: `ServiceConfigurationError` while loading plugins could prevent
    Jaybird from loading ([JDBC-636](http://tracker.firebirdsql.org/browse/JDBC-636)) \
    Fix was also back-ported to 3.0.10.

Jaybird 4.0.1
-------------

The following has been changed or fixed since Jaybird 4.0.0

-   Fixed: Changes to the transaction configuration (transaction parameter
    buffer configuration) of one connection are no longer propagated to other
    connections with the same connection properties ([JDBC-386](http://tracker.firebirdsql.org/browse/JDBC-386)) \
    This change introduces a binary incompatibility as method 
    `setTransactionParameters(int, TransactionParameterBuffer)` in
    `FBManagedConnection` can now throw `ResourceException` where previously it
    did not. Under the assumption that most users of Jaybird are not directly
    using this class, the change should not break anything.
-   Fixed: Search index of Javadoc in Java 11 version used incorrect links ([JDBC-619](http://tracker.firebirdsql.org/browse/JDBC-619))
-   Fixed: The cleanup of native resources didn't dispose the native library
    held by JNA, as a change in implementation no longer allowed directly access
    to the JNA `NativeLibrary` ([JDBC-620](http://tracker.firebirdsql.org/browse/JDBC-620))
-   Fixed: When updating a row through an updatable result set, selected but
    not updated blob fields were set to `NULL` ([JDBC-623](http://tracker.firebirdsql.org/browse/JDBC-623))
-   Added: Support for type `INT128` (reported as JDBC type `NUMERIC`) ([JDBC-624](http://tracker.firebirdsql.org/browse/JDBC-624)) \
    See also [Firebird 4 INT128 support](#firebird-4-int128-support).
-   Added: A static utility method `FBDriver.normalizeProperties` which, given a
    JDBC url and a `Properties` object, returns a `Map<String, String>`
    containing the merged properties normalized to common property name. ([JDBC-627](http://tracker.firebirdsql.org/browse/JDBC-627)) \
    The current implementation normalizes known property names to the long-form
    `isc_dpb` name, and removes the `database` property. These are both 
    implementation details that might change in future versions. \
    This feature is also back-ported to Jaybird 3.0.10.
-   Fixed: Use of `isc_dpb_no_db_triggers` no longer logs a warning ([JDBC-628](http://tracker.firebirdsql.org/browse/JDBC-628))
-   Incompatible change: While making changes to time zone support, the API of 
    `org.firebirdsql.gds.ng.tz.TimeZoneDatatypeCoder` was made almost entirely
    private. This should not affect normal user code. \
    Although we try to avoid these types of incompatible changes in point
    releases, we explicitly allow them for the `org.firebirdsql.gds.ng` package
    and sub-packages.
-   Changed: conversions from `TIME WITH TIME ZONE` now use 2020-01-01 as base
    date for named zones ([JDBC-629](http://tracker.firebirdsql.org/browse/JDBC-629)) \
    There are some caveats with this conversion, especially between `OffsetTime`
    and `OffsetDateTime` for named zones. For `OffsetTime`, we will always use
    the offset as it was on 2020-01-01, while for `OffsetDateTime` we will first
    rebase the time in the named zone to the current date, and then derive the
    offset. \
    See also [jdp-2020-06 OffsetTime derivation for named zone](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-06-offsettime-derivation-for-named-zone.md)
-   New feature: Added support for `java.time.ZonedDateTime` for the `WITH TIME
    ZONE` types ([JDBC-630](http://tracker.firebirdsql.org/browse/JDBC-630)) \
    To preserve named zones, we have added support for getting and setting both
    `TIME WITH TIME ZONE` and `TIMESTAMP WITH TIME ZONE` as a `ZonedDateTime`. \
    For `TIME WITH TIME ZONE`, the returned value is rebased on the current
    date.
-   Fixed: `Connection.setNetworkTimeout` incorrectly used the provided
    `Executor` to set the timeout ([JDBC-631](http://tracker.firebirdsql.org/browse/JDBC-631)) \
    This caused a race condition where the timeout was possibly applied too
    late, and when `connection.close()` was called immediately after, this could
    trigger a `NullPointerException` that would bubble up into the executor.

Jaybird 4.0.0
-------------

The following has been changed or fixed since Jaybird 4.0.0-beta-2

-   Changed: The 'DEC_FIXED' extended numeric precision support (for Firebird
    4.0.0-beta-1) has been removed ([JDBC-596](http://tracker.firebirdsql.org/browse/JDBC-596)) \
    Now only the 'INT128' extended numeric precision (for Firebird 4.0.0-beta-2
    and higher, or snapshot 4.0.0.1604 or higher) is supported.
-   Changed: Updated dependency on JNA from 5.3.0 to 5.5.0 ([JDBC-509](http://tracker.firebirdsql.org/browse/JDBC-509)) \
    Make sure to replace `jna-4.4.0.jar` (or `jna-5.3.0.jar` when coming from
    an earlier test version of Jaybird 4) with `jna-5.5.0.jar`.
-   New feature: Support for `EXTENDED TIME(STAMP) WITH TIME ZONE` types
    introduced in Firebird 4.0.0.1795 ([JDBC-611](http://tracker.firebirdsql.org/browse/JDBC-611)) \
    The 'extended' time zone types are a 'bind-only' data type that provides an
    additional offset, so an offset is also known for values using a named zone.
    Jaybird ignores this information and handles these types exactly the same as
    normal time zone types. \
    See also [jdp-2020-01: Extended Time Zone Types Support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-01-extended-time-zone-types-support.md) 
-   Fixed: Problem connecting to Firebird 4.0.0.1737 and higher with 
    `SQLException` with message "Unexpected tag type: 3" ([JDBC-612](http://tracker.firebirdsql.org/browse/JDBC-612)) 

Jaybird 4.0.0-beta-2
--------------------

The following has been changed or fixed since Jaybird 4.0.0-beta-1

-   New feature: support for `DatabaseMetaData.getFunctions` ([JDBC-552](http://tracker.firebirdsql.org/browse/JDBC-552)) \
    See also [DatabaseMetaData getFunctions implemented](#databasemetadata-getfunctions-implemented).
-   New feature: support for `DatabaseMetaData.getFunctionColumns` ([JDBC-552](http://tracker.firebirdsql.org/browse/JDBC-552)) \
    See also [DatabaseMetaData getFunctionColumns implemented](#databasemetadata-getfunctioncolumns-implemented).
-   Fixed: Connection property `defaultIsolation`/`isolation` did not work
    through `DriverManager`, but only on `DataSource` implementations. ([JDBC-584](http://tracker.firebirdsql.org/browse/JDBC-584))
-   Changed: Changed version numbering and naming scheme ([JDBC-585](http://tracker.firebirdsql.org/browse/JDBC-585)) \
    See [Changes in artifact and library names](#changes-in-artifact-and-library-names)
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
-   New feature: Support for `Connection.setNetworkTimeout` and 
    `getNetworkTimeout` ([JDBC-589](http://tracker.firebirdsql.org/browse/JDBC-589)) \
    This JDBC feature is only supported on pure Java connections, not in native
    or embedded. Calling `setNetworkTimeout` will override the timeout set with
    the `soTimeout` connection property. When a timeout occurs, the connection
    will be closed.
-   Changed: Procedures in packages are no longer returned from 
    `DatabaseMetaData.getProcedures` and `getProcedureColumns` ([JDBC-590](http://tracker.firebirdsql.org/browse/JDBC-590)) \
    See also [Excluding procedures from packages](#excluding-procedures-from-packages).
-   Changed: On Firebird 4 and higher, precision of `FLOAT` and `DOUBLE PRECISION`
    are reported using binary precision (24 and 53 respectively), instead of
    decimal precision (7 and 15 respectively) ([JBC-591](http://tracker.firebirdsql.org/browse/JDBC-591)) \
    See also [Precision reported for FLOAT and DOUBLE PRECISION on Firebird 4](#precision-reported-for-float-and-double-precision-on-firebird-4).
-   Improvement: added binary literal prefix (`x'`) and suffix (`'`) to 
    `DatabaseMetaData.getTypeInfo` for `LONGVARBINARY`, `VARBINARY` and 
    `BINARY` ([JDBC-593](http://tracker.firebirdsql.org/browse/JDBC-593))
-   New feature: added `FBEventManager.createFor(Connection)` to create an
    `EventManager` for an existing connection. Back-ported to Jaybird 3.0.7. ([JDBC-594](http://tracker.firebirdsql.org/browse/JDBC-594)) \
    The created event manager does not allow setting properties (other than
    `waitTimeout`). It is still required to use `connect()` and `disconnect()`,
    to start respectively stop listening for events. \
    Due to implementation limitations, the lifetime is tied to the physical 
    connection. When using a connection pool, this means that the event manager
    works as long as the physical pooled connection remains open, which can be
    (significantly) longer than the logical connection used to create the event
    manager. \
    This feature was contributed by [Vasiliy Yashkov](https://github.com/vasiliy-yashkov).
-   Changed: Firebird 4.0.0.1604 and later changed the format of extended
    numeric precision from a Decimal128 to an Int128, increasing the maximum
    decimal precision from 34 to 38. The Int128 format is now supported. ([JDBC-595](http://tracker.firebirdsql.org/browse/JDBC-595)) \
    Support for the old format will be removed after Jaybird 4.0.0-beta-2. See
    also [Firebird 4 extended numeric precision support](#firebird-4-extended-numeric-precision-support).
-   New experimental feature: A way to monitor driver operations (specifically
    statement executes and fetches). ([JDBC-597](http://tracker.firebirdsql.org/browse/JDBC-597)) \
    See [Operation monitoring](#operation-monitoring) for details. \
    This feature was contributed by [Vasiliy Yashkov](https://github.com/vasiliy-yashkov).
-   Fixed: On Firebird 3 and 4 with `WireCrypt = Enabled`, the connection could
    hang or throw exceptions like _"Unsupported or unexpected operation code"_. ([JDBC-599](http://tracker.firebirdsql.org/browse/JDBC-599)) \
    The implementation could read wrong data, followed by either a read blocked
    waiting for more data or an exception. \
    The underlying problem was how buffer padding was skipped using 
    `InputStream.skip`, which in `CipherInputStream` never skips beyond its
    current buffer. If that buffer was at (or 1 or 2 bytes from) the end, 
    Jaybird was reading less bytes than it should. This caused subsequent reads
    to read wrong data, reading too little or too much data.
-   New feature: Support for the v15 protocol (Firebird 3.0.2 and higher). ([JDBC-601](http://tracker.firebirdsql.org/browse/JDBC-601)) \
    The v15 protocol supports database encryption key callbacks during the
    authentication phase, supporting encrypted security databases. We decided to
    implement the v14 changes only as part of the v15 implementation. \
    See also [Database encryption support](#database-encryption-support).
-   New feature: Jaybird now supports UTF-8 URL encoding for connection
    properties in the JDBC url. ([JDBC-604](http://tracker.firebirdsql.org/browse/JDBC-604)) \
    This introduce a minor incompatibility, see also 
    [URL encoding in query part of JDBC URL](#url-encoding-in-query-part-of-jdbc-url). \
    This feature was back-ported to Jaybird 3.0.9.
-   New feature: Firebird 4 data type bind configuration support ([JDBC-603](http://tracker.firebirdsql.org/browse/JDBC-603)) \
    This change also removes the `timeZoneBind` and `decfloatBind` connection
    properties. This feature requires Firebird 4 beta 2 or snapshot 
    Firebird 4.0.0.1683 or higher. \
    See also [Firebird 4 data type bind configuration support](#firebird-4-data-type-bind-configuration-support). \
    This feature was partially back-ported to Jaybird 3.0.9.
-   New feature: Support for statement timeouts through `java.sql.Statement.setQueryTimeout`
    for the v16 protocol (Firebird 4 and higher) ([JDBC-602](http://tracker.firebirdsql.org/browse/JDBC-602)) \
    See also [Firebird 4 statement timeout support](#firebird-4-statement-timeout-support).
-   New feature: Support for zlib wire compression in the pure Java wire
    protocol implementation (Firebird 3 and higher) ([JDBC-606](http://tracker.firebirdsql.org/browse/JDBC-606)) \
    See also [Wire compression support](#wire-compression-support).
-   New feature: Support for JDBC escape `DAYNAME`, will always return day names
    in English ([JDBC-607](http://tracker.firebirdsql.org/browse/JDBC-607))
-   New feature: Support for JDBC escape `MONTHNAME`, will always return month
    names in English ([JDBC-608](http://tracker.firebirdsql.org/browse/JDBC-608))
-   New feature: Support for JDBC escape `DATABASE` ([JDBC-609](http://tracker.firebirdsql.org/browse/JDBC-609))

Known issues
============

-   Using a native connection with a Firebird 3 client library to a Firebird 2.5
    or older server may be slow to connect. The workaround is to specify the
    IPv4 address instead of the host name in the connection string, or to use a
    Firebird 2.5 or earlier `fbclient.dll`.
    
    This is caused by [CORE-4658](http://tracker.firebirdsql.org/browse/CORE-4658)

Support
=======

If you need support with Jaybird, join the [Firebird-Java Google Group](https://groups.google.com/g/firebird-java)
and mailing list. You can subscribe by sending an email to [firebird-java+subscribe@googlegroups.com](mailto:firebird-java+subscribe@googlegroups.com).

Looking for professional support of Jaybird? Jaybird is now part of the [Tidelift subscription](https://tidelift.com/subscription/pkg/maven-org-firebirdsql-jdbc-jaybird?utm_source=maven-org-firebirdsql-jdbc-jaybird&utm_medium=referral&utm_campaign=docs).

See also [Where to get help](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#where-to-get-help)

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

- [Wire encryption support](#wire-encryption-support) (back-ported to Jaybird 3.0.4)
- [Database encryption support](#database-encryption-support) (back-ported to Jaybird 3.0.4)
- [Wire compression support](#wire-compression-support)
- [Authentication plugin improvements](#authentication-plugin-improvements)
- [Firebird 4 data type bind configuration support](#firebird-4-data-type-bind-configuration-support) (since Jaybird 4.0.0-beta-2)
- [Firebird 4 DECFLOAT support](#firebird-4-decfloat-support)
- [Firebird 4 extended numeric precision support](#firebird-4-extended-numeric-precision-support)
- [Firebird 4 time zone support](#firebird-4-time-zone-support)
- [Firebird 4 statement timeout support](#firebird-4-statement-timeout-support) (since Jaybird 4.0.0-beta-2)
- [JDBC RowId support](#jdbc-rowid-support)
- [DatabaseMetaData getPseudoColumns implemented](#databasemetadata-getpseudocolumns-implemented)
- [DatabaseMetaData getVersionColumns implemented](#databasemetadata-getversioncolumns-implemented)
- [DatabaseMetaData getFunctions implemented](#databasemetadata-getfunctions-implemented) (since Jaybird 4.0.0-beta-2)
- [DatabaseMetaData getFunctionColumns implemented](#databasemetadata-getfunctioncolumns-implemented) (since Jaybird 4.0.0-beta-2)
- [Improved JDBC function escape support](#improved-jdbc-function-escape-support)
- [New JDBC protocol prefix jdbc:firebird:](#new-jdbc-protocol-prefix-jdbcfirebird)
- [URL encoding in query part of JDBC URL](#url-encoding-in-query-part-of-jdbc-url) (back-ported to Jaybird 3.0.9)
- [Generated keys support improvements](#generated-keys-support-improvements)
- [Operation monitoring](#operation-monitoring)

Upgrading from Jaybird 3 to 4 should be simple, but please make sure to read 
[Compatibility changes](#compatibility-changes) before using Jaybird 4. See also 
[Upgrading from Jaybird 3 to Jaybird 4](#upgrading-from-jaybird-3-to-jaybird-4).

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the [Firebird-java Google Group](https://groups.google.com/g/firebird-java) 
or reported on the issue tracker <https://github.com/FirebirdSQL/jaybird/issues/>.

Supported Firebird versions
---------------------------

Jaybird @VERSION_WO_TARGET@ was tested against Firebird 2.5.9, 3.0.10, and
4.0.2, but should also support other Firebird versions from 2.5 and up.

Formal support for Firebird 2.0 and 2.1 has been dropped (although in general we 
expect the driver to work). The Type 2 and embedded server JDBC drivers use JNA to
access the Firebird client or embedded library.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

### Notes on Firebird 3 support

Jaybird 4 adds support for the Firebird 3 zlib compression in the pure Java wire
protocol. The compression is disabled by default.

### Notes on Firebird 4 support

Jaybird 4 supports the protocol improvements of Firebird 4 for statement
timeouts, but does not implement the new batch protocol.

Jaybird time zone support uses functionality added after Firebird 4 beta 1 (4.0.0.1436), 
you will need version 4.0.0.1683 or later for the `dataTypeBind` connection 
property.

Jaybird 4 supports the extended numeric precision types introduced after
Firebird 4 beta 1 (4.0.0.1436), you will need version 4.0.0.1604 to be able to
use `NUMERIC` or `DECIMAL` with a precision higher than 18.

Jaybird does not support the ChaCha and ChaCha64 wire encryption plugins. This 
may be added in a future major or point release.

### Notes on Firebird 5 support

Jaybird 4 does not provide full support for Firebird 5.0. In general, it offers
support at the Firebird 4.0 feature set, but there are some caveats. 

Most notably, the generated-keys support will not work for DML statements that 
now use multi-row `RETURNING` (all DML except `insert ... values (..) returning ...`).
Full support for multi-row `RETURNING` with generated-keys will be provided in 
Jaybird 5. As a workaround, use `executeQuery()` with an explicit `RETURNING`
clause.

Supported Java versions
-----------------------

Jaybird 4 supports Java 7 (JDBC 4.1), Java 8 (JDBC 4.2), and Java 9 and higher 
(JDBC 4.3). Support for earlier Java versions has been dropped.

Given the limited support period for Java 9 and higher versions, we limit 
support to Java 8, 11, 17 and the most recent LTS version after Java 17 and 
the latest Java release. Currently, that means we support Java 8, 11, 17 and 19.

Jaybird 4 provides libraries for Java 7, Java 8 and Java 11. The Java 8 builds 
have the same source and all JDBC 4.3 related functionality and can be used on
Java 9 and higher as well.

Jaybird 4 is not modularized, but all versions declare the automatic module name 
`org.firebirdsql.jaybird`.

See als [Java support](#java-support) in [What's new in Jaybird 4](#whats-new-in-jaybird-4).

Specification support
---------------------

Jaybird supports the following specifications:

|Specification|Notes
|-------------|----------------------------------------------------------------
| JDBC 4.3    | All JDBC 4.3 methods for features supported by Firebird; Java 9 and higher supported using the Java 8 or Java 11 driver.
| JDBC 4.2    | All JDBC 4.2 methods for features supported by Firebird.
| JDBC 4.1    | All JDBC 4.1 methods for features supported by Firebird.
| JTA 1.0.1   | Implementation of `javax.transaction.xa.XAResource` interface via `XADataSource` implementation.

Getting Jaybird 4
=================

Jaybird @VERSION_WO_TARGET@
---------------------------

### Maven ###

Jaybird @VERSION_WO_TARGET@ is available from Maven central: 

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird`,\
Version: `@VERSION_SIMPLE@.javaXX@VERSION_TAG@` (where `XX` is `7`, `8` or `11`).

For ease of transition to the new artifact naming, we also provide a Maven
relocation artifact with artifact id `jaybird-jdkXX` (with `XX` is `17` or `18`).
However, we recommend switching to the `jaybird` artifact id.

NOTE: SNAPSHOT releases are only available from the Sonatype snapshot 
repository, <https://oss.sonatype.org/content/repositories/snapshots>

For example:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird</artifactId>
    <version>@VERSION_EXAMPLE@</version>
</dependency>
~~~

If your application is deployed to a Java EE application server, you will need to
exclude the `javax.resource:connector-api` dependency, and add it as a provided 
dependency:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird</artifactId>
    <version>@VERSION_EXAMPLE@</version>
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
explicitly include JNA 5.5.0 as a dependency:

~~~ {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.5.0</version>
</dependency>
~~~

For Windows and Linux, you can add the `org.firebirdsql.jdbc:fbclient`
dependency on your classpath to provide the native libraries for the `native` 
and `local` protocol. Be aware that this dependency does not support `embedded`.

See also [Type 2 (native) and embedded driver](#type-2-native-and-embedded-driver).

### Download ###

You can download the latest versions from <https://www.firebirdsql.org/en/jdbc-driver/>

At minimum Jaybird 4 requires `jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar` 
(where `XX` is `7`, `8` or `11`) and `connector-api-1.5.jar`. You can also use 
`jaybird-full-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar` which includes
the connector-api files.

If you deploy your application to a Java EE application server, then you must 
use `jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar` (not `-full`!), and **not**
include `connector-api-1.5.jar` as this dependency will be provided by your
application server.

For native, local or embedded support, you will need to include `jna-5.5.0.jar` 
on your classpath. See also [Type 2 (native) and embedded driver](#type-2-native-and-embedded-driver).

Upgrading from Jaybird 3 to Jaybird 4
=====================================

Please make sure to read [Compatibility changes](#compatibility-changes) and 
[Changes in artifact and library names](#changes-in-artifact-and-library-names)
before upgrading to Jaybird 4.

Maven
-----

Change the artifact id from `jaybird-jdkXX` to `jaybird`, and change the version
of the dependency to `@VERSION_SIMPLE@.javaXX@VERSION_TAG@` (where `XX` is your
Java version, `7` for Java 7, `8` for Java 8 and `11` for Java 11). If you use
native or embedded verify that you upgrade JNA (`net.java.dev.jna:jna`) from
4.4.0 to 5.5.0.

For more detailed instructions, see also the information on Maven in
[Getting Jaybird 4](#getting-jaybird-4). 

Manual install
--------------

If you manage your dependencies manually, you need to do the following:

1.  Replace the Jaybird 3 library with the Jaybird 4 version
    - `jaybird-3.0.x.jar` with `jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar`
    (where `XX` is `7`, `8` or `11`) 
    - `jaybird-full-3.0.x.jar` with `jaybird-full-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar`
    
2.  If installed, remove `antlr-runtime-4.7.jar` or `antlr-runtime-4.7.2.jar` 
    (unless of course your application itself needs these libraries). Since 
    Jaybird 4.0.8, Jaybird no longer relies on ANTLR.
    
3.  If installed, remove `jna-4.4.0.jar` and replace it with `jna-5.5.0.jar`.
    This library is only necessary for native, local or embedded connections.
    If you use pure-java connections (the default), you don't need JNA.
    
Gotchas
-------

If you find a problem while upgrading, or other bugs: please report it 
on <https://github.com/FirebirdSQL/jaybird/issues/>.

For known issues, consult [Known Issues](#known-issues).

What's new in Jaybird 4
=======================

For a full list of changes, see [Firebird tracker for Jaybird 4](https://github.com/FirebirdSQL/jaybird/issues?q=label%3A%22fix-version%3A+Jaybird+4%22).

Changes in artifact and library names
-------------------------------------

Historically, the naming of Jaybird artifacts and libraries has been a bit
inconsistent. With the rapid release cycle of Java, naming collisions are
imminent with the old naming convention. For example, the Maven artifact 
`jaybird-jdk15` was used for Java 1.5 and with this naming convention, this 
would be reused for Java 15.

Forced by this issue, we have overhauled the naming convention entirely to bring
more consistency between Maven artifacts and the Jaybird zip distribution. The
full naming convention is documented in [jdp-2019-04: Version Number and Naming Scheme](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2019-04-version-number-and-naming-scheme.md).

This new naming convention has been changed compared to the one from Jaybird
4.0.0-beta-1.

This new naming convention has the following effects:

-   The targeted Java version is no longer part of the Maven artifact id. The
    artifact id is now `jaybird` for all Java versions. We provide a relocation
    artifact for `jaybird-jdk17` and `jaybird-jdk18` for backwards compatibility.
-   The targeted Java version is now part of the version (eg `4.0.0.java11`)
-   Names of libraries in the distribution zip are now consistent with the Maven
    naming convention

As a result of these new naming conventions, the following has been changed:

-   Maven artifact: `jaybird` (for all Java versions) (was `jaybird-jdk18`)
-   Maven version: `4.0.0.java8` (was `3.0.5`) 
-   Distribution zip: `jaybird-4.0.0.java8.zip` (was `Jaybird-3.0.5_JDK1.8.zip`)
-   Jaybird: `jaybird-4.0.0.java8.jar` (was `jaybird-3.0.5.jar` in zip 
    distribution)
-   Jaybird (full): `jaybird-full-4.0.0.java8.jar` (was 
    `jaybird-full-3.0.5.jar`)
-   Jaybird sources: `jaybird-4.0.0.java8-sources.jar` (was 
    `jaybird-3.0.5-sources.jar` in zip distribution)
-   Jaybird javadoc: `jaybird-4.0.0.java8-javadoc.jar` (was
    `jaybird-3.0.5-javadoc.jar` in zip distribution)
    
Furthermore, the client name reported to Firebird 2.5 and higher has been 
changed from `Jaybird 3.0.5-JDK_1.8` to `Jaybird jaybird-4.0.0.java8` 

Java support
------------

### Java 7 ###

The driver supports Java 7 with caveats.
 
-   Firebird 4 time zone types are not supported under Java 7, see also
    [Firebird 4 time zone support](#firebird-4-time-zone-support).

-   Under Java 7, Jaybird requires JAXB (`javax.xml.bind`), this will work in
    standard Java, but may require additional configuration in certain 
    environments, for example JBoss/Wildfly.
    
-   Some libraries used for testing Jaybird have upped their minimum 
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
recommend using the Java 11 driver, though its sources are identical to the 
Java 8 driver. 

We recommend not to use the Java 7 version of Jaybird with Java 9 or higher. The 
Java 7 version doesn't implement all JDBC 4.3 features that are implemented in
the Java 8 version. In addition, since Jaybird 3.0.4, the Java 7 version of
Jaybird needs the `java.xml.bind` module, where the Java 8 and higher versions
do not need that module.

Given the limited support period for Java 9 and higher versions, we limit 
support to Java 8, 11, 17 and the most recent LTS version after Java 17 and 
the latest Java release. Currently, that means we support Java 8, 11, 17 and 19.

For compatibility with Java 9 modules, Jaybird defines the automatic module name 
`org.firebirdsql.jaybird`. This guarantees a stable module name for Jaybird, and 
allows for future modularization of Jaybird.

Firebird support
----------------

Support for Firebird 2.0 and 2.1 has been dropped. See [Firebird 2.0 and 2.1 no
longer supported](#firebird-2.0-and-2.1-no-longer-supported) for details.

Firebird versions 2.5, 3.0 and 4.0 are supported.

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

The new ChaCha and ChaCha64 wire encryption introduced in Firebird 4 is not yet 
supported.

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

In addition, version 15 of the protocol (Firebird 3.0.2 and higher) was also
implemented, supporting encryption callbacks during authentication for use with
encrypted security databases.

The current implementation is simple and only supports replying with a static 
value from a connection property. Be aware that a static value response for 
database encryption is not very secure as it can easily lead to replay attacks 
or unintended key exposure. 

Future versions of Jaybird (likely 5) will introduce plugin support for database
encryption plugins that require a more complex callback.

The static response value of the encryption callback can be set through the 
`dbCryptConfig` connection property. `DataSource` and `ServiceManager` 
implementations have an equivalent property with the same name. This 
property can be set as follows:

-   Absent or empty value: empty response to callback (depending on the database 
    encryption plugin this may just work or yield an error later).
-   Strings prefixed with `base64:`: rest of the string is decoded as base64 to 
    bytes. The `=` padding characters are optional, but when present they must
    be valid (that is: if you use padding, you must use the right number of 
    padding characters for the length). \
    When the base64 encoded value contains `+`, it must be escaped as `%2B` in
    the JDBC URL. For backwards compatibility with Jaybird 3, we can't switch to
    the URL-safe variant of base64.
-   Plain string value: string is encoded to bytes using UTF-8, and these bytes
    are used as the response. Avoid use of `:` in the plain string value.
    
Because of the limitation of connection URL parsing, we strongly suggest
avoiding plain string values with `&` or `;`. Likewise, avoid `:` so that we can
support other prefixes similar to `base64:` in the future. If you need these 
characters, consider using a base64 encoded value instead, or ensure these
characters are URL encoded: `&`: `%26`, `;`: `%3B`, `:`: `%3A`, `%`: `%25`,
`+`: `%2B`.

For service operations, as implemented in the `org.firebirdsql.management` 
package, Firebird requires the `KeyHolderPlugin` configuration to be globally 
defined in `firebird.conf`. Database-specific configuration in `databases.conf` 
will be ignored for service operations. Be aware that some service operations on 
encrypted databases are not supported by Firebird 3 (eg `gstat` equivalents 
other than `gstat -h` or `gstat -e`).                  

Other warnings and limitations

-   Database encryption callback support is only available in the pure Java
    implementation. Support for native and embedded connections may be added
    in a future version.
-   The database encryption callback does not require an encrypted connection, 
    so the key can be exchanged unencrypted if wire protocol encryption has been 
    disabled client-side or server-side, or if legacy authentication is used. 
    Consider setting connection property `wireCrypt=REQUIRED` to force 
    encryption (caveat: see the next point).
-   Firebird may ask for the database encryption key before the connection has
    been encrypted (for example if the encrypted database itself is used as the
    security database). _This applies to v15 and higher protocol support._
-   We cannot guarantee that the `dbCryptConfig` value cannot be obtained by 
    someone with access to your application or the machine hosting your 
    application (although that in itself would already imply a severe security 
    breach).
    
Wire compression support
------------------------

Support for zlib wire compression was added in the pure Java wire protocol.
Compression can be enabled using boolean connection property `wireCompression`.

The connection property only affects the pure Java wire protocol connections on
Firebird 3 and higher, if the server has the zlib library. Native connections
will follow the `WireCompression` configuration in the `firebird.conf` read by
the client library, if the zlib library is on the search path.

Compression is currently disabled by default. We may change this in future 
versions of Jaybird to be enabled by default.

The `wireCompression` property is also available on data sources and the
management classes in `org.firebirdsql.management`.
    
Authentication plugin improvements
----------------------------------

Jaybird 4 has added support for the new `SrpNNN` (with NNN is 224, 256, 384 
and 512) authentication plugins added in Firebird 4 (back-ported to Firebird 
3.0.4 for Srp256 only).

The original `Srp` plugin uses SHA-1, the new Srp-variants use SHA-224, SHA-256,
SHA-384 and SHA-512 respectively[^srpHash].

[^srpHash]: Internally `SrpNNN` continues to use SHA-1, only the client-proof 
applies the SHA-NNN hash. See also [firebird#6051/CORE-5788](https://github.com/FirebirdSQL/firebird/issues/6051).

Be aware, support for these plugins depends on support of these hash algorithms 
in the JVM. For example, SHA-224 is not supported in Oracle Java 7 by default 
and may require additional JCE libraries.

### Default authentication plugins ###

The default plugins applied by Jaybird are now - in order - `Srp256`, `Srp`. 
This applies only for the pure Java protocol and only when connecting to 
Firebird 3 or higher. The native implementation will use its own default, or the 
value configured through its `firebird.conf`. 

When connecting to Firebird 3 versions earlier than 3.0.4, or if `Srp256` has 
been removed from the `AuthServer` setting in Firebird, this might result in 
slower authentication because more roundtrips to the server are needed. After 
the attempt to use `Srp256` fails, authentication continues with `Srp`.

To avoid this, consider explicitly configuring the authentication plugins to 
use, see [Configure authentication plugins](#configure-authentication-plugins)
for details.

When connecting to Firebird 3 or higher, the pure Java protocol in Jaybird will 
no longer try the `Legacy_Auth` plugin by default as it is an unsafe 
authentication mechanism. We strongly suggest to use SRP users only, but if you 
really need to use legacy authentication, you can specify connection property 
`authPlugins=Legacy_Auth`, see [Configure authentication plugins](#configure-authentication-plugins)
for details.

Firebird 2.5 and earlier are not affected and will always use legacy 
authentication.

### Configure authentication plugins ###

Jaybird 4 introduces the connection property `authPlugins` (alias 
`auth_plugin_list`) to specify the authentication plugins to try when 
connecting. The value of this property is a comma-separated[^authPluginSeparator] 
list with the plugin names.

[^authPluginSeparator]: The `authPlugins` values can be separated by commas, 
spaces, tabs, or semi-colons. The semi-colon should not be used in a JDBC URL as 
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
    unstable; they may change with point releases (although we will try to avoid 
    that) 
-   For now, it will be necessary for the jar containing the authentication 
    plugin to be loaded by the same class loader as Jaybird itself

If you implement a custom authentication plugin and run into problems, contact 
us on the Firebird-Java mailing list.

If you use a native connection, check the Firebird documentation how to add
third-party authentication plugins to fbclient.

Firebird 4 data type bind configuration support
-----------------------------------------------

Firebird 4 (build 4.0.0.1683 or later) introduced the `SET BIND` statement and 
`isc_dpb_set_bind` DPB item. This allows you to define data type conversion
rules for compatibility or ease of processing data.

This feature is specifically necessary for using the `WITH TIME ZONE` types
under Java 7. See also [Time zone bind configuration](#time-zone-bind-configuration).

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
configuration, encoding the semicolon is not necessary and will result in errors.

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
`decfloatBind` from earlier Jaybird 4 beta or snapshot versions. The
`timeZoneBind` and `decfloatBind` properties are no longer supported. 

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
digits, and an exponent[^decimalFormat] between -398 and 369 (`DECFLOAT(16)`), or 
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
    exponent and preserving sign, e.g. for `DECFLOAT(16)`, the value will become 
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
    and exponent = 384 - 15 = 369, and these values for coefficient and exponent
    are in range of the storage requirements.
    
        The resulting value is now `1000000000000000E+369` or `1.000000000000000E+384`,
    or in other words, we 'increased' precision by adding zeroes as 
    least-significant digits to make the value fit.
    
    2.  Otherwise, we have what is called an overflow, and an `SQLException` is 
    thrown as the value is out of range.
    
If you need other rounding and overflow behavior, make sure you round the values
appropriately before you set them.

### Configuring decfloat traps and rounding

To configure the server-side(!) error and rounding behaviour of the `DECFLOAT`
data types, you can configure use the following connection properties:

-   `decfloatRound` (alias: `decfloat_round`) \
    Possible values: `ceiling`, `up`, `half_up` (default), `half_even`,
    `half_down`, `down`, `floor`, `reround`
-   `decfloatTraps` (alias: `decfloat_traps`) \
    Comma-separated list with options: `Division_by_zero` (default), `Inexact`,
    `Invalid_operation` (default), `Overflow` (default), `Underflow`
    
Configuring these options does not change driver behaviour, only server-side
behaviour.
     
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
    may be removed in future Firebird 4 snapshots, or may be disabled by default.

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
may throw a `SQLException` instead, see also related note 9.
 
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
`10E-1`) etc will be **`false`**. 

    This behavior may change in the future and only allow `0` for `false` and 
exactly `1` for `true` and throw an `SQLException` for all other values, or 
maybe `true` for everything other than `0`. In general, we advise to not use 
numerical types for boolean values, and especially not to retrieve the result of 
a calculation as a boolean value. Instead, use a real `BOOLEAN`.

12. Setting values as `String` is supported following the format rules of 
`new BigDecimal(String)`, with extra support for special values `+NaN`, `-NaN`, 
`+sNaN`, `-sNaN`, `+Infinity` and `-Infinity` (case-insensitive). Other 
non-numerical strings throw an `SQLException` with a `NumberFormatException` as 
cause. Out of range values are handled as described in [Precision and range](#precision-and-range).

13. Getting values as `String` will be equivalent to `BigDecimal.toString()`,
with extra support for the special values mentioned in the previous note.

14. As mentioned in earlier notes, support for the special values is under
discussion, and may be removed or change in future versions of Jaybird 
and/or Firebird.

15. Getting as `BigInteger` will behave as `BigDecimal.toBigInteger()`, which
discards the fractional part (rounding by truncation), and may add 
`(-1 * scale - precision)` least-significant zeroes if the scale exceeds
precision. Be aware that use of `BigInteger` for large values may result in 
significant memory consumption. 

16. Setting as `BigInteger` will lose precision for values with more digits than
the target type. It applies the rules described in [Precision and range](#precision-and-range).

17. Values can also be set and retrieved as types `Decimal32`, `Decimal64` and 
`Decimal128` from the `org.firebirdsql.extern.decimal` package. Where `Decimal64`
exactly matches the `DECFLOAT(16)` protocol format, and `Decimal128` the 
`DECFLOAT(34)` protocol format. Be aware that this is an implementation detail
that might change in future Jaybird versions (both in terms of support for these 
types, and in terms of the interface (API) of these types).

18. Setting a `Decimal128` on a `DECFLOAT(16)`, or a `Decimal32` on a 
`DECFLOAT(16)` or `DECFLOAT(34)`, or retrieving a `Decimal32` from
a `DECFLOAT(16)` or `DECFLOAT(34)`, or a `Decimal64` from a `DECFLOAT(34)`
will apply the rules described in [Precision and range](#precision-and-range).

19. Zero values can have a sign (eg `-0` vs `0` (`+0`)), this can only be 
set or retrieved using `String` or the `DecimalXX` types, or the result of 
rounding. This behaviour is subject to change, and future releases may 'round' 
to `0` (aka `+0`).

Firebird 4 extended numeric precision support
---------------------------------------------

Added support for the extended precision for `NUMERIC` and `DECIMAL` introduced 
in Firebird 4, increasing the maximum precision to 38. In the implementation in 
Firebird, this extended precision is backed by an Int128.

Any `NUMERIC` or `DECIMAL` with a precision between 19 and 38 will allow storage
up to a precision of 38 (technically even 39, but not full range). 

Values set on a field or parameter will be rounded to the target scale of the 
field using `RoundingMode.HALF_UP`. Values exceeding that do not fit in an 
Int128 after rounding will be rejected with a `TypeConversionException`.

### Important notice about extended numeric precision support ###

The implementation of extended numeric precision was changed after Firebird
4.0.0-beta-1 (in build 1604) from a maximum precision of 34 backed by a 
Decimal128 to a maximum precision of 38 backed by an Int128. 

Support for the old 'DEC_FIXED' format backed by a Decimal128 was removed in
Jaybird 4.0.0-beta-2.

Firebird 4 INT128 support
-------------------------

Since: Jaybird 4.0.1

After Firebird 4 beta 2 (in build 4.0.0.2076), the Int128 type used to back the
extended precision numerics was exposed as SQL type `INT128`. As JDBC does not
provide this type, we map it to JDBC type `NUMERIC`, with a reported precision
of 38 and a scale of 0.

We think this mapping will offer the best support in tool and libraries. For
further details, see [jdp-2020-08 Int128 support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-08-int128-support.md)

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
`dataTypeBind`, for more information see [Time zone bind configuration](#time-zone-bind-configuration).

See also [jdp-2019-03: Time Zone Support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2019-03-time-zone-support.md)  

### Scope of time zone support ###

JDBC 4.2 introduced support for time zones, and maps these types to 
`java.time.OffsetTime` and `java.time.OffsetDateTime`. JDBC does not define
explicit setters for these types. Use `setObject(index, value)`,
`updateObject(index, value)`, `getObject(index/name)` or 
`getObject(index/name, classType)`.

Firebird 4 supports both offset and named time zones. Given the definition in
JDBC, Jaybird defaults to offset time zones. On retrieval of a value with a
named zone, Jaybird will make a best-effort attempt to convert to the equivalent
offset using Java's time zone information. If no mapping is available, the time
will be returned at UTC (offset zero).

Since Jaybird 4.0.1, it is also possible to get and set 
`java.time.ZonedDateTime`, which preserves the named zone information.

Jaybird 4 supports the following Java types on fields of time zone types (those
marked with * are not defined in JDBC)

`TIME WITH TIME ZONE`:

- `java.time.OffsetTime` (default for `getObject`)
  - On get, if the value is a named zone, it will derive the offset using the 
    base date 2020-01-01 (in 4.0.0 it used the current date). The offset can be
    different from the offset of the `OffsetDateTime` for the same value.
- `java.time.OffsetDateTime`
  - On get, the current date is added
    - For a named zone, the time in the zone is derived at 2020-01-01 and then
      rebased to the current date. As a result, the offset can be different from
      an `OffsetTime`.
  - On set the date information is removed
- `java.time.ZonedDateTime` (\*)
  - On get, the time in the zone is derived at 2020-01-01 and then rebased to the
    current date.
  - On set, the time is rebased to 2020-01-01 and then the date information is
    removed.
- `java.lang.String`
  - On get, applies `OffsetTime.toString()` (eg `13:25:13.1+01:00`)
  - On set, tries the default parse format of either `OffsetTime` or 
    `OffsetDateTime` (eg `13:25:13.1+01:00` or `2019-03-10T13:25:13+01:00`)
    and then sets as that type
- `java.sql.Time` (\*)
  - On get, obtains `java.time.OffsetDateTime`, converts this to epoch 
    milliseconds and uses `new java.sql.Time(millis)`
  - On set, applies `toLocalTime()`, combines this with `LocalDate.now()`
    and then derives the offset time for the default JVM time zone
- `java.sql.Timestamp` (\*)
  - On get, obtains `java.time.OffsetDateTime`, converts this to epoch 
    milliseconds and uses `new java.sql.Timestamp(millis)`
  - On set, applies `toLocalDateTime()` and derives the offset time for the 
    default JVM time zone
  
`TIMESTAMP WITH TIME ZONE`:

- `java.time.OffsetDateTime` (default for `getObject`)
- `java.time.OffsetTime` (\*)
  - On get, the date information is removed
  - On set, the current date is added
- `java.time.ZonedDateTime` (\*)
- `java.lang.String`
  - On get, applies `OffsetDateTime.toString()` (eg `2019-03-10T13:25:13.1+01:00`)
  - On set, tries the default parse format of either `OffsetTime` or 
    `OffsetDateTime` (eg `13:25:13.1+01:00` or `2019-03-10T13:25:13+01:00`)
    and then sets as that type
- `java.sql.Time` (\*)
  - On get, obtains `java.time.OffsetDateTime`, converts this to epoch 
    milliseconds and uses `new java.sql.Time(millis)`
  - On set, applies `toLocalTime()`, combines this with `LocalDate.now()`
    and then derives the offset date time for the default JVM time zone
- `java.sql.Timestamp` (\*)
  - On get, obtains `java.time.OffsetDateTime`, converts this to epoch 
    milliseconds and uses `new java.sql.Timestamp(millis)`
  - On set, applies `toLocalDateTime()` and derives the offset date time for the 
    default JVM time zone
- `java.sql.Date` (\*)
  - On get, obtains `java.time.OffsetDateTime`, converts this to epoch 
    milliseconds and uses `new java.sql.Date(millis)`
  - On set, applies `toLocalDate()` at start of day and derives the offset date 
    time for the default JVM time zone
  
In addition, Firebird 4 has 'bind-only' data types `EXTENDED TIME/TIMESTAMP WITH
TIME ZONE`. These data types can be set through the data type bind configuration
and include an extra offset in its data so clients without access to ICU or
other time zone data can use the offset as determined by Firebird.

Jaybird provides minimal support for these types by handling them the same as
the normal `WITH TIME ZONE` types. That means the extra offset information is
ignored and Jaybird will always use the Java time zone information to calculate
the offset of a named zone, and if a zone is unknown in Java, Jaybird will
fallback to UTC even when the actual offset is available in the 'extended' time
zone type.

See also:
 
- [jdp-2020-01: Extended Time Zone Types Support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-01-extended-time-zone-types-support.md)
- [jdp-2020-06: OffsetTime derivation for named zone](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-06-offsettime-derivation-for-named-zone.md)
- [jdp-2020-09: Add ZonedDateTime support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-09-add-zoneddatetime-support.md) 

#### Support for legacy JDBC date/time types ####

For the `WITH TIME ZONE` types, JDBC does not define support for the legacy JDBC 
types (`java.sql.Time`, `java.sql.Timestamp` and `java.sql.Date`). To ease the 
transition and potential compatibility with tools and libraries, Jaybird does
provide support. However, we strongly recommend avoiding using these types. 

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

Jaybird also does not support non-standard extensions like `java.time.Instant`.
If there is interest, we may add them in the future. 

### Time zone bind configuration ###

If you are using Java 7 and need to handle the `WITH TIME ZONE` types, you will
need to redefine the data type binding for the time zone types as the necessary
`java.time` types do not exist in Java 7. Redefining the binding can also be
used for tools or applications that expect `java.sql.Time`/`Timestamp` types and
cannot use the `java.time.OffsetTime`/`OffsetDateTime` types returned for the
`WITH TIME ZONE` types.

To redefine the data type binding, you can use the connection property
`dataTypeBind`. See [Firebird 4 data type bind configuration support](#firebird-4-data-type-bind-configuration-support)
for details.

You will need to map the time zone types, either to `legacy` (which uses `time`
and `timestamp` (without time zone)) or to a desired target data type (for
example `time` or `varchar`). You can map per type or for both types together:

```
Properties props = new Properties();
props.setProperty("dataTypeBind", "time zone to legacy");
```

The `TIME ZONE TO EXTENDED` binds (including type-specific variants) is only
supported under Java 8 and higher using the Java 8 or higher version of Jaybird.
As mentioned earlier, the support for 'extended' time zone types will behave
identical to the normal time zone types. 

**Important**: These features requires Firebird 4 beta 2 or higher (or a snapshot
build version 4.0.0.1683 or later). It will be ignored in builds before 1481 as
the necessary database parameter buffer item does not exist, and it will raise
an error in versions between 1481 and 1682 as there the DPB item points to the
removed DPB item `isc_time_zone_bind`.

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

Valid values are time zone names known by Firebird, we recommend using the long
names (eg `Europe/Amsterdam`), and not the ambiguous short IDs (eg `CET`). 
Although not required, we recommend to use time zone names that are known by 
Firebird and Java (see [Session time zone for conversion](#session-time-zone-for-conversion)
for caveats).

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
to the legacy JDBC date/time types: the offset date/time is converted to epoch
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
    
-   As `CURRENT_TIME` uses the session time zone, which usually is a named zone,
    use in combination with `java.time.OffsetTime` can yield confusing results.
    For example, if the current date and time is '2020-07-01T14:51:00 
    Europe/Amsterdam', then retrieving `CURRENT_TIME` as an `OffsetTime` will
    return the value '14:51:00+01:00', and not '14:51:00+02:00'. \
    It is recommended to avoid `CURRENT_TIME` and use `CURRENT_TIMESTAMP`
    instead.

-   Overall, using `TIME WITH TIME ZONE` with named zones is rather fragile and
    prone to interpretation errors. This is a result of how this is implemented
    in Firebird: values are stored at UTC with their offset or named zones,
    where derivation of the time in the named zone needs to use 2020-01-01 as
    the date for the time zone rules to apply. \
    We recommend avoiding `TIME WITH TIME ZONE` where possible.
    
Firebird 4 statement timeout support
------------------------------------

Support was added for Firebird 4 statement timeouts through 
`java.sql.setQueryTimeout`. On Firebird 3 and earlier or a native connection
with a Firebird 3 or earlier client library, the timeout is silently ignored.

This implementation supports a maximum timeout of 4294967 seconds. Larger values
will be handled as if `0` (unlimited) was set. Firebird also has attachment
level and global statement timeouts. This configuration governs the statement
level statement timeout only. In practice, a more stringent timeout might be
applied by this attachment level or global statement timeout.

**Important**: Query timeouts in Firebird 4 and higher have an important caveat:
for result set producing statements, the timeout covers the time from execution
start until the cursor is closed. This includes the time that Firebird waits for
the application to fetch more rows. This means that if you execute a SELECT and
take your time processing the results, the statement may be cancelled even when
Firebird itself returns rows quickly.

See Firebird 4 release notes and documentation for more information.

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

Unfortunately, this support does not extend to parameters, as parameters (e.g. 
in `where RDB$DB_KEY = ?`) cannot be distinguished from parameters of a normal 
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
name `RDB$DB_KEY` (case-insensitive) to `DB_KEY` as Firebird automatically 
applies this alias for the `RDB$DB_KEY` column(s) in a select-list.

Be aware that result set metadata will still report `DB_KEY` as the column name 
and label.

DatabaseMetaData getPseudoColumns implemented
---------------------------------------------

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

DatabaseMetaData getVersionColumns implemented
----------------------------------------------

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

DatabaseMetaData getFunctions implemented
-----------------------------------------

The `DatabaseMetaData.getFunctions` method has now been implemented.

The JDBC API specifies this method as:

> Retrieves a description of the system and user functions available in the
> given catalog.

The implementation only returns functions that are available from
the `RDB$FUNCTIONS` table. This means that the built-in functions are not
included in the result of this method.

For Firebird 3 and higher, the result includes native UDF, PSQL and UDR
functions. The result does not include functions defined in packages as JDBC
does not provide support for packages.

Jaybird provides additional columns with Firebird specific information. As these
columns are not defined by JDBC, they may change position when JDBC adds new
columns. We recommend retrieving these columns by name.

The additional columns are:

-  `JB_FUNCTION_SOURCE` - Source of Firebird 3+ PSQL function, this is the part
after the `AS` clause.
-  `JB_FUNCTION_KIND` - Kind of function, one of `"UDF"`, `"PSQL"` (Firebird 3+)
or `"UDR"` (Firebird 3+)
-  `JB_MODULE_NAME` - Value of `RDB$MODULE_NAME`, is `null` for PSQL functions
- `JB_ENTRYPOINT` - Value of `RDB$ENTRYPOINT`, is `null` for PSQL functions
- `JB_ENGINE_NAME` - Value of `RDB$ENGINE_NAME`, is `null` for UDF and PSQL
functions

DatabaseMetaData getFunctionColumns implemented
-----------------------------------------------

The `DatabaseMetaData.getFunctionColumns` method has now been implemented.

The JDBC API specifies this method as:

> Retrieves a description of the given catalog's system or user function
> parameters and return type.

The implementation only returns columns of functions that are available from
the `RDB$FUNCTIONS` table. This means that the built-in functions are not
included in the result of this method.

For Firebird 3 and higher, the result includes native UDF, PSQL and UDR
functions. The result does not include functions defined in packages as JDBC
does not provide support for packages.

Where Firebird provides no column name, Jaybird generates one by combining 
the string `PARAM_` with the value of `RDB$ARGUMENT_POSITION`. Names are not
available for the parameters of legacy UDF functions, and for the return value
of any function.

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
-   `DAYNAME(date)` - A character string representing the day component of
    _date_; implemented to always return English day names (ie Sunday, Monday,
    etc)
-   `MONTHNAME(date)` - A character string representing the month component of
    _date_; implemented to always return English month names (ie January,
    February, etc)
-   `DATABASE()` Name of the database; implemented as
    `RDB$GET_CONTEXT('SYSTEM', 'DB_NAME')`

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
-   `CONVERT(value, SQLtype)` - See [Improved CONVERT support](#improved-convert-support).

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
    
URL encoding in query part of JDBC URL
--------------------------------------

Jaybird now supports UTF-8 URL encoded values (and keys) in the query part of
the JDBC URL.

As a result of this change, the following previously unsupported characters can
be used in a connection property value when escaped:

- `;` escaped as `%3B`
- `&` escaped as `%26`

URL encoding can also be used to encode any unicode character in the query
string. Jaybird will always use UTF-8 for decoding.

This change introduces the following backwards incompatibilities:

- `+` in the query part now means _space_ (0x20), so occurrences of `+` (_plus_)
need to be escaped as `%2B`; make sure to do this for base64 encoded values of
`dbCryptConfig`
- `%` in the query part now introduces an escape, so occurrences 
of `%` (_percent_) need to be escaped as `%25`

Invalid URL encoded values will now throw a `SQLNonTransientConnectionException`.

The reason for this change is that the new `dataTypeBind` connection property
requires semicolon-separated values, but Jaybird supports semicolon-separated
key/value connection properties in the query part. To be able to support this
new property in the connection string, we had to introduce URL encoding.

This change only applies to the JDBC URL part after the first `?`. This change
does not apply to connection properties set through `java.util.Properties` or on
a `javax.sql.DataSource`.
    
Generated keys support improvements
-----------------------------------

Support for generated keys generation was improved with the following changes.

### Configuration of generated keys behaviour ###

A new connection property `generatedKeysEnabled` (alias `generated_keys_enabled`)
has been added that allows the behaviour of generated keys support to be 
configured. Also available on data sources.

This property supports the following values (case-insensitive):

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
will behave as `ignored` (e.g. using `generatedKeysEnabled=disable` instead of 
`disabled` will behave as `ignored`).

#### Selectively enable statement types ####

This last option allows you to selectively enable support for generated keys.
For example, `generatedKeysEnabled=insert` will only enable it for `insert` 
while ignoring it for all other statement types. Statement types that are not 
enabled will behave as if they generate no keys and will execute normally. For 
these statement types, `Statement.getGeneratedKeys()` will return an empty 
result set.

Possible statement type values (case-insensitive) are:

- `insert`
- `update`
- `delete`
- `update_or_insert`
- `merge`

Invalid values will be ignored. If none of the specified statement types are 
supported by Firebird, it will behave as `ignored`[^generated15].

[^generated15]: This is not the case for the unsupported Firebird 1.0 and 1.5
versions. There this will behave similar to `disabled`, and you will need to
explicitly specify `ignored` instead to get this behaviour.

Some examples:

- `jdbc:firebird://localhost/testdb?generatedKeysEnabled=insert` will only 
enable insert support
- `jdbc:firebird://localhost/testdb?generatedKeysEnabled=merge` will only 
enable merge support, but only on Firebird 3 and higher, for Firebird 2.5 this 
will behave as `ignored` given the lack of `RETURNING` support for merge in that 
version.
- `jdbc:firebird://localhost/testdb?generatedKeysEnabled=insert,update` will 
only enable insert and update support

This feature can be used to circumvent issues with frameworks or tools that 
always use generated keys methods for prepare or execution. For example, with 
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
[Configuration of generated keys behaviour](#configuration-of-generated-keys-behaviour)
above.

### Other behavioural changes to generated keys ###

See [Changes to behaviour of generated keys](#changes-to-behaviour-of-generated-keys)
in [Stricter JDBC compliance](#stricter-jdbc-compliance). 

Operation monitoring
--------------------

**Experimental feature**

Operation monitoring is an experimental feature that allows an application to
monitor and - in a limited fashion - control driver operations. This feature is
exposed as the interfaces `org.firebirdsql.gds.ng.monitor.OperationAware` and
`org.firebirdsql.gds.ng.monitor.Operation`.

An application can implement `OperationAware` and register it through 
`org.firebirdsql.gds.ng.OperationMonitor.initOperationAware(OperationAware)`.
When a `SecurityManager` is active, registering an `OperationAware` will
require the `SQLPermission` with name `org.firebirdsql.jaybird.initOperationAware`.
Only one instance can be registered at a time. Setting `null` will clear the
current instance.

Once registered, the `OperationAware` instance will be notified of operation
start and end through the methods `startOperation(Operation)` and 
`endOperation(Operation)`. These methods are called on the thread performing the
operation, so implementations must complete these methods as soon as possible to
prevent performance problems in the driver.

The `Operation` instance exposes the type of operation through `getType()` and
allows the operation to be cancelled. Cancellation is only possible as long as
the operation is active. Attempting to cancel when the operation is complete
will throw an `SQLException`.

This feature is experimental and its API may be removed or changed, and
operation types may be added or removed in point releases.

See also [jdp-2019-06: Ability to monitor driver operations](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2019-06-ability-to-monitor-driver-operations.md)

This feature was contributed by [Vasiliy Yashkov](https://github.com/vasiliy-yashkov).

New parser for generated keys handling {#generated-keys-parser-replaced}
--------------------------------------

Added in Jaybird 4.0.8, backported from Jaybird 5.

The "`generated keys`" parser has been replaced. This parser is used to detect
statement types, the table name, and presence or absence of a `RETURNING` 
clause. The new parser has no external dependencies, so Jaybird no longer 
depends on the ANTLR runtime (`org.antlr:antlr4-runtime`).

As a result of this change, it is possible that detection of some statements has
changed, especially detection of the presence of a `RETURNING` clause. Please 
report any incorrect changes in detection on https://groups.google.com/g/firebird-java[the firebird-java list] 
or on https://github.com/FirebirdSQL/jaybird/issues.

If you were relying on disabling generated keys support by excluding 
the antlr4-runtime library from the classpath, you will now need to explicitly 
disable it. Disabling generated keys can be done using the connection property 
`generatedKeysEnabled` with value `disabled`, or `ignored` if you don't want 
an exception thrown when calling a generated-keys-related execute or prepare 
method.

This backport was prompted by the fact that Jaybird 4 still used ANTLR 4.7.2, 
while ANTLR 4.10 has introduced a backwards incompatible change, which, for 
example, causes problems when using Jaybird with Hibernate 6. Instead of 
upgrading ANTLR, we decided that replacing the parser was less invasive as it
was less likely to introduce compatibility issues for users depending on older
versions of ANTLR.

Potentially breaking changes
----------------------------

Jaybird 4 contains a number of changes that might break existing applications.

See also [Compatibility changes](#compatibility-changes) for details.

Other fixes and changes
-----------------------

-   The distribution zip no longer includes the jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.rar.
This file was an example JCA Resource Archive.

    JCA support will be removed entirely in Jaybird 5. See also [Dropping JCA support](#dropping-jca-support).

-   Added support for Firebird 4 page size 32768 (32KB) in `FBManager` and backup 
managers (back-ported to Jaybird 3.0.5) ([JDBC-468](http://tracker.firebirdsql.org/browse/JDBC-468))

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
    
-   Upgraded jna library used for native/embedded from 4.4 to 5.5 ([JDBC-509](http://tracker.firebirdsql.org/browse/JDBC-509)

    The pull request to upgrade (from 4.4 to 5.2) was contributed by [Julien Nabet](https://github.com/serval2412).
    
-   Native libraries will now be disposed on application exit ([JDBC-519](http://tracker.firebirdsql.org/browse/JDBC-519))

    On JVM exit or - if deployed inside a WAR - servlet context destroy (tested 
on Tomcat), Jaybird will call `fb_shutdown` on any loaded native libraries and 
dispose the JNA handle to the native library. This should prevent crashes (e.g. 
access violation / 0xc0000005 error on Windows) on library unload if there were 
still embedded connections open.

    Given the potential for bugs or timing issues with this feature, it can be 
disabled with system property `org.firebirdsql.nativeResourceShutdownDisabled` 
set to `true`. This property must be set before Jaybird is loaded, preferably
on the Java command line.

Removal of deprecated classes and packages
------------------------------------------

See [Removal of deprecated classes, packages and methods](#removal-of-deprecated-classes-packages-and-methods)
in [Compatibility changes](#compatibility-changes) for more details.

Compatibility changes
=====================

Jaybird 4 introduces some changes in compatibility and announces future
breaking changes.

**The list might not be complete, if you notice a difference in behavior that is
not listed, please report it as bug.** It might have been a change we forgot to
document, but it could just as well be an implementation bug.

Firebird 2.0 and 2.1 no longer supported
----------------------------------------
<!-- For GitHub markdown compatibility -->
<a name="firebird-2.0-and-2.1-no-longer-supported"></a>

Support for Firebird 2.0 and 2.1 has been dropped in Jaybird 4. In general, we
expect the driver to remain functional, but chances are certain metadata (eg 
`DatabaseMetaData`) will break if we use features introduced in newer versions.

In general, we will no longer fix issues that only occur with Firebird 2.1 or
earlier.

As a result of changes in `FBDatabaseMetaData`, most result set producing 
methods will no longer work with Firebird 1.5 or earlier (unsupported since 
Jaybird 3).

Removed Legacy_Auth from default authentication plugins
-------------------------------------------------------

The pure Java protocol in Jaybird will - by default - no longer try the 
`Legacy_Auth` plugin when connecting to Firebird 3 or higher.

See [Default authentication plugins](#default-authentication-plugins) for more
information.

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
See [Connection property sessionTimeZone](#connection-property-sessiontimezone)
for more information.

RDB$DB_KEY columns no longer of Types.BINARY
--------------------------------------------

With the introduction of [JDBC RowId support](#jdbc-rowid-support), `RDB$DB_KEY`
columns are no longer identified as `java.sql.Types.BINARY`, but as
`java.sql.Types.ROWID`. The column will behave in a backwards-compatible manner
as a binary field, except `getObject`, which will return a `java.sql.RowId`
instead.

Unfortunately this does not apply to parameters, see also [JDBC RowId support](#jdbc-rowid-support).

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
[JDBC RowId support](#jdbc-rowid-support).

If you are currently using `DatabaseMetaData.getBestRowIdentifier` with 
`scope` value `DatabaseMetaData.bestRowSession`, consider if you need to
use `bestRowTransaction` instead.

If you are relying on the `SCOPE` column containing the value for the requested
scope, change your logic to remove that dependency.

Precision reported for FLOAT and DOUBLE PRECISION on Firebird 4
---------------------------------------------------------------

Firebird 4 introduced support for SQL standard `FLOAT(p)` with 1 <= p <= 24 a
synonym of `FLOAT`, and 25 <= p <= 53 as synonym of `DOUBLE PRECISION`. This
precision is expressed in binary digits (or radix 2).

To bring the metadata reported by Jaybird in line for this change, on Firebird 4
and higher, we now report the precision for `FLOAT` and `DOUBLE PRECISION` in
binary digits instead of decimal digits.

For example for `FLOAT` precision, on Firebird 3 and lower Jaybird 4 returns `7`,
and `24` on Firebird 4 and higher. For `DOUBLE PRECISION` precision, on Firebird 3
and lower Jaybird 4 returns `15`, and `53` on Firebird 4 and higher. In addition,
the radix reported in the metadata for these types will be 2 instead of 10 on
Firebird 4 and higher.

This change affects 

- `DatabaseMetaData.getColumns` (columns `COLUMN_SIZE` and `NUM_PREC_RADIX`),
- `DatabaseMetaData.getProcedureColumns` (columns `PRECISION` and `RADIX`), 
- `DatabaseMetaData.getTypeInfo` (columns `PRECISION` and `NUM_PREC_RADIX`),
- `ParameterMetaData.getPrecison`, 
- `ResultSetMetaData.getPrecision`.

Incompatibilities due to URL encoding in JDBC URL query part
------------------------------------------------------------

With the introduction of URL encoding for the query part of the JDBC URL, the
use of characters `+` and `%` in the query part of a JDBC URL now have different
meaning and can lead to errors or unexpected results. This is especially
relevant for base64 encoded values of `dbCryptConfig`.

See [URL encoding in query part of JDBC URL](#url-encoding-in-query-part-of-jdbc-url)
for more information.

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
empty array if the statement is a statement that generates keys. Instead, an 
exception is thrown with message _"Generated keys array (columnIndexes|columnNames) 
was empty or null. A non-empty array is required."_

This change does not apply for statements that already explicitly include a 
`RETURNING` clause or for non-generated keys statements. In those cases, the
array is ignored.

#### Invalid column index no longer allowed ####

In addition, the methods accepting an `int[]` array no longer ignore invalid 
column indexes, and instead throw an exception with message _"Generated keys 
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

See also [Generated keys grammar simplification](#generated-keys-grammar-simplification). 

### DatabaseMetaData ###

#### Excluding procedures from packages ####

The definition of `getProcedures` and `getProcedureColumns` does not offer a way
to return information on procedures in packages without breaking tools or
applications that rely on the JDBC standard behaviour. To avoid these problems,
stored procedures in packages are no longer returned from these methods.

The way Jaybird handled this previously was 'by accident', and the information
returned was not enough to correctly call the procedure as the package name was
not included.

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
longer be available. As part of this change, the following parts of the 
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

Generated keys support always available {#generated-keys-always}
---------------------------------------

Since Jaybird 4.0.8.

Previously, support for generated keys depended on the presence of 
the antlr4-runtime library on the classpath. With [New parser for generated keys handling][#generated-keys-parser-replaced], 
generated keys support is now always available.

See [New parser for generated keys handling][#generated-keys-parser-replaced] 
for information on disabling or ignoring generated keys support if you relied on
this behaviour.

Removal of constants without deprecation
----------------------------------------

The following array constants in `FBDatabaseMetaData` have been made private or
have been removed to avoid unintended side effects of modification:

-   `ALL_TYPES_2_5`
-   `ALL_TYPES_2_1`
-   `ALL_TYPES`

Instead, use `DatabaseMetaData.getTableTypes()` (which returns a `ResultSet`),
or `FirebirdDatabaseMetaData.getTableTypeNames()` (which returns a `String[]`).

To access `getTableTypeNames()`, the `DatabaseMetaData` needs to be unwrapped to
`FirebirdDatabaseMetaData` 

```java
DatabaseMetaData dbmd = connection.getDatabaseMetaData();
String[] tableTypes = dbmd
        .unwrap(FirebirdDatabaseMetaData.class)
        .getTableTypeNames();
```
    
Removal of deprecated classes, packages and methods
---------------------------------------------------

The following connection properties (and equivalent data source properties) have
been removed:

-   `useTranslation`: See [Removal of character mapping](#removal-of-character-mapping)
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
-   See also [Removal of character mapping](#removal-of-character-mapping) for
    a number of removed methods
    
The following classes have been removed in Jaybird 4:

-   `org.firebirdsql.gds.ExceptionListener`, use `org.firebirdsql.gds.ng.listeners.ExceptionListener`
-   `org.firebirdsql.pool.FBSimpleDataSource`, use `org.firebirdsql.ds.FBSimpleDataSource` 

### Removal of deprecated constants ###

The following constants have been removed in Jaybird 4:

-   All `SQL_STATE_*` constants in `FBSQLException`,
    `FBResourceTransactionException`, `FBResourceException`, and
    `FBDriverNotCapableException` have been removed. Use equivalent constants in
    `org.firebirdsql.jdbc.SQLStateConstants`.

Breaking changes for Jaybird 5
------------------------------

With Jaybird 5 the following breaking changes will be introduced.

### Dropping support for Java 7 ###

Jaybird 5 will drop support for Java 7.

### Dropping support for Java 8 (tentative) ###

Jaybird 5 will continue to support Java 8.

### Dropping JCA support ###

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
-   `FbStatement.getFieldDescriptor()`, use `FbStatement.getRowDescriptor()`
-   `AbstractFbStatement.setFieldDescriptor(RowDescriptor fieldDescriptor)`, 
    use `AbstractFbStatement.setRowDescriptor(RowDescriptor rowDescriptor)`
-   `FBField.isType(FieldDescriptor, int)`, use 
    `JdbcTypeConverter.isJdbcType(FieldDescriptor, int)`
    
### Removal of deprecated classes ###

The following classes will be removed in Jaybird 5:

-   `FBMissingParameterException`, exception is no longer used.
    
### Removal of deprecated constants ###

The following constants will be removed in Jaybird 5:

-   All `SQL_STATE_*` constants in `FBSQLParseException` will be removed. Use equivalent 
    constants in `org.firebirdsql.jdbc.SQLStateConstants`.
-   `DatabaseParameterBufferExtension.EXTENSION_PARAMETERS` will be removed. There is no
    official replacement as this should be considered an implementation detail. It is
    possible that `DatabaseParameterBufferExtension` will be removed entirely.

### Removal of UDF support for JDBC escapes ###

Jaybird 4 and earlier have support to map JDBC function escapes to UDFs from
`ib_udf` instead of built-in function using the boolean connection property
`useStandarUdf`\[sic\].

Given recent Firebird versions have significantly improved support for built-in
functions, and UDFs are now deprecated, this option will be removed in Jaybird 5. 
    
Compatibility notes
===================

Type 2 (native) and embedded driver
-----------------------------------

Jaybird uses JNA to access the client library. If you want to use the Type 2 
driver, or Firebird embedded, then you need to include `jna-5.5.0.jar` on the 
classpath.

When using Maven, you need to specify the dependency on JNA yourself, as we 
don't depend on it by default (it is specified as an optional dependency):

``` {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.5.0</artifactId>
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
    <version>3.0.5.1</artifactId>
</dependency>
```

In the future we will move the Type 2 support to a separate library and provide 
JNA-compatible jars that provide the embedded libraries of a specific Firebird 
version.
