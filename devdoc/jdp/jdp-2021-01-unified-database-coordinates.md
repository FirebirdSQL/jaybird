# jdp-2021-01: Unified Database Coordinates

## Status

- Published: 2021-09-25
- Implemented in: Jaybird 5

## Type

- Feature-Specification

## Context

Jaybird - as at Jaybird 4 - has multiple ways to specify the 'coordinates' of a
Firebird database or service:

1. JDBC URLs, optionally with connection properties
2. `database` property on `FBXADataSource`, `FBConnectionPoolDataSource`,
   `FBSimpleDataSource`, `FBManagedConnectionFactory` and
   `FBConnectionProperties` using the same format as JDBC URLs, but without the
   `jdbc:firebird[sql]:[<subprotocol>:]` prefix and without connection 
   properties.
3. `databaseName` as an alias to `database` on `FBSimpleDataSource` (deprecated,
   but retained for compatibility)
4. The triplet (`serverName`, `portNumber`, `databaseName`) on `FBXADataSource`,
   `FBConnectionPoolDataSource`, and `IConnectionProperties` with semantics 
   that vary. For example, in `FBXADataSource` and `FBConnectionPoolDataSource`,
   the values are used to build `database`, while in `IConnectionProperties`,
   the values are used directly when connecting using PURE_JAVA, but used to
   generate a `fbclient` connection string for NATIVE, LOCAL and EMBEDDED.
5. `attachObjectName` as an alias to `databaseName` in `IConnectionProperties`.
6. `serviceName` as equivalent to `databaseName` (with alias `attachObjectName`)
   in `IServiceProperties`.
7. The triplet (`database`, `host`, `port`) on `EventManager`, and 
   `ServiceManager`. For `ServiceManager`, the tuple (`host`, `port`) identifies
   the service itself (always using `service_mgr` as the service, and usage of 
   `database` varies by service operation)

As a result, we may transform a URL (`database`) to triplet (`serverName`, 
`portNumber`, `databaseName`), and then back to a URL (possibly multiple times). 

We also cannot support the native URLs introduced in Firebird 3.0 and later,
because at the lowest level, we use the triplet (`serverName`, `portNumber`, 
`attachObjectName`), leaving it to the native protocol implementations to 
construct the URL.

In the future we may want to add failover support in the URLs (i.e. specifying
multiple host+port combinations, or even host+port+database combinations), which
is not possible in the current setup.

The various inconsistent properties also leads to [jdp-2020-10](jdp-2020-10-propagation-of-connection-properties.md)
not being able to fully unify connection properties, resulting in inconsistent
handling and some forms of similar but not identical code.

JDBC 4.3, section 9.6.1, specifies the properties `serverName`, `portNumber` and
`databaseName` as standard properties for data sources (though only 
`description` is really required by the specification).

## Decision

The primary coordinate for a database is `databaseName` (with `database` as an
alias if already present, deprecated for removal). The primary coordinate for a
service is `serviceName`. Internally, in the `IAttachProperties` hierarchy, these
two are aliased by `attachObjectName` depending on the type of property holder.

The properties `serverName` (with `host` as alias if already present, deprecated
for removal) and `portNumber` (with `port` as alias if already present, 
deprecated for removal) are also available. The `serverName` property no longer
has an explicit default.

If `serverName` is not `null`, and the protocol implementation supports
hostnames (e.g. PURE_JAVA, NATIVE, OOREMOTE), the protocol implementation uses
the triplet (`serverName`, `portNumber`, `databaseName`) to connect, using
`databaseName` as if it is only a database path or alias.

For PURE_JAVA and OOREMOTE, if `serverName` is `null` and the implementation
cannot parse a hostname from `databaseName` (i.e. it is not of the format
`//<host>[:<port>]/<path>` or `<host>[/port]:<path>`), it will assume
`localhost` and use `databaseName` as database path or alias.

For NATIVE, if `serverName` is `null`, values of `databaseName` starting with
`//` will be parsed as `//<host>[:<port>]/<path>` and transformed to the
`fbclient` legacy URL `<host>[/port]:<path>`. All other values will be used as
is.

Similar behaviour applies for services, with the added rule that if `serviceName`
is `null`, then `service_mgr` is assumed. For protocols that require hostnames,
if both `serviceName` and `serverName` are `null`, a default of `localhost` 
(with `service_mgr`) is used.

### Rejected design decisions

- Only support triplets (e.g. (`serverName`, `portNumber`, `databaseName`)) at
  the edges (i.e. data source, event manager, etc.).

  Protocol-specific interpretation of information means it makes more sense to
  push this down into the protocol implementations.

- Solution as specified, but `serverName` and `portNumber` deprecated for 
  removal, as transitory solution.

  Given JDBC specifies these properties, it makes sense to keep them. We can
  always remove them later.

- As an alternative to 1, allow (or require?) `jdbc:firebird[sql]:[<subprotocol>:]`
  to be included. 

  We would need to come up with a way how this interacts with `type` or 
  implementations that have a pre-determined type/protocol. It also doesn't make
  sense in `FbDatabaseFactory`. We could allow this in the future, on the edges.

- Naming:

  - Making `database` the primary name.

    `databaseName` is defined in JDBC.
  
  - Using `databaseUrl`/`serviceUrl` instead of `databaseName`/`serviceName`

    Introduced yet another name.

  - Using `datasource` instead of `databaseName`/`serviceName`

    Introduced yet another name. Overloaded with usage of term in JDBC 

  - Using `url`

    Possibly confusing as generic `DataSource`s (e.g. Apache DBCP) use this for
    the JDBC URL, and users may expect connection properties to be allowed in
    the URL. 

  - Using `service` instead of `serviceName`

    Name `serviceName` was already used in `IServiceProperties`.

## Consequences

The property `serverName` no longer defaults to `localhost` (except in the 
situations detailed above). The property `portNumber` will be ignored if
`serverName` is `null`, unless one of the exceptions applies.

The NATIVE protocol will no longer default to `localhost` if no server name is
specified.

The LOCAL protocol is obsolete and has been removed. The JDBC sub-protocol
`local` is now an alias for `native`, and the type name `LOCAL` is now
an alias for `NATIVE`.

The property `database` on data sources, etc., is deprecated for removal.
The properties `host` and `port` on service managers and event manager are
deprecated for removal, the properties `serverName` and `portNumber` are
added as replacement.

The property `serviceName` is added to service managers.

Although discouraged, it will be possible to specify `serverName`, `portNumber`
and `databaseName` as connection properties in the JDBC URL. This can result in
oddities. For example, `jdbc:firebird://localhost/abc?databaseName=//someServer/cde`
will connect to database `cde` on server `someServer`, while 
`jdbc:firebird://localhost/abc?serverName=someServer` will attempt to open
database `//localhost/abc` on server `someServer`. It will also be possible to
construct URLs like `jdbc:firebird:?databaseName=//localhost/abc`.

The NATIVE protocol now supports the `fbclient` connection URLs introduced in
Firebird 3.0. As an artifact of implementation, the EMBEDDED protocol also
supports these URLs.

For implementation reasons, `serviceName`, `databaseName`, `database` and 
`attachObjectName` are handled as aliases of each other in 
`setProperty`/`getProperty` and JDBC properties.
