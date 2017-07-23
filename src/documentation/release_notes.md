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

Jaybird 4 does not (yet) support the Firebird 3 wire encryption nor zlib compression.

To be able to connect to Firebird 3, it is necessary to change the `WireCrypt` 
setting from its default `Required` to `Enabled` in `firebird.conf`:
                                     
    WireCrypt = Enabled
    
This configuration option can also be set to `Disabled`, but that is not 
advisable as that will also disable it for clients that do support wire 
encryption.

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

You can use the Java 8 driver under Java 9, contrary to earlier Jaybird 3 test 
releases, it is no longer necessary to add the `java.xml.bind` module using 
`--add-modules java.xml.bind`.

Jaybird cannot be fully tested under Java 9 at this moment, as some of our tests
fail due to recent changes, that prevent JMock (or specifically cglib) from
dynamically generating classes.

Firebird support
----------------

Support for Firebird 2.0 and 2.1 has been dropped. See [Firebird 2.0 and 2.1 no
longer supported] for details.

See also [Jaybird and Firebird 3](https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3)
on the wiki.

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

Removal of deprecated classes, packages and methods
---------------------------------------------------

The following deprecated methods have been removed in Jaybird 4:

-   `CharacterTranslator.getMapping()`, use `CharacterTranslator.getMapping(char)`
    instead.
    
    Complete removal of the character translation support is also being
    considered, as similar effects can be achieved by a custom encoding 
    implementation.
    
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
    
The following classes have been removed in Jaybird 3:

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