# jdp-2020-10: Propagation of connection properties

## Status

- Published: 2021-09-25
- Implemented in: Jaybird 5

## Type

- Feature-Specification

## Context

The API of Jaybird provides connection properties at several levels:

1. For `DriverManager` with connection properties in the JDBC url and 
   `Properties` object (defined through `isc_dpb_types.properties`, 
   `driver_property_info.properties` and values in `ISCConstants` prefixed with
   `isc_dpb_`)
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
   
Ideally, properties should be specified once (e.g. in a definition shared both 
by `FirebirdConnectionProperties` and `IConnectionProperties` (and siblings)),
though defining twice may be necessary (that is, also for properties in JDBC 
URL/`Properties`). Although possibly some form of reflection might allow for a
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

### New interfaces for properties

We introduce a number of new interfaces that specify the connection properties.
These interfaces will provide a default implementation of each setter,
delegating to a common setter. This allows a single definition of the
properties, where handling setting and getting properties is implemented using
generic configuration setters and getters.

The new interfaces are:

-   `org.firebirdsql.jaybird.props.BaseProperties` - generic configuration 
    setters and getters
-   `org.firebirdsql.jaybird.props.AttachmentProperties` - common configuration
    shared by both database and service attachments, extends `BaseProperties`
-   `org.firebirdsql.jaybird.props.DatabaseConnectionProperties` - configuration
    for database connections extends `AttachmentProperties`
-   `org.firebirdsql.jaybird.props.ServiceConnectionProperties` - configuration
    for service connections, extends `AttachmentProperties`

These new interfaces will be extended by the existing interfaces:
`FirebirdConnectionProperties` and `IConnectionProperties` will extend 
`DatabaseConnectionProperties`, while `ServiceManager` and `IServiceProperties`
will extend `ServiceConnectionProperties`.

The generic configuration setters and getters will support setting `String`,
`Integer` and `Boolean`, which allows for removing the value of a primitive.
The specific property setters and getters will - in general - use `String`,
`int` and `boolean`, applying either a default value, or a sensible 'not set'
sentinel value.

In general, properties on the existing interfaces will be moved to the new
interfaces, except properties that are highly specific for that interface (as an
example `FirebirdConnectionProperties.setType`). In some cases, existing aliases
or similar properties will be retained on the old interface, delegating to the
new interface. On a property-by-property basis, we will decide on deprecation
and - optional - removal.

It may be necessary to change the types of some properties. We accept these 
incompatible type changes as a necessity to move forward.

Although some properties are only relevant for JDBC connections, and not for
GDS-ng connections, we will move those to `DatabaseConnectionProperties` as well.
Having a single place to define properties simplifies the API and removes the
need to decide where a property 'really belongs'.

### Defining string connection properties and mapping to DPB/SPB

The string connection properties (for `DriverManager`) will be defined using
code using the normal `ConnectionProperty` builder and all its available
properties.

Doing this in code gives more flexibility compared to more declarative options
like using annotations or configuration files.

### Allowing extra properties to be defined by plugins

Plugins may need additional connection properties, for example a third party
encryption plugin may need extra configuration properties. To support this, a
Service Provider Interface (SPI), `org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi`
is added.

The SPI can contribute connection properties, as long as they don't try to map
names or aliases, op DPB/SPB items that are already defined. Jaybird will report
back if a property was not registered. This callback can be used by the
plugin to detect issues with duplicate properties and - for example - disable
itself.

This allows plugins to explicitly configure the type and things like default
values. It can also be helpful for introspection tools to report on all
available properties.

### Handling of 'undefined' DPB/SPB items

Firebird provides a lot of DPB and SPB items, where Jaybird only supports a
subset of these properties. To provide access to DPB and SPB items not
explicitly defined in Jaybird or by a plugin, Jaybird will scan `DpbItems` and 
`SpbItems` for items prefixed with `isc_dpb_` and `isc_spb_` to define those as
string properties with as name the constant name without prefix, and as 
alias(es) the constant names with prefix.

This should be considered a last-ditch effort; preferably all properties should
be defined explicitly (though this does not make sense for all properties).

### Handling of 'unknown' properties

Use of undefined property names will be allowed: they will be added to the
configuration and can be retrieved by name, their type is string.

### Deprecation of some properties

The following properties are deprecated and will be removed in Jaybird 6:

- `timestampUsesLocalTimezone`: inverts time zone offset when using
  `getTime`/`getTimestamp` with a `Calendar`. The semantics of this property are
  unclear, and might be better addressed by doing appropriate conversions in
  user code with use of `LocalDateTime` and `LocalDate`.

