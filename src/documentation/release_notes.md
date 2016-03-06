WARNING {-}
=======

Jaybird 3.0 is still in development. This version is provided for testing
purposes only. We'd appreciate your feedback, but we'd like to emphasize that
this version is **unstable** and **not ready for production**.

The protocol implementation has been fundamentally rewritten and changes have
been made for stricter JDBC conformance. As a result the driver might exhibit
different behavior than previous versions. Read these release notes carefully to
see if those differences are intentional. Bug reports about undocumented changes
in behavior are appreciated.

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

Supported Firebird versions
---------------------------

Jaybird 3.0 was tested against Firebird 2.1.7 and 2.5.5, and recent snapshots
of Firebird 3, but should also support other Firebird versions from 2.0 and up.
Formal support for Firebird 1.x has been dropped (although in general we expect
the driver to work). The Type 2 and embedded server JDBC drivers use JNA to
access the Firebird client or embedded library.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

Jaybird 3.0 is the last version to support Firebird 2.0.

Supported Java versions
-----------------------

Jaybird 3.0 supports Java 7 (JDBC 4.1) and Java 8 (JDBC 4.2).
Support for earlier Java versions has been dropped.

Support for Java 9 (JDBC 4.3) is planned, but implementation has not yet
started.

Jaybird 3.0 is the last version to support Java 7.

Specification support
---------------------

**TODO: Take and update table from old ODT release notes**

Getting Jaybird 3.0 snapshot
============================

