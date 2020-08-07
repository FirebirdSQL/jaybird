# jdp-2020-10: Propagation of connection properties

## Status

- Draft

## Type

- Feature-Specification

## Context

The API of Jaybird provides connection properties at several levels:

1. For `DriverManager` with connection properties in the JDBC url and 
   `Properties` object
2. Through implementations of `org.firebirdsql.jdbc.FirebirdConnectionProperties`:
   - `org.firebirdsql.ds.FBAbstractCommonDataSource`
   - `org.firebirdsql.ds.FBConnectionPoolDataSource`
   - `org.firebirdsql.ds.FBSimpleDataSource`
   - `org.firebirdsql.ds.FBXADataSource`
   - `org.firebirdsql.jaybird.xca.FBManagedConnectionFactory`
   - `org.firebirdsql.jdbc.FBConnectionProperties` (most of the above classes
     delegate directly or indirectly to this type)
3. Through implementations of `org.firebirdsql.management.ServiceManager` (for
   service connections)
3. Through implementations of `org.firebirdsql.gds.DatabaseParameterBuffer` and
   `org.firebirdsql.gds.ServiceParameterBuffer`
4. Implementations of `org.firebirdsql.gds.ng.IConnectionProperties` (and its
   sibling `org.firebirdsql.gds.ng.IServiceProperties`)
   
The interaction between these types is complex. For example, when creating a
connection using `DriverManager`, the properties from the URL and `Properties`
object are parsed and unified, then set on `FBManagedConnectionFactory` (which
holds them in an instance of `FBConnectionProperties`),
`FBManagedConnectionFactory` will convert its configuration to a
`DatabaseParameterBuffer`, which is then used to populate a
`FbConnectionProperties` instance. `FbConnectionProperties` in turn holds a
`DatabaseParameterBuffer` for 'extra', unsupported properties. When the
connection is created, yet another `DatabaseParameterBuffer` is created and
populated to generate the actual DPB bytes to send to the Firebird server.

The properties exposed in the URL and `FirebirdConnectionProperties` are all
mapped to DPB items (either Firebird defined, or Jaybird specific), while the
properties in `IConnectionProperties` are not necessarily connected to a DPB
item.

The current situation has a number of problems:

1. Complexity and mental overhead due the layers of conversion
2. Specifying properties thrice (or maybe even five times if you consider
   services)
3. Implementing properties multiple times (eg in data sources, managed
   connection factory, etc)
3. Impossible to allow 'custom' properties for plugins, as DPB items must be
   declared in Jaybird itself
4. In `DatabaseParameterBuffer`, the boolean properties are defined by their
   existence (true) or non-existence (false), which disallows properties with a
   default of true unless the default is made explicit at the edge instead of
   in the core.
   
Ideally, properties should be specified once (eg in a definition shared both by
`FirebirdConnectionProperties` and `IConnectionProperties` (and siblings)),
though defining twice may be necessary (that is, for properties in JDBC URL and
`Properties`). Although possibly some form of reflection might allow for a
single definition.

The interface definition for these properties should - as much as possible -
provide a default implementation that delegates to a common setter. This will
remove the need for a lot of mapping code in various implementations.

Properties in the JDBC API (that is `DriverManager` and `DataSource`
implementations) should only support the primitive types `String`, `int` and
`boolean`. The `IConnectionProperties` and siblings may also support additional
types, as long as they also provide an alternative in the primitive type.

Preserving backwards compatibility is important (though more important for the
JDBC API, than for the GDS-ng API). 

## Decision

TODO: First we experiment

## Consequence

TODO: First we experiment
