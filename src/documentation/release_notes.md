Jaybird 5.0.x changelog
=======================

Changes per Jaybird 5 release. See also [What's new in Jaybird 5](#whats-new-in-jaybird-5). For known
issues, consult [Known Issues](#known-issues).

...

Known issues
============

-   Using a native connection with a Firebird 3 client library to a Firebird 2.5
    or older server may be slow to connect. The workaround is to specify the
    IPv4 address instead of the host name in the connection string, or to use a
    Firebird 2.5 or earlier `fbclient.dll`.
    
    This is caused by [CORE-4658](http://tracker.firebirdsql.org/browse/CORE-4658)

Support
=======

If you need support with Jaybird, join the Firebird-Java mailing list. You can
subscribe by sending an email to [firebird-java-subscribe@yahoogroups.com](mailto:firebird-java-subscribe@yahoogroups.com).

Looking for professional support of Jaybird? Jaybird is now part of the [Tidelift subscription](https://tidelift.com/subscription/pkg/maven-org-firebirdsql-jdbc-jaybird?utm_source=maven-org-firebirdsql-jdbc-jaybird&utm_medium=referral&utm_campaign=docs).

See also [Where to get help](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#where-to-get-help)

General Notes
=============

Jaybird is a JDBC driver suite to connect to Firebird database servers from Java
and other Java Virtual Machine (JVM) languages.

About this version
------------------

...

Bug reports about undocumented changes in behavior are appreciated. Feedback can
be sent to the Firebird-java mailing list or reported on the issue tracker
<http://tracker.firebirdsql.org/browse/JDBC>.

Supported Firebird versions
---------------------------

Jaybird @VERSION_WO_TARGET@ was tested against Firebird 2.5.9, 3.0.5, and
a recent Firebird 4 snapshot build, but should also support other Firebird
versions from 2.5 and up.

This driver does not support InterBase servers due to Firebird-specific changes
in the protocol and database attachment parameters that are sent to the server.

Supported Java versions
-----------------------

Jaybird 5 supports Java 8 (JDBC 4.2), and Java 9 and higher (JDBC 4.3). Support
for earlier Java versions has been dropped.

Given the limited support period for Java 9 and higher versions, we will limit
support on those versions to the most recent LTS version and the latest release.
Currently that means we support Java 11 and Java 13.

Jaybird 5 provides libraries for Java 8 and Java 11. The Java 8 builds have the
same source and all JDBC 4.3 related functionality and can be used on Java 9 and
higher as well.

Jaybird 5 is not modularized, but all versions declare the automatic module name 
`org.firebirdsql.jaybird`.

See als [Java support](#java-support) in [What's new in Jaybird 5](#whats-new-in-jaybird-5).

Specification support
---------------------

Jaybird supports the following specifications:

|Specification|Notes
|-------------|----------------------------------------------------------------
| JDBC 4.3    | All JDBC 4.3 methods for features supported by Firebird; Java 9 and higher supported using the Java 8 or Java 11 driver.
| JDBC 4.2    | All JDBC 4.2 methods for features supported by Firebird.
| JTA 1.0.1   | Implementation of `javax.transaction.xa.XAResource` interface via `XADataSource` implementation.

Getting Jaybird 5
=================

Jaybird @VERSION_WO_TARGET@
---------------------------

### Maven ###

Jaybird @VERSION_WO_TARGET@ is available from Maven central: 

Groupid: `org.firebirdsql.jdbc`,\
Artifactid: `jaybird`,\
Version: `@VERSION_SIMPLE@.javaXX@VERSION_TAG@` (where `XX` is `8` or `11`).

For ease of transition to the new artifact naming, we also provide a Maven
relocation artifact with artifact id `jaybird-jdkXX` (with `XX` is `18`).
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

At minimum Jaybird 5 requires `jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar` 
(where `XX` is `8` or `11`) and `connector-api-1.5.jar`. You can also use 
`jaybird-full-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar` which includes
the connector-api files.

If you deploy your application to a Java EE application server, then you must 
use `jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar` (not `-full`!), and **not**
include `connector-api-1.5.jar` as this dependency will be provided by your
application server.

For `getGeneratedKeys` support you will need to include 
`antlr-runtime-4.7.2.jar` on your classpath.

For native, local or embedded support, you will need to include `jna-5.5.0.jar` 
on your classpath. See also [Type 2 (native) and embedded driver](#type-2-native-and-embedded-driver).

Upgrading from Jaybird 4 to Jaybird 5
=====================================

Please make sure to read [Compatibility changes](#compatibility-changes) before
upgrading to Jaybird 5.

Maven
-----

Change the artifact id from `jaybird-jdkXX` to `jaybird`, and change the version
of the dependency to `@VERSION_SIMPLE@.javaXX@VERSION_TAG@` (where `XX` is your
Java version, `8` for Java 8 and `11` for Java 11).

For more detailed instructions, see also the information on Maven in
[Getting Jaybird 5](#getting-jaybird-5). 

Manual install
--------------

If you manage your dependencies manually, you need to do the following:

1.  Replace the Jaybird 4 library with the Jaybird 5 version
    - `jaybird-3.0.x.jar` with `jaybird-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar`
    (where `XX` is `8` or `11`) 
    - `jaybird-full-4.0.x.jar` with `jaybird-full-@VERSION_SIMPLE@.javaXX@VERSION_TAG@.jar`
    
Gotcha's
--------

If you find a problem while upgrading, or other bugs: please report it 
on <http://tracker.firebirdsql.org/browse/JDBC>.

For known issues, consult [Known Issues](#known-issues).

What's new in Jaybird 5
=======================

For a full list of changes, see [Firebird tracker for Jaybird 5](http://tracker.firebirdsql.org/secure/ReleaseNote.jspa?projectId=10002&styleName=Text&version=10441).

Java support
------------

### Java 7 support dropped ###

Java 7 is no longer supported. See also [jdp-2020-02 Drop Java 7 support](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-02-drop-java-7-support.md).

### Java 8 ###

The driver supports Java 8. Depending on the actual release timeline of
Jaybird 5, Java 8 support might be dropped before release.

### Java 9 and higher ###

Jaybird 5 supports Java 9 and higher (JDBC 4.3) with the Java 8 and 11 version 
of the driver. Most of the JDBC 4.3 features have been implemented (in as far 
as they are supported by Firebird).

You can use the Java 8 driver under Java 9 and higher. For Java 11 or higher we 
recommend to use the Java 11 driver, though its sources are identical to the 
Java 8 driver. 

Given the limited support period for Java 9 and higher versions, we limit 
support on those versions to the most recent LTS version and the latest release.
Currently that means we support Java 7, 8, 11 and 13.

For compatibility with Java 9 modules, Jaybird defines the automatic module name 
`org.firebirdsql.jaybird`. This guarantees a stable module name for Jaybird, and 
allows for future modularization of Jaybird.

Firebird support
----------------

Firebird versions 2.5, 3.0 and (upcoming) 4.0 are supported.

Potentially breaking changes
----------------------------

Jaybird 5 contains a number of changes that might break existing applications.

See also [Compatibility changes](#compatibility-changes) for details.

Other fixes and changes
-----------------------

...

Removal of deprecated classes and packages
------------------------------------------

See [Removal of deprecated classes, packages and methods](#removal-of-deprecated-classes-packages-and-methods)
in [Compatibility changes](#compatibility-changes) for more details.

Compatibility changes
=====================

Jaybird 5 introduces some changes in compatibility and announces future
breaking changes.

**The list might not be complete, if you notice a difference in behavior that is
not listed, please report it as bug.** It might have been a change we forgot to
document, but it could just as well be an implementation bug.

Support for Java 7 dropped
--------------------------

Jaybird 5 does not support Java 7. You will need to upgrade to Java 8 or higher,
or remain on Jaybird 4.

Removal of classes, packages and methods without deprecation
------------------------------------------------------------

### Removal of methods without deprecation ###

The following methods have been removed in Jaybird 5:

-   `JdbcVersionSupport.createBatchUpdateException`, the `JdbcVersionSupport`
    interface is an internal API of Jaybird, but was previously not marked as
    such.

### Removal of constants without deprecation ###

The following constants have been removed in Jaybird 5:

-   `TIME_WITH_TIMEZONE` and `TIMESTAMP_WITH_TIMEZONE` from
    `org.firebirdsql.jdbc.JaybirdTypeCodes`. Use the constants with the same
    name from `java.sql.Types`.

Removal of deprecated classes, packages and methods
---------------------------------------------------

### Removal of deprecated methods ###

The following methods have been removed in Jaybird 5:

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

The following classes have been removed in Jaybird 5:

-   `FBMissingParameterException`, exception is no longer used.
    
### Removal of deprecated constants ###

The following constants have been removed in Jaybird 5:

-   All `SQL_STATE_*` constants in `FBSQLParseException`. Use equivalent
    constants in `org.firebirdsql.jdbc.SQLStateConstants`.
-   `DatabaseParameterBufferExtension.EXTENSION_PARAMETERS` has been removed.
    There is no official replacement as this should be considered an
    implementation detail. It is possible that `DatabaseParameterBufferExtension`
    will be removed entirely.
    
Removal of UDF support for JDBC escapes
---------------------------------------

Given recent Firebird versions have significantly improved support for built-in
functions, and UDFs are now deprecated, the support to map JDBC function escapes
to UDFs from `ib_udf` instead of built-in functions using the boolean connection
property `useStandarUdf`\[sic\] has been removed.

As a result, the following methods, constants, properties and others are no
longer available:

-   Connection property `useStandarUdf`\[sic\] and its alias `use_standard_udf`
-   `isUseStandardUdf()` and `setUseStandardUdf(boolean useStandardUdf)` in
    `FirebirdConnectionProperties` and in implementations of `DataSource` and
    other classes
-   Constants `FBConnectionProperties.USE_STANDARD_UDF_PROPERTY`, 
    `DatabaseParameterBufferExtension.USE_STANDARD_UDF`,
    `ISCConstants.isc_dpb_use_standard_udf`
-   Enum `EscapeParserMode` and its usages in `FBEscapedCallParser` and
    `FBEscapedParser`
-   Public classes in package are now marked as internal-api 

Breaking changes for Jaybird 5
------------------------------

<!-- TODO Move relevant parts to Compatibility changes once actually removed -->

With Jaybird 5 the following breaking changes will be introduced.

### Dropping support for Java 8 (tentative) ###

Jaybird 5 may drop support for Java 8, depending on the actual release time line.

This decision is not final yet.

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
