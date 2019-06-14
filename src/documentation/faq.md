Jaybird
=======

Where do I get Jaybird?
-----------------------

### Maven ###

#### Jaybird 4 ####

NOTE: Jaybird 4 is in beta. Use [Jaybird 3.0] for production.

Jaybird 4 is available from Maven central:

Groupid: `org.firebirdsql.jdbc`,  
Artifactid: `jaybird-XX` (where `XX` is `jdk17`, `jdk18` or `java11`)  
Version: `4.0.0-beta-1`

For ease of use, we also provide a Maven relocation artifact with artifact id
`jaybird`. For Jaybird 4 this relocation artifact points to `jaybird-jdk18`.

For example, for Java 8:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>4.0.0-beta-1</version>
</dependency>
~~~

If your application is deployed to a Java EE application server, you will need to
exclude the `javax.resource:connector-api` dependency, and add it as a provided 
dependency:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>4.0.0-beta-1</version>
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
explicitly include JNA 5.3.0 as a dependency:

~~~ {.xml}
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>5.3.0</version>
</dependency>
~~~

#### Jaybird 3.0 ####

Jaybird 3.0 is available from Maven central:

Groupid: `org.firebirdsql.jdbc`,  
Artifactid: `jaybird-XX` (where `XX` is `jdk17` or `jdk18`)  
Version: `3.0.6`

For ease of use, we also provide a Maven relocation artifact with artifact id
`jaybird`. For Jaybird 3 this relocation artifact points to `jaybird-jdk18`.

For example, for Java 8:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>3.0.6</version>
</dependency>
~~~

If your application is deployed to a Java EE application server, you will need to
exclude the `javax.resource:connector-api` dependency, and add it as a provided 
dependency:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>3.0.6</version>
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

#### Jaybird 2.2 ####

Jaybird 2.2 is available on maven, with a separate artifact
for each supported Java version.

Groupid: `org.firebirdsql.jdbc`,  
Artifactid: `jaybird-XX` (where `XX` is `jdk16`, `jdk17` or `jdk18`)  
Version: `2.2.15`

For ease of use, we also provide a Maven relocation artifact with artifact id
`jaybird`. For Jaybird 2.2 this relocation artifact points to `jaybird-jdk17`.

For example:

~~~ {.xml}
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-jdk18</artifactId>
    <version>2.2.15</version>
</dependency>
~~~

When deploying to a JavaEE environment, exclude the `javax.resource connector-api`
dependency as this will be provided by the application server.

### Download ###

