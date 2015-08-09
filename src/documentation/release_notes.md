---
title: Jaybird @VERSION@ Release Notes
tags: [jaybird, firebird, jdbc, sql, database, java]
...

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

**Current snapshot versions do not support the embedded driver**

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

Jaybird 3.0 was tested against Firebird ~~2.1.7 and~~ 2.5.4, and recent 
snapshots of Firebird 3, but should also support other Firebird versions 
from 2.0 and up. Formal support for Firebird 1.x has been dropped (although in
general we expect the driver to work). The Type 2 ~~and embedded server~~ JDBC 
drivers use JNA to access the Firebird client ~~or embedded library~~.

**Current snapshot versions do not support the embedded driver**
 
This driver does not supports InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

Jaybird 3.0 is the last version to support Firebird 2.0.

Supported Java versions
-----------------------

Jaybird 3.0 supports Java 7 (JDBC 4.1) and Java 8 (JDBC 4.2).
Support for earlier Java versions has been dropped.

Support for Java 7 might still be dropped before Jaybird 3.0 final release.

Specification support
---------------------

**TODO: Take and update table from old ODT release notes**

What's new in Jaybird 3.0
=========================

Java support
------------

Support for Java 5 and 6 has been dropped.

Firebird support
----------------

Support for Firebird 1.0 and 1.5 has been dropped. See [Firebird 1.0 and 1.5 no
longer supported] for details.

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

Jaybird 3.0 does not yet provide the new Firebird 3.0 authentication and wire
encryption. This is planned for Jaybird 3.1, but might be moved into Jaybird 
3.0 before the final release.

The new low-level implementation also means that the old GDS API 
(`org.firebirdsql.gds.GDS`) has been removed and is no longer available.

The changes due to the new protocol implementation and/or JDBC conformance are
listed below.

**The list is not yet complete, if you notice a difference in behavior that is
not listed, please report it as bug.** It might have been a change we forgot to
document, but it could just as well be an implementation bug.

### Statement ###

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

### Exceptions ###

* `FBSQLException` and sub-classes replaced with actual `java.sql.*` exceptions.

    Over time the JDBC exception hierarchy has become more complicated with more
    specific exceptions. It was easier to use the `java.sql` exception-
    hierarchy, than to duplicate the hierarchy within Jaybird.
    
    This change does not mean that there are no Firebird-specific `SQLException`
    sub-classes anymore, but in general we strive to use the standard 
    exceptions.

* `org.firebirdsql.gds.GDSException` removed from exception causes.

    The new low-level implementation throws `java.sql.SQLException` classes
    eliminating the need for `GDSException` (which was usually set as the
    `cause` of an `SQLException`). In some cases uses of `GDSException`
    have been replaced by `org.firebirdsql.jdbc.FBSQLExceptionInfo` to
    report exception message elements and their error codes.

* Exception message format changed:
    * Exception message elements now separated by semi-colon, not by 
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

* More specific error reported by `SQLException.getErrorCode` and 
  `SQLException.getSQLState`.
  
    In previous versions a large class of errors always reported error 335544569
    (or `isc_dsql_error`) with SQLState 42000, Jaybird now tries to find a more
    specific error code (and SQLState) in the status vector.

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

Removal of deprecated classes and packages
------------------------------------------

See [Removal of deprecated classes, packages and methods] in 
[Compatibility changes] for more details.

Known Issues
============

* `ResultSetMetaData.getTableAlias` reports the original table name. 
  
    In the new protocol implementation, the information items to retrieve this
    information are dependent on the protocol version. Older Firebird versions
    don't know the required information item (`isc_info_sql_relation_alias`).
    
    As soon as other protocol versions get implemented, this problem will be 
    solved.

* Native and embedded protocol are not working.

    The new protocol implementation has a new set of interfaces, the native
    implementation hasn't been rewritten yet to conform to these interfaces.

Compatibility changes
=====================

Jaybird 3.0 introduces some changes in compatibility and announces future
breaking changes.

**Some of the compatibility changes are documented in [What's new in Jaybird 3.0].**

Firebird 1.0 and 1.5 no longer supported
----------------------------------------

Support for Firebird 1.0 and 1.5 has been dropped in Jaybird 3.0. In general we
expect the driver to remain functional, but chances are certain metadata (eg 
`DatabaseMetaData`) will break if we use features introduced in newer versions.

In general we will no longer fix issues that only occur with Firebird 1.5 or
earlier.

Java 5 and 6 no longer supported
--------------------------------

Support for Java 5 (JDBC 3.0) and Java 6 (JDBC 4.0) has been dropped in 
Jaybird 3.0. The Jaybird 3.0 sources no longer compile with Java 5 and 6 due to 
use of Java 7 language features and JDBC 4.1 specific features.

Stricter JDBC compliance
------------------------

In Jaybird 3.0 a number of changes were made for stricter compliance to the JDBC
specification.

**TODO: Document or refer to change what's new?**

Removal of old GDS API
----------------------

The old GDS API (`org.firebirdsql.gds.GDS`) has been removed. This removal
includes a number of related classes and methods.

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

With this change, there are no `DataSource` implementations in Jaybird to
provide connection pooling (the `ConnectionPoolDataSource` implementations are
for use by a connection pool and not a connection pool themselves). Either use
the connection pool provided by your Application Server, or use a third-party
connection pool like c3p0, Apache DBCP or BoneCP.

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

Breaking changes for Jaybird 3.1
--------------------------------

With Jaybird 3.1 the following breaking changes will be introduced.

### Dropping support for Firebird 2.0 ###

Jaybird 3.1 will drop support for Firebird 2.0. In general we expect the driver
to remain functional, but chances are certain metadata (eg `DatabaseMetaData`)
will break if we use features introduced in newer versions.