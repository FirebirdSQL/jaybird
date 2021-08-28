# jdp-2021-01: Unified Database Coordinates

## Status

- Draft
- Proposed for: Jaybird 5

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

The various inconsistent properties also leads to jdp-2020-10 not being able to
fully unify connection properties, resulting in inconsistent handling and some
forms of similar but not identical code.

## Exploration of Options (temporary section)

1. Make the fundamental unit of connection information a URL-like thing (without
   the `jdbc:firebird[sql]:[<subprotocol>:]` prefix), leave interpretation of
   the URL to the protocol implementations.

2. Only at the edges (data sources and event manager) also allow specifying 
   existing triplets (e.g. (`serverName`, `portNumber`, `databaseName`)), where
   the implementation will generate the URL of point 1 using the currently 
   configured type (would need to regenerate when selecting a different type).

   For service manager, the same would be done for the tuple (`host`, `port`).

3. As an alternative to 1+2, allow both URL and triplets/tuples as fundamental
   coordinates. Before connect, the connection factory selects the URL, if
   present, or falls back to generating a URL (with help of the type 
   implementation) from the triplets/tuples.

4. In combination with 2, deprecate the triplets/tuples for removal to clean up
   the API

5. As an alternative to 1, allow (or require?) `jdbc:firebird[sql]:[<subprotocol>:]`
   to be included. We would need to come up with a way how this interacts with
   `type` or implementations that have a pre-determined type/protocol.

   Would also wreak havoc with `FbDatabaseFactory`. Maybe allow at edges?

6. ...

With regard to naming, we are in a bind. Ideally, we don't want to introduce new
properties and reuse `database` for the URL, but the existing confusion with 
`database`/`databaseName` (especially in `FBSimpleDataSource`, event manager and
service manager) results in either broken code, or brittle workarounds to
preserve current functionality. On the other hand, introducing yet another name
will increase and not reduce complexity (at least, unless we can deprecate and
eventually remove them).

Possible naming options for URLs:

- `database` for both database and service URLs

  Using `database` for service URLs doesn't make much sense.

- `database` for database URLs, `service` for service URLs.

  Accepts backwards incompatibilities?

- `databaseUrl` for database URLs, `serviceUrl` for service URLs.

- `url` for both database and service URLs.

  Possibly confusing as generic `DataSource`s (e.g. Apache DBCP) use this for
  the JDBC URL, and users may expect properties to be allowed in the URL.

- `datasource` for both database and service URLs.

  Maybe overloaded with usage of term in JDBC

## Decision

## Consequences
