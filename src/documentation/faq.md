Jaybird
=======

Where do I get Jaybird?
-----------------------

### Maven ###

#### Jaybird 2.2 ####

Jaybird is available on maven (since version 2.2.0), with a separate artifact
for each supported Java version.

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird-jdkXX` (where `XX` is `16`, `17` or `18`).\
Version: `2.2.11`

For example:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>2.2.11</version>
</dependency>
~~~

When deploying to a JavaEE environment, exclude the `javax.resource connector-api`
dependency as this will be provided by the application server.

#### Jaybird 3.0 for testing ####

Jaybird 3.0 Alpha 1 is available from Maven central:

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird-jdkXX` (where `XX` is `17` or `18`).\
Version: `3.0.0-alpha-1`

For example:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>3.0.0-alpha-1</version>
</dependency>
~~~

If your application is deployed to a Java EE application server, you will need to
exclude the `javax.resource:connector-api` dependency, and add it as a provided 
dependency:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>3.0.0-alpha-1</version>
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
explicitly include JNA 4.2.2 as a dependency:

~~~ {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>4.2.2</version>
</dependency>
~~~

### Download ###

Firebird can be downloaded from the Firebird website, under Downloads,
[JDBC Driver](http://www.firebirdsql.org/en/jdbc-driver/).

Alternatively, you can go directly to GitHub and download Jaybird from the
[jaybird releases](https://github.com/FirebirdSQL/jaybird/releases).

Where can I get the sourcecode?
-------------------------------

All Jaybird distribution zips contain a `jaybird-<version>-sources.zip` with the
sources used for that specific version. The full Jaybird sourcecode is also
available from GitHub in the jaybird repository:

[https://github.com/FirebirdSQL/jaybird](https://github.com/FirebirdSQL/jaybird)

Each release is also tagged in the repository.

How is Jaybird licensed?
------------------------

Jaybird JCA/JDBC driver is distributed under the GNU Lesser General Public
License (LGPL). Text of the license can be obtained from
[http://www.gnu.org/copyleft/lesser.html](http://www.gnu.org/copyleft/lesser.html).

Using Jaybird (by importing Jaybird's public interfaces in your Java code), and
extending Jaybird by subclassing or implementation of an extension interface
(but not abstract or concrete class) is considered by the authors of Jaybird to
be dynamic linking. Hence our interpretation of the LGPL is that the use of the
unmodified Jaybird source does not affect the license of your application code.

Even more, all extension interfaces to which an application might want to link
are released under dual LGPL/modified BSD license. Latter is basically "AS IS"
license that allows any kind of use of that source code. Jaybird should be
viewed as an implementation of that interfaces and the LGPL section for dynamic
linking is applicable in this case.

Which version of the LGPL applies?
----------------------------------

Current releases of Jaybird do not explicitly specify an LGPL version. This
means that you can choose which version applies. Future versions of Jaybird may
specify an explicit version, or be released under a different license.

Which Java versions are supported?
----------------------------------

Jaybird 3.0 supports Java 7 and 8 and as rudimentary support for Java 9.

Jaybird 2.2 supports Java 6, 7 and 8. 

Jaybird 2.2.4 added basic support for Java 8 (JDBC 4.2), although not all 
JDBC 4.2 features are supported or fully implemented.

Jaybird 2.2.7 is the last version to support Java 5, support has been dropped
with Jaybird 2.2.8.

Jaybird 2.2 is the last version to support Java 6, support will be dropped with
Jaybird 3.0.

Which Firebird versions are supported?
--------------------------------------

Jaybird 3.0 supports Firebird versions 2.0 and higher.

Jaybird 3.0 is the last version to support Firebird 2.0. Future versions of
Jaybird are not guaranteed to work with version 2.0 and earlier.

Jaybird 2.2 supports all Firebird versions 1.0 and higher. Jaybird 2.2.4 added
support for new features of Firebird 3 (eg `BOOLEAN` support).

Jaybird 2.2 is the last version to support Firebird 1.0 and 1.5. Future
versions of Jaybird are not guaranteed to work with these versions.

Can Jaybird connect to Interbase?
---------------------------------

Jaybird does not support Interbase, and as far as we know connecting to
Interbase 6.0 and later will fail due to Firebird specific changes in the
implementation.

Documentation and Support
=========================

Where to get more information on Jaybird
----------------------------------------

Apart from this FAQ, you can get additional information from:

* [Jaybird wiki](https://github.com/FirebirdSQL/jaybird/wiki/)
* [Jaybird 2.1 Programmers Manual](http://www.firebirdsql.org/file/documentation/drivers_documentation/Jaybird_2_1_JDBC_driver_manual.pdf) (PDF)
* [Firebird Website: Development, JDBC Driver](http://www.firebirdsql.org/en/devel-jdbc-driver/)

Where to get help
-----------------

*   On [Stack Overflow](http://stackoverflow.com/), please tag your questions
    with *jaybird* and *firebird*
*   The [Firebird-Java group](http://groups.yahoo.com/group/Firebird-Java) and
    corresponding mailing list firebird-java@yahoogroups.com

    You can subscribe to the mailing list by sending an email to
    [firebird-java-subscribe@yahoogroups.com](mailto:firebird-java-subscribe@yahoogroups.com)

*   The [Firebird project home page](http://www.firebirdsql.org)
*   Firebird support and other [Firebird mailing lists](http://www.firebirdsql.org/en/mailing-lists/)
    for questions not directly related to Jaybird and java.

Contributing
------------

There are several ways you can contribute to Jaybird or Firebird in general:

* Participate on the mailing lists (see [http://www.firebirdsql.org/en/mailing-lists/](http://www.firebirdsql.org/en/mailing-lists/))
* Report bugs or submit patches on the tracker (see [Reporting Bugs])
* Create pull requests on GitHub ([https://github.com/FirebirdSQL/jaybird](https://github.com/FirebirdSQL/jaybird))
* Become a developer (for Jaybird contact us on firebird-java, for Firebird in
  general, use the Firebird-devel mailing list)
* Become a paying member or sponsor of the Firebird Foundation (see
  [http://www.firebirdsql.org/en/firebird-foundation/](http://www.firebirdsql.org/en/firebird-foundation/))

See also [http://www.firebirdsql.org/en/consider-your-contribution/](http://www.firebirdsql.org/en/consider-your-contribution/)

Reporting Bugs
--------------

The developers follow the firebird-java@yahoogroups.com list. Join the list and
post information about suspected bugs. List members may be able to help out to
determine if it is an actual bug, provide a workaround and get you going again,
whereas bug fixes might take awhile.

You can report bugs in the Firebird bug tracker, project
["Java Client (Jaybird)"](http://tracker.firebirdsql.org/browse/JDBC)

When reporting bugs, please provide a minimal, but complete reproduction,
including databases and sourcecode to reproduce the problem. Patches to fix bugs
are also appreciated. Make sure the patch is against a recent master version of
the code. You can also fork the jaybird repository and create pull requests.

JDBC URLs (`java.sql.DriverManager`)
====================================

Pure Java (default)
-------------------

Default URL format:

    jdbc:firebirdsql://host[:port]/<database>

This will connect to the database using the Type 4 JDBC driver using the Java
implementation of the Firebird wire-protocol. This is best suited for
client-server applications with dedicated database server. Port can be omitted
(default value is `3050`), host name must be present.

The `<database>` part should be replaced with the database alias or the path to
the database. In general it is advisable to use database aliases instead of the
path of the database file as it hides implementation details like file locations
and OS type.

On Linux the root `/` should be included in the path. A database located on
`/opt/firebird/db.fdb` should use (note the double slash after
port!):  

    jdbc:firebirdsql://host:port//opt/firebird/db.fdb

Deprecated, but still supported alternative URL format:

    jdbc:firebirdsql:host[/port]:<database>

Open Office/Libre Office (Pure Java)
------------------------------------

Jaybird can be used together with OpenOffice and Libre Office Base. To address
some compatibility issues (and differences in interpretation of JDBC
specifications) a separate subprotocol is used:

    jdbc:firebirdsql:oo://host[:port]/<database>

Native (using Firebird client library)
--------------------------------------

    jdbc:firebirdsql:native:host[/port]:<database>

Type 2 driver, will connect to the database using client library (`fbclient.dll`
on Windows, and `libfbclient.so` on Linux). Requires correct installation of the
client library and - for Jaybird 2.2 or earlier - the Jaybird native library, 
or - for Jaybird 3.0 - the JNA jar file.

    jdbc:firebirdsql:local:<database>

Type 2 driver in local mode. Uses client library as in previous case, however
will not use socket communication, but rather access database directly. Requires
correct installation of the client library and - for Jaybird 2.2 or earlier - 
the Jaybird native library, or - for Jaybird 3.0 - the JNA jar file.

Embedded Server
---------------

    jdbc:firebirdsql:embedded:<database>

Similar to the Firebird client library, however `fbembed.dll` on Windows and
`libfbembed.so` on Linux are used. Requires correctly installed and configured
Firebird embedded library and - for Jaybird 2.2 or earlier - the Jaybird native
library, or - for Jaybird 3.0 - the JNA jar file.

JDBC Support
============

How much of JDBC is supported by Jaybird?
-----------------------------------------

**WARNING** The information in this section is not 100% up-to-date

Jaybird 3 follows the JDBC 4.3 specification with some features and methods not
implemented as they aren't supported by Firebird.

Implemented features:

* Most useful JDBC functionality ("useful" in the opinion of the developers).
* Complete JCA API support: may be used directly in JCA-supporting application
  servers.
* XA transactions with true two phase commit when used as a JCA resource adapter
  in a managed environment (with a `TransactionManager` and JCA deployment
  support) as well as when used via `javax.sql.XADataSource` implementation.
* `ObjectFactory` implementation for use in environments with JNDI but no
  `TransactionManager`.
* `DataSource` implementations without pooling.
* Driver implementation for use in legacy applications.
* Complete access to all Firebird database parameter block and transaction
  parameter block settings.
* JMX mbean for database management (so far just database create and drop).

What parts of JDBC are NOT supported by JayBird?
------------------------------------------------

**WARNING** The information in this section is outdated

The following optional features are NOT supported:

The following optional features and the methods that support it are not
implemented:

* Ref and Array types.
    * `java.sql.PreparedStatement`
        * `setRef(int i, Ref x)`
        * `setArray(int i, Array x)`
    * `java.sql.ResultSet`
        * `getArray(int i)`
        * `getArray(String columnName)`
        * `getRef(int i)`
        * `getRef(String columnName)`
* User Defined Types/Type Maps.
    * `java.sql.ResultSet`
        * `getObject(int i, java.util.Map map)`
        * `getObject(String columnName, java.util.Map map)`
    * `java.sql.Connection`
        * `getTypeMap()`
        * `setTypeMap(java.util.Map map)`

Excluding the unsupported features, the following methods are not yet implemented:

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

Features
========

Does Jaybird support connection pooling?
----------------------------------------

Jaybird itself no longer provides connection pooling. Earlier versions had a
`DataSource` implementation with connection pooling, but this implementation had
severe bugs. This implementation (and all other classes in
`org.firebirdsql.pool`) was deprecated in 2.2 and dropped in 3.0.

Jaybird provides a basic `DataSource` implementation and a
`ConnectionPoolDataSource` implementation. Contrary to its name the latter **does
not provide a connection pool**, but is intended to be used by a connection pool
(as implemented in an application server) to create connections *for* the
connection pool.

If your application is built on a Java EE application server, we suggest you use
the connection pooling provided by the application server. Either through the
resource-adapter of the JCA implementation of Jaybird, or using the
`java.sql.ConnectionPoolDataSource` implementation
`org.firebirdsql.ds.FBConnectionPoolDataSource`.

If you develop standalone applications, or you use an application server without
connection pooling, we suggest you use third-party libraries like:

* [HikariCP](http://brettwooldridge.github.io/HikariCP/)
* [c3p0](http://www.mchange.com/projects/c3p0/)
* [Apache DBCP](http://commons.apache.org/proper/commons-dbcp/)

Compatibility notes
===================

Wildfly
-------

When you use Jaybird 3 in Wildfly (or JBoss), you will need to add the module 
`javax.xml.bind.api` to your module to get it to work.

The minimal `module.xml` to use Jaybird 3 under Wildfly is:

``` {.xml}
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.0" name="org.firebirdsql">
  <resources>
    <resource-root path="jaybird-3.0.x.jar"/>
  </resources>
  <dependencies>
    <module name="javax.api"/>
    <module name="javax.transaction.api"/>
    <module name="javax.resource.api"/>
    <module name="javax.xml.bind.api"/> <!-- Add this -->
  </dependencies>
</module>
```