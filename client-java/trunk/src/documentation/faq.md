---
title: Jaybird Frequently Asked Questions
tags: [jaybird, firebird, jdbc, sql, database, java]
...

Jaybird
=======

Where do I get Jaybird?
-----------------------

Firebird can be downloaded from the Firebird website, under Downloads, [JDBC Driver](http://www.firebirdsql.org/en/jdbc-driver/).

Alternatively, you can go directly to Source Forge and download Jaybird from the [firebird-jca-jdc-driver files section](http://sourceforge.net/projects/firebird/files/firebird-jca-jdbc-driver/).

Where can I get the sourcecode?
-------------------------------

The Jaybird sourcecode is available from Source Forge in the Firebird subversion repository, under client-java. The full URL (trunk) is:

[http://svn.code.sf.net/p/firebird/code/client-java/trunk](http://svn.code.sf.net/p/firebird/code/client-java/trunk)

JDBC Support
============

How much of JDBC is supported by Jaybird?
-----------------------------------------

**WARNING** The information in this section is outdated

Jaybird follows the JDBC 4.2 specification with some features and methods not implemented. Some of the unimplemented items are required by the specification and some are optional.

Implemented features:

* Most useful JDBC functionality ("useful" in the opinion of the developers).
* Complete JCA API support: may be used directly in JCA-supporting application servers such as JBoss and WebLogic.
* XA transactions with true two phase commit when used as a JCA resource adapter in a managed environment (with a TransactionManager and JCA deployment support) as well as when used via javax.sql.XADataSource implementation.
* Includes optional internal connection pooling for standalone use and use in non-JCA environments such as Tomcat 4.
* ObjectFactory implementation for use in environments with JNDI but no TransactionManager such as Tomcat 4.
* DataSource implementations without pooling.
* Driver implementation for use in legacy applications.
* Complete access to all Firebird database parameter block and transaction parameter block settings.
* Optional integrated logging through log4j.
* JMX mbean for database management (so far just database create and drop).

What parts of JDBC are NOT supported by JayBird?
------------------------------------------------

**WARNING** The information in this section is outdated

The following optional features are NOT supported:

The following optional features and the methods that support it are not implemented:

* Batch Updates
    * `java.sql.Statement`
        * `addBatch(String sql)`
        * `clearBatch()`
        * `executeBatch()`
        * `addBatch()`
* Scrollable cursors
    * `java.sql.ResultSet`
        * `beforeFirst()`
        * `afterLast()`
        * `first()`
        * `last()`
        * `absolute(int row)`
        * `relative(int rows)`
        * `previous()`
* Updateable cursors
    * `java.sql.ResultSet`
        * `rowUpdated()`
        * `rowInserted()`
        * `rowDeleted()`
        * `updateXXX(....)`
        * `insertRow()`
        * `updateRow()`
        * `deleteRow()`
        * `refreshRow()`
        * `cancelRowUpdates()`
        * `moveToInsertRow()`
        * `moveToCurrentRow()`
* Cursors/Positioned update/delete
    * `java.sql.Statement`
        * `setCursorName()`
    * `java.sql.ResultSet`
        * `getCursorName()`
* Ref, Clob and Array types.
    * `java.sql.PreparedStatement`
        * `setRef(int i, Ref x)`
        * `setClob (int i, Clob x)`
        * `setArray(int i, Array x)`
    * `java.sql.ResultSet`
        * `getArray(int i)`
        * `getArray(String columnName)`
        * `getRef(int i)`
        * `getRef(String columnName)`
        * `getClob(int i)`
        * `getClob(String columnName)`
* User Defined Types/Type Maps.
    * `java.sql.ResultSet`
        * `getObject(int i, java.util.Map map)`
        * `getObject(String columnName, java.util.Map map)`
    * `java.sql.Connection`
        * `getTypeMap()`
        * `setTypeMap(java.util.Map map)`

Excluding the unsupported features, the following methods are not yet implemented:

* `java.sql.Statement`
    * `cancel()`
* `java.sql.CallableStatement`
    * `registerOutParameter(int parameterIndex, int sqlType)`
    * `registerOutParameter(int parameterIndex, int sqlType, int scale)`
    * `wasNull()`
* `java.sql.Blob`
    * `length()`
    * `getBytes(long pos, int length)`
    * `position(byte pattern[], long start)`
    * `position(Blob pattern, long start)`

The following methods are implemented, but do not work as expected:

* `java.sql.Statement`
    * `get/setMaxFieldSize` does nothing
    * `get/setQueryTimeout` does nothing
* `java.sql.PreparedStatement`
    * `setObject(index,object,type)` This method is implemented but behaves as `setObject(index,object)`
    * `setObject(index,object,type,scale)` This method is implemented but behaves as `setObject(index,object)`
* `java.sql.ResultSetMetaData`
    * `isReadOnly(i)` always returns false
    * `isWritable(i)` always returns true
    * `isDefinitivelyWritable(i)` always returns true
* `java.sql.DatabaseMetaData`
    * `getBestRowIdentifier(i)` always returns empty resultSet