Firebird can be downloaded from the Firebird website, under Downloads,
[JDBC Driver](https://www.firebirdsql.org/en/jdbc-driver/).

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

### Which version of the LGPL applies? ###

Current releases of Jaybird do not explicitly specify an LGPL version. This
means that you can choose which version applies. Future versions of Jaybird may
specify an explicit version, or be released under a different license.

Which Java versions are supported?
----------------------------------

Jaybird 4 supports Java 7, 8, 11 and 12. Support for Java 9 and higher is 
limited to the latest LTS and current latest release, but in practice Jaybird
should work on all Java 9+ versions.

Jaybird 4 is the last version to support Java 7, support will be dropped with
Jaybird 5. Java 8 support may be dropped from Jaybird 5 as well depending on
the actual release timeline. 

Jaybird 3.0 supports Java 7 and 8 and has basic support for Java 9.

Jaybird 2.2 supports Java 6, 7 and 8. 

Jaybird 2.2.4 added basic support for Java 8 (JDBC 4.2), although not all 
JDBC 4.2 features are supported or fully implemented.

Jaybird 2.2.7 is the last version to support Java 5, support has been dropped
with Jaybird 2.2.8.

Jaybird 2.2 is the last version to support Java 6, support has been dropped with
Jaybird 3.0.

### What is the Java 9 module name for Jaybird?

Jaybird itself is not (yet) modularized. To ensure a stable module name, 
Jaybird, since 2.2.14 and 3.0.3, declares the automatic module name 
`org.firebirdsql.jaybird`.

Which Firebird versions are supported?
--------------------------------------

Jaybird 4 supports Firebird version 2.5 and higher, and introduces support for
Firebird 4 types `DECLOAT`, extended precision of `NUMERIC` and `DECIMAL`, and 
time zone types (`TIME WITH TIME ZONE` and `TIMESTAMP WITH TIME ZONE`).

Jaybird 3.0 supports Firebird versions 2.0 and higher. Support for Firebird 4 is
limited to the Firebird 3 feature set.

Jaybird 3.0 is the last version to support Firebird 2.0 and 2.1. Future versions
of Jaybird are not guaranteed to work with version 2.1 and earlier.

Jaybird 2.2 supports all Firebird versions 1.0 and higher. Jaybird 2.2.4 added
support for new features of Firebird 3 (eg `BOOLEAN` support). Support for 
Firebird 4 is limited to the Firebird 3 feature set.

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
* [Jaybird JDBC Driver Java Programmer’s Manual](https://firebirdsql.github.io/jaybird-manual/jaybird_manual.html)
(covers Jaybird 2.2 and higher, work in progress)
* [Jaybird 2.1 JDBC driver - Java Programmers Manual](https://www.firebirdsql.org/file/documentation/drivers_documentation/Jaybird_2_1_JDBC_driver_manual.pdf) (PDF)
* [Firebird Website: Development, JDBC Driver](https://www.firebirdsql.org/en/devel-jdbc-driver/)

For version specific details, consult the release notes

* [Jaybird 4.0.x release notes](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/4.0.x/release_notes.html)
* [Jaybird 3.0.x release notes](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/3.0.x/release_notes.html)
* [Jaybird 2.2.x release notes](https://www.firebirdsql.org/file/documentation/drivers_documentation/java/2.2.x/release_notes.html)

Where to get help
-----------------

*   On [Stack Overflow](https://stackoverflow.com/), please tag your questions
    with *jaybird* and *firebird*
    
*   The [Firebird-Java group](http://groups.yahoo.com/group/Firebird-Java) and
    corresponding mailing list firebird-java@yahoogroups.com

    You can subscribe to the mailing list by sending an email to
    [firebird-java-subscribe@yahoogroups.com](mailto:firebird-java-subscribe@yahoogroups.com)

*   Looking for professional support of Jaybird? Jaybird is now part of the [Tidelift subscription](https://tidelift.com/subscription/pkg/maven-org-firebirdsql-jdbc-jaybird?utm_source=maven-org-firebirdsql-jdbc-jaybird&utm_medium=referral&utm_campaign=docs).

*   The [Firebird project home page](https://www.firebirdsql.org)

*   Firebird support and other [Firebird mailing lists](https://www.firebirdsql.org/en/mailing-lists/)
    for questions not directly related to Jaybird and java.

Contributing
------------

There are several ways you can contribute to Jaybird or Firebird in general:

* Participate on the mailing lists (see <https://www.firebirdsql.org/en/mailing-lists/>)
* Report bugs or submit patches on the tracker (see [Reporting Bugs])
* Create pull requests on GitHub (<https://github.com/FirebirdSQL/jaybird>)
* Become a developer (for Jaybird contact us on firebird-java, for Firebird in
  general, use the Firebird-devel mailing list)
* Become a paying member or sponsor of the Firebird Foundation (see
  <https://www.firebirdsql.org/en/firebird-foundation/>)

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

Connecting to Firebird
======================

JDBC URLs (`java.sql.DriverManager`)
------------------------------------

### Pure Java (default)

Default URL format:

    jdbc:firebirdsql://host[:port]/<database>
    
This will connect to the database using the Type 4 JDBC driver using the Java
implementation of the Firebird wire-protocol. This is best suited for
client-server applications with dedicated database server. Port can be omitted
(default value is `3050`), host name must be present.

The `<host>` part is either the hostname, the IPv4 address, or the IPv6 address 
in brackets (eg `[::1]`). Use of IPv6 address literals is only supported in 
Jaybird 3 or newer with Firebird 3 or newer.

The `<database>` part should be replaced with the database alias or the path to
the database. In general it is advisable to use database aliases instead of the
path of the database file as it hides implementation details like file locations
and OS type.

On Linux the root `/` should be included in the path. A database located on
`/opt/firebird/db.fdb` should use (note the double slash after port!):  

    jdbc:firebirdsql://host:port//opt/firebird/db.fdb
    
Deprecated, but still supported legacy URL format:

    jdbc:firebirdsql:host[/port]:<database>

The legacy URL format does not support IPv6 address literals.

Jaybird 4 and higher also support:

    jdbc:firebird://host[:port]/<database>
    jdbc:firebird:host[/port]:<database>

### Open Office/Libre Office (Pure Java)

Jaybird can be used together with OpenOffice and Libre Office Base. To address
some compatibility issues (and differences in interpretation of JDBC
specifications) a separate subprotocol is used:

    jdbc:firebirdsql:oo://host[:port]/<database>

Jaybird 4 and higher also support:

    jdbc:firebird:oo://host[:port]/<database>

### Native (using Firebird client library)

Default URL format:

    jdbc:firebirdsql:native://host[:port]/<database>

Legacy URL format:

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

Jaybird 4 and higher also support:

    jdbc:firebird:native://host[:port]/<database>
    jdbc:firebird:native:host[/port]:<database>
    jdbc:firebird:local:<database>

### Embedded Server

    jdbc:firebirdsql:embedded:<database>

Similar to the Firebird client library, however `fbembed.dll` on Windows and
`libfbembed.so` on Linux are used. Requires correctly installed and configured
Firebird embedded library and - for Jaybird 2.2 or earlier - the Jaybird native
library, or - for Jaybird 3.0 - the JNA jar file.

Jaybird 4 and higher also support:

    jdbc:firebird:embedded:<database>
    
Character sets
--------------

### How can I specify the connection character set?

Jaybird provides two connection properties to specify the connection character set:

-   `charSet` with a Java character set name (alias: `localEncoding`)

    The Java character set name must map to an equivalent Firebird character set.

-   `encoding` with a Firebird character set name (alias: `lc_ctype`)

    The Firebird character set name - with the exception of `NONE` must map to
    an equivalent Java character set.

For most applications, use only one of these two properties.

For special situations it is possible to specify both `charSet` and `encoding` to
convert/reinterpret a character set into another character set, this is usually only
necessary to fix data problems.

To phrase differently:

-   `encoding=<firebird charset>`: use connection encoding `<firebird charset>` and 
    interpret in the equivalent Java character set
    
-   `charSet=<java charset>`: use Firebird equivalent of `<java charset>` as 
    connection encoding and interpret in <java charset>
    
-   `encoding=<firebird charset>&charSet=<java charset>`: use connection encoding 
    `<firebird charset>`, but interpret in `<java charset>`

The handling of Firebird character set `NONE` is slightly different, see below.

### How does character set `NONE` work?

The Firebird character set `NONE` is a special case, it essentially means "no 
character set". You can store anything in it, but conversions to or from this
character set are not defined.

Using character set `NONE` can result in incorrect character set handling when 
the database is used from different locales.

When used as a connection character set, Jaybird handles `NONE` as follows:

#### Jaybird 3.0 {#none-jaybird3}

-   `encoding=NONE` means connection encoding `NONE` and interpret columns with 
    character set `NONE` using the default JVM encoding, and interpret columns
    with an explicit character set in their equivalent Java character set
    
-   `encoding=NONE&charSet=ISO-8859-1` the same, but instead of the JVM default,
    use `ISO-8859-1`

#### Jaybird 2.2 and earlier {#none-jaybird2-2}

-   `encoding=NONE` means use connection encoding `NONE` and interpret everything 
    using the default JVM encoding

-   `encoding=NONE&charSet=ISO-8859-1` the same, but instead of the JVM default, 
    use `ISO-8859-1`

### What happens if no connection character set is specified?

When no character set has been specified explicitly, Jaybird 2.2 and earlier, 
and Jaybird 3.0.2 and higher default to connection character set `NONE`. See 
[How does character set `NONE` work?] for details on character set `NONE`.
 
Jaybird 3.0.0 and 3.0.1, however, will reject the connection, see
[How can I solve the error "Connection rejected: No connection character set specified"].

In Jaybird 3 it is possible to override the default connection character set by
specifying system property `org.firebirdsql.jdbc.defaultConnectionEncoding` with
a valid Firebird character set name. 

Jaybird 3.0.2 introduces the system property `org.firebirdsql.jdbc.requireConnectionEncoding`,
which - when set to `true` - will reject connections without a character set (which 
was the default behavior in Jaybird 3.0.0 and 3.0.1).

### How can I solve the error "Connection rejected: No connection character set specified"

If no character set has been set, Jaybird 3.0 will reject the connection with 
an `SQLNonTransientConnectionException` with message 
_"Connection rejected: No connection character set specified (property lc_ctype,
encoding, charSet or localEncoding). Please specify a connection character set 
(eg property charSet=utf-8) or consult the Jaybird documentation for more 
information."_

In Jaybird 3.0.0 and 3.0.1 this error will be thrown if the character set has 
not been set explicitly. In Jaybird 3.0.2 and higher this error will only be 
thrown if system property `org.firebirdsql.jdbc.requireConnectionEncoding` has
been set to `true`. 

To address this error, you can set the default connection character set using
one of the following options:

*   Use connection property `encoding` (alias: `lc_ctype`) with a Firebird character
    set name. 
    
    Use `encoding=NONE` for the default behavior (with some caveats, see 
    [How does character set `NONE` work?]).

*   Use connection property `charSet` (alias: `localEncoding`) with a Java character
    set name.
    
*   Use a combination of `encoding` and `charSet`, if you want to reinterpret a 
    Firebird character set in a Java character set other than the default 
    mapping.

*   By providing a default Firebird character set with system property 
    `org.firebirdsql.jdbc.defaultConnectionEncoding`. Jaybird will apply the
    specified character set as the default when no character set is specified
    in the connection properties.
    
    This property only supports Firebird character set names.

    Use `-Dorg.firebirdsql.jdbc.defaultConnectionEncoding=NONE` to revert to the
    default behavior (with some caveats, see [How does character set `NONE` work?]).
    With Jaybird 3.0.2 or higher, it is better to just not set system property 
    `org.firebirdsql.jdbc.requireConnectionEncoding` if you want to apply `NONE`.
    
How can I enable the Windows "TCP Loopback Fast Path" introduced in Firebird 3.0.2?
-----------------------------------------------------------------------------------

Firebird 3.0.2 adds support for "TCP Loopback Fast Path" (`SIO_LOOPBACK_FAST_PATH` 
socket option). This is available in Windows 8 / Windows Server 2012 and higher.
This feature enables performance optimizations when connecting through 
localhost (127.0.01 / ::1). It requires support on both client and server side.

Java support for "TCP Loopback Fast Path" was introduced in Java 8 update 60, 
it can be enabled by specifying the system property `jdk.net.useFastTcpLoopback` 
with value `true` (eg specify `-Djdk.net.useFastTcpLoopback=true` in your Java 
commandline).
  
Unfortunately, Java only has an 'all-or-nothing' support for the "TCP Loopback 
Fast Path", so Jaybird cannot enable this for you: you must specify this 
property on JVM startup. On the other hand, this has the benefit that this works 
for all Jaybird versions, as long as you use Java 8 update 60 or higher (and 
Firebird 3.0.2 or higher).

Common connection errors
------------------------

### Your user name and password are not defined. Ask your database administrator to set up a Firebird login. (335544472) ###

This error means that the user does not exist, or that the specified password is
not correct.

When connecting to Firebird 3 and higher, this error can also mean that the user
does exist (with that password), but not for the authentication plugins tried 
for this connection.

For example, Jaybird 2.2.x and earlier only support legacy authentication, if
you try to login as a user created for SRP authentication, you will get the same
error.

### Incompatible wire encryption levels requested on client and server (335545064) ###  

With Jaybird 3.0.0 - 3.0.3 connecting to Firebird 3 or higher, this usually 
means that the setting `WireCrypt` is set to its (default) value of `Required`.

Upgrade to Jaybird 3.0.4 or higher, or relax this setting (in `firebird.conf`) 
to `WireCrypt = Enabled`.

See also [Jaybird Wiki - Jaybird and Firebird 3](https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3)

With Jaybird 3.0.4 or higher, or Jaybird 4, this error means that you have 
requested a connection with a mismatch in encryption settings. For example, you 
specified connection property `wireCrypt=required` while Firebird is set to 
`WireCrypt = Disabled` (or vice versa).

### connection rejected by remote interface (335544421) ###

In general this error means that Jaybird requested a connection with properties 
not supported by Firebird. It can have other causes than described below.

#### Cause: user name or password is null ####

With Jaybird 3 and higher connecting to Firebird 3 or higher, leaving user name 
or password null will lead to Jaybird not trying any authentication plugin, and
as a result, Firebird will reject the connection.

With Firebird 2.5 and earlier, or Jaybird 2.2, this situation will normally
yield error _"Your user name and password are not defined. Ask your database 
administrator to set up a Firebird login."_. 

#### Cause: wirecrypt required ####

With Jaybird 2.2.x connecting to Firebird 3 or higher, this usually means that 
the setting `WireCrypt` is set to its (default) value of `Required`.

Relax this setting (in `firebird.conf`) to `WireCrypt = Enabled`.

See also [Jaybird Wiki - Jaybird and Firebird 3](https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3)

Make sure you check the other settings mentioned in that article, otherwise 
you'll get the next error.

### Error occurred during login, please check server firebird.log for details (335545106) ###

If the logging contains something like

```
SERVER	Sat Oct 28 10:07:26 2017
	Authentication error
	No matching plugins on server
```

with Jaybird 2.2.x connecting to Firebird 3 or higher, this means that the 
setting `AuthServer` does not include the `Legacy_Auth` plugin.

Enable `Legacy_Auth` (in `firebird.conf`) by adding this value to the property 
`AuthServer`, for example: `AuthServer = Srp, Legacy_Auth`.

With Jaybird 4 and higher this can also mean that none of the default 
authentication plugins or those specified using connection property 
`authPlugins`, are listed in the `AuthServer` setting. Either revise the
Firebird configuration, or explicitly configure connection property `authPlugins`
with authentication plugins that are configured in Firebird.

You also need to make sure your user is created with the legacy user manager,
see [Jaybird Wiki - Jaybird and Firebird 3](https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3) 
for details.

### Encryption key did not meet algorithm requirements of Symmetric/Arc4 (337248282) ###

If the exception cause is _java.security.InvalidKeyException: Illegal key size 
or default parameters_, this means that your Java install applies a security 
policy that does not allow ARCFOUR with a 160 bit encryption key.

If `wireCrypt=ENABLED` (the default), this is just logged as a warning. The 
connection will succeed, but it does mean that the connection will not be 
encrypted. If `wireCrypt=REQUIRED`, this is thrown as an exception, and the 
connection will fail.

This could indicate that your Java version applies the limited strength 
Cryptographic Jurisdiction Policy (this was the default in Java 8 Update 152 and 
earlier), or has been explicitly configured to apply the limited policy, or has
a custom security policy to restrict the cryptographic key size. 

Solutions and workarounds:

- Apply the unlimited Cryptographic Jurisdiction Policy, see [this Stack 
Overflow answer](https://stackoverflow.com/a/3864276/466862)
- Relax your custom security policy to allow 160 bit keys for ARCFOUR
- Disable wire encryption for Firebird by setting `WireCrypt = Disabled` in 
`firebird.conf`
- Set `wireCrypt=DISABLED` in the connection properties

Be aware that the first two options may have legal implications depending on the
local law in your country regarding cryptography.

JDBC Support
============

How much of JDBC is supported by Jaybird?
-----------------------------------------

**WARNING** The information in this section is not 100% up-to-date

Jaybird 3 follows the JDBC 4.3 specification with some features and methods not
implemented as they are not supported by Firebird.

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

What parts of JDBC are NOT supported by Jaybird?
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
* `java.sql.CallableStatement`
    * `getBigDecimal(index,scale)` This method is implemented but behaves as `getBigDecimal(index)`. 
      The method is deprecated, and we suggest to use `getBigDecimal(index)` and adjust the scale of
      the returned `BigDecimal` using `BigDecimal.setScale(newScale,roundingMode)` 
* `java.sql.ResultSetMetaData`
    * `isReadOnly(i)` always returns false
    * `isWritable(i)` always returns true
    * `isDefinitivelyWritable(i)` always returns true
* `java.sql.ResultSet`
    * `getBigDecimal(index,scale)` This method is implemented but behaves as `getBigDecimal(index)`. 
      The method is deprecated, and we suggest to use `getBigDecimal(index)` and adjust the scale of
      the returned `BigDecimal` using `BigDecimal.setScale(newScale,roundingMode)`

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
  </dependencies>
</module>
```

With Jaybird 3.0.4 and higher for Java 7 (but not Java 8!) in Wildfly (or JBoss), 
you will need to add the module `javax.xml.bind.api` to your module:

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

Alternatively, use Jaybird for Java 8 (or higher).