Occasionally we release a Jaybird 3 snapshot for testing purposes to
the [Sonatype OSS snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/).

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird-jdkXX` (where `XX` is `17` or `18`).\
Version: `3.0.0-SNAPSHOT`

For example:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
~~~

You need to add the Sonatype OSS snapshot repository to your maven repository config or pom.

What's new in Jaybird 3.0
=========================

Java support
------------

Support for Java 6 has been dropped.

Firebird support
----------------

Support for Firebird 1.0 and 1.5 has been dropped. See [Firebird 1.0 and 1.5 no
longer supported] for details.

Firebird 2.0 support is improved with the implementation of wire protocol
version 11. (_TODO: Or was v11 Firebird 2.1?_)

Firebird 2.5 support is improved with the implementation of wire protocol
version 12.

Firebird 3.0 support is improved with the partial implementation of wire
protocol 13 and support for the _Srp_ authentication plugin. Version 13 support
does not yet provide Firebird 3.0 wire encryption and zlib compression. Wire
encryption is planned for Jaybird 3.1, but might be moved into Jaybird 3.0
before the final release. Support for zlib compression is not planned yet.

*TODO: Add FB 3 compatibility notes / link to wiki*

Support for protocol version 13 and the SRP authentication was contributed
by [Hajime Nakagami](https://github.com/nakagami).

### Other Firebird feature support

* Add support for streaming backup and restore ([JDBC-256](http://tracker.firebirdsql.org/browse/JDBC-256))

   This feature was contributed by [Ivan Arabadzhiev](https://github.com/ls4f)

New low-level implementation
----------------------------

Jaybird 3.0 has a substantially rewritten low-level implementation (the wire
protocol and native implementation) and a number of changes for JDBC 
conformance.

The rewrite of the low-level implementation was prompted by the new
authentication (and wire encryption) in Firebird 3.0 (protocol version 13), and
the fact that other improvements in the Firebird wire protocol (versions 11 and
12) were not yet available in the pure Java implementation in Jaybird. The old
implementation of the wire protocol did not lend itself for - easily - 
supporting multiple protocol versions.

The new low-level implementation also means that the old GDS API 
(`org.firebirdsql.gds.GDS`) has been removed and is no longer available.

Potentially breaking changes
----------------------------

Jaybird 3.0 contains a number of changes that might break existing applications.

See [Compatibility changes] for details.

Other fixes and changes
-----------------------

* Fix: IP-address is reversed on big-endian platforms ([JDBC-98](http://tracker.firebirdsql.org/browse/JDBC-98))

* Reimplemented character set handling (*TODO: Provide more info?*)

* Improved support of JDBC Escape syntax (`{...}`) and supported functions (*TODO: Provide more info?*)
  ([JDBC-223](http://tracker.firebirdsql.org/browse/JDBC-223))

    The escape parser will now only allow the function names defined in
    Appendix D of the JDBC specification (4.1 for now). For unsupported
    functions or functions not listed in Appendix D a `FBSQLParseException` will
    be thrown.
    
    The database metadata will now correctly report supported functions for
    `getNumericFunctions`, `getStringFunctions`, `getSystemFunctions` and 
    `getTimeDateFunctions`.
    
* Nested JDBC escapes are now supported ([JDBC-292](http://tracker.firebirdsql.org/browse/JDBC-292))

* `CHAR` and `VARCHAR` columns with character set `OCTETS` are now handled as
  JDBC type `BINARY` and `VARBINARY`, respectively ([JDBC-240](http://tracker.firebirdsql.org/browse/JDBC-240))

    See also [Character set OCTETS handled as JDBC (VAR)BINARY]

Removal of deprecated classes and packages
------------------------------------------

See [Removal of deprecated classes, packages and methods] in 
[Compatibility changes] for more details.

Known Issues
============

There are currently no known issues.

Compatibility changes
=====================

Jaybird 3.0 introduces some changes in compatibility and announces future
breaking changes.

The changes due to the new protocol implementation and/or JDBC conformance are
listed below.

**The list is not yet complete, if you notice a difference in behavior that is
not listed, please report it as bug.** It might have been a change we forgot to
document, but it could just as well be an implementation bug.

Exceptions
----------

* `FBSQLException` and sub-classes replaced with actual `java.sql.*` exceptions.

    Over time the JDBC exception hierarchy has become more complicated with more
    specific exceptions. It was easier to use the `java.sql` exception-
    hierarchy, than to duplicate the hierarchy within Jaybird.

    This change does not mean that there are no Firebird-specific `SQLException`
    sub-classes anymore, but in general we strive to use the standard
    exceptions where possible.

* Class `FBSQLWarning` has been removed and should be replaced with `SQLWarning`.

* Methods with `throws FBSQLException` changed to `throws SQLException`

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

* `org.firebirdsql.gds.GDSException` removed from exception causes.

    The new low-level implementation throws `java.sql.SQLException` classes
    eliminating the need for `GDSException` (which was usually set as the
    `cause` of an `SQLException`). In some cases uses of `GDSException`
    have been replaced by `org.firebirdsql.jdbc.FBSQLExceptionInfo` to
    report exception message elements and their error codes.

* Exception message format changed:
    * Exception message elements are now separated by semi-colon, not by
      linebreak.

        Errors reported by Firebird can consist of multiple elements. In Jaybird
        2.2 and earlier the final exception message was constructed by
        separating these elements by a linebreak. These elements are now
        separated by a semi-colon and a space.

    * Exception message now reports SQLState and error code.

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
    those messages do not contain the SQLState and error code in the message.

* More specific error reported by `SQLException.getErrorCode` and
  `SQLException.getSQLState`.

    In previous versions a large class of errors always reported error 335544569
    ("Dynamic SQL Error" or `isc_dsql_error`) with SQLState 42000, Jaybird now
    tries to find a more specific error code (and SQLState) in the status vector.

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

### Statement ###

_Unless explicitly indicated, changes also apply to `PreparedStatement` and
`CallableStatement`_

* Generated keys `ResultSet` only available through `getGeneratedKeys`.

    The generated keys `ResultSet` from a statement is no longer available
    through `getResultSet`, but only through `getGeneratedKeys` as the JDBC
    specification does not consider the generated keys `ResultSet` a normal
    `ResultSet`.

    <span id="generated-query-types">This applies to statements executed (or
    prepared) using:</span>

    * `Statement.execute(String, int)` or `Statement.executeUpdate(String, int)`
      with value `Statement.RETURN_GENERATED_KEYS`,
    * `Statement.execute(String, int[])` or
      `Statement.executeUpdate(String, int[])`,
    * `Statement.execute(String, String[])` or
      `Statement.executeUpdate(String, String[])`,
    * `Connection.prepareStatement(String, int)` with value
      `Statement.RETURN_GENERATED_KEYS`,
    * `Connection.prepareStatement(String, int[])`,
    * `Connection.prepareStatement(String, String[])`.

    This change does not apply to executing `INSERT ... RETURNING ...` as a
    normal statement.

* Update count immediately available after executing generated keys queries.

    Previously the update count of a generated keys query was only available
    after calling `getMoreResults` followed by a call to `getUpdateCount`. This
    change means that `executeUpdate` will now correctly return the update count
    (usually `1`) instead of `-1`. The same applies to calling `getUpdateCount`
    after `execute` (without the need to call `getMoreResults`).

    For the definition of generated keys queries see [the previous item](#generated-query-types).

* Use of function escapes (`{fn ...}`) not defined in the JDBC standard will now
  throw an `FBSQLParseException`, previously the escape was removed and the
  function was used as is.

#### PreparedStatement ####

_Unless explicitly indicated, changes also apply to `CallableStatement`_

* Method `setUnicodeStream` now always throws
  an `SQLFeatureNotSupportedException`. The previous implementation did not
  conform to the (deprecated) JDBC requirements and instead behaved like
  `setBinaryStream`.

    For the behavior in Jaybird 2.2 and earlier, use `setBinaryStream`.
    Otherwise use `setCharacterStream`.

### ResultSet ###

* Method `getUnicodeStream` now always throws
  an `SQLFeatureNotSupportedException`. The previous implementation did not
  conform to the (deprecated) JDBC requirements and instead behaved like
  `getBinaryStream`.

    For the behavior in Jaybird 2.2 and earlier, use `getBinaryStream`.
    Otherwise use `getCharacterStream`.

**TODO: Add other changes**

Character set OCTETS handled as JDBC (VAR)BINARY
------------------------------------------------

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

Jaybird no longer needs a `jaybirdxx.dll` or `jaybirdxx.so` for the Type 2 and 
embedded driver. Jaybird now uses JNA to access the client library.

If you want to use the Type 2 driver, or Firebird embedded, then you need to
include the `jna-x.x.x.jar` on the classpath. The `fbclient.dll`, `fbembed.dll`, 
`libfbclient.so`, or `libfbembed.so` need to be on the path.

**TODO: May need further documentation**

Removal of deprecated classes, packages and methods
---------------------------------------------------

### DataSource and connection pooling ###

The classes in `org.firebirdsql.pool` and `org.firebirdsql.pool.sun` have been
removed completely, with the exception of 
`org.firebirdsql.pool.FBSimpleDataSource`. This class has been moved to
`org.firebirdsql.ds.FBSimpleDataSource`. A subclass with the same name is kept
in `org.firebirdsql.pool` for backwards compatibility. This subclass will be
removed in future versions of Jaybird.

With this change, there are no `javax.sql.DataSource` implementations in Jaybird
that provide connection pooling (the `javax.sql.ConnectionPoolDataSource`
implementations are for use by a connection pool and not a connection pool
themselves). Either use the connection pool provided by your Application Server,
or use a third-party connection pool like c3p0, Apache DBCP or HikariCP.

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

Breaking changes for Jaybird 3.1
--------------------------------

With Jaybird 3.1 the following breaking changes will be introduced.

### Dropping support for Firebird 2.0 ###

Jaybird 3.1 will drop support for Firebird 2.0. In general we expect the driver
to remain functional, but chances are certain metadata (eg `DatabaseMetaData`)
will break if we use features introduced in newer versions.

### Dropping support for Java 7 ###

Jaybird 3.1 will very likely drop support for Java 7 (this decision is not final yet).

### Removal of deprecated classes, interfaces, and constants ###

The following constants will be removed in Jaybird 3.1

-   All `SQL_STATE_*` constants in `FBSQLException`,
    `FBResourceTransactionException`, and `FBResourceException` will be removed.
    Use equivalent constants in `org.firebirdsql.jdbc.SQLStateConstants`.