### Rejected design decisions

1. Keep JDBC connection properties only on `FirebirdConnectionProperties` (or on
   a new interface, eg `JdbcProperties`).
    
   Pro: does not pollute the API for GDS-ng with properties not used by GDS-ng.

   Con: higher complexity of API, does not allow for potential future removal
   of intermediate interfaces, and in some areas, access to
   an implementation of `IConnectionProperties` might be available, but not 
   `FirebirdConnectionProperties`.
    
2. Defining the string connection properties using annotations on the
   interfaces (`AttachmentProperties`, etc.).
    
   Although this looked promising initially as it kept definitions in a single
   place, it added an extra layer of complexity (processing the annotations).
   It also made some ideas for validation/transformation of properties more 
   complex to realise due to limitations of values available to annotations.
   However, right now those ideas do get used, so maybe this needs to be 
   evaluated again at a later time.

3. Including defaults in the connection property definition.

   We explored this option, but decided that having the defaults in the
   interface, and explicitly populated in `FbConnectionProperties` for 
   properties where the interface route wasn't sufficient, resulted in lower
   overhead (in terms of either having to pre-populate connection properties
   with defaults, or providing a mechanism to obtain the default).

   The downside is that extensions that contribute additional properties will
   need to track their own defaults, and cannot contribute non-standard DPB/SPB
   items with a default value.
    
### Open questions

The following questions came up while implementing, but are not addressed:

- How to handle transaction configuration?

  Handled different for JDBC url compared to `FirebirdConnectionProperties`.
  Might need to remain 'as-is' for now.
    
- Is it possible to eliminate `FirebirdConnectionProperties` entirely? Do we
  want to take that incompatibility without deprecation?

  Removal hindered by presence of `database` property. This may first require 
  unification of database coordinates in some form. See [jdp-2021-01](jdp-2021-01-unified-database-coordinates.md).

- Is it possible to eliminate `FBConnectionProperties` entirely? See also
  previous point.

- Is it possible to eliminate `IAttachProperties`, `IConnectionProperties` and
  `IServiceProperties` interfaces?

  Removal hindered by presence of `serverName`, `portNumber` and 
  `databaseName`/`serviceName` properties, and methods for mutable/immutable
  copying. This may first require unification of database coordinates in some 
  form, see [jdp-2021-01](jdp-2021-01-unified-database-coordinates.md).

## Consequences

The hierarchies of connection property interfaces have been unified as much as
possible. Some properties in interfaces have been deprecated (e.g.
`buffersNumber`), in favour of a better name in the common interface, see
release notes.

Where possible, implementations of the interface have removed their
implementations of the getter/setter pairs, instead using the default
implementation provided by the interfaces. In some cases (data sources and
management API), we have deviated from that, and had to implement the default
methods, delegating to the interface default method, to ensure that the
getter/setter pairs can be introspected as JavaBeans.

There is now a service loader mechanism, 
`org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi` for
applications or plugins to contribute connection properties. This includes
contributing Firebird DPB/SPB items that Jaybird doesn't know about.

Constants for connection property names in `FBConnectionProperties` have been
deprecated for removal (replacement is 
`org.firebirdsql.jaybird.props.PropertyNames`).

Various mutable implementations of connection properties have been reimplemented
to delegate to an instance of `FbConnectionProperties`.

Setting `encoding` will no longer populate `charSet`, or vice versa. This
applies to the various data sources and `FBManagedConnectionFactory`. The
interaction between `encoding` and `charSet` is now only evaluated when
connecting and is not reflected in the connection properties.

Resources `driver_property_info.properties` and `isc_dpb_types.properties` no
longer exist.

Information returned by `FBDriver.getPropertyInfo(String, Properties)` will no
longer have description populated (we consider this of low value, so we got rid
of this duplication of information).

Jaybird-specific connection properties no longer have an `isc_dpb_*` constant in
`ISCConstants`. For purposes of code organization, Firebird-specific constants 
in `ISCConstants` that are prefixed with `isc_dpb_*`, `isc_spb_*` that are 
connection properties, and `isc_tpb_*`, have been deprecated for removal, with
their new home in `org.firebirdsql.jaybird.fb.constants.DpbItems`, `SpbItems`
and `TpbItems`.

Various repeated definitions of the DPB, SPB and TPB items have been deprecated,
for example in `DatabaseParameterBuffer`, `ServiceParameterBuffer`,  
`TransactionParameterBuffer` and `FirebirdConnection`.

The interface `DatabaseParameterBufferExtension` has been removed.
