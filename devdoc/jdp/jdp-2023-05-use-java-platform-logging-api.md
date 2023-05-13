# jdp-2023-04: Use Java Platform Logging API

## Status

- Draft
- Proposed for: Jaybird 6

## Type

- Feature-Specification

## Context

Jaybird currently has a custom logging facade in the package 
`org.firebirdsql.logging`. Since Jaybird 3.0, it defaults to an implementation
using `java.util.logging` (`j.u.l` for short), but also has a "console logger"
to log directly to `System.out` and a "null logger" to disable logging, and the
ability for applications/users to provide their own implementation. The exact
behaviour and implementation used is controlled through a number of Jaybird
system properties, and the configuration of the underlying logging library (e.g.
`java.util.logging`).

Since Java 9, the `java.util.logging` package is part of the module 
`java.logging`. In the `java.base` module, Java provides a basic logging facade
through `System.getLogger(String)` (e.a.), and the `System.Logger` interface and
`System.LoggerFinder` class, also called the Java Platform Logging API ([JEP-264](https://openjdk.org/jeps/264)).
By default, it uses `j.u.l`, if the `java.logging` module is on the module path,
or otherwise directly to `System.err`, but custom implementations can be used.
For example, Log4j provides a dependency `org.apache.logging.log4j:log4j-jpl`
which implements `System.Logger` using Log4j (and if on the classpath, it will
be used instead of the defaults).

Informal polling on [firebird-java](https://groups.google.com/g/firebird-java)
if people used a custom implementation of `org.firebirdsql.logging.Logger` did
not yield any response.

Jaybird uses a number of custom logging methods (e.g. for logging some things on
_error/warn/info_, with more details on _debug_ or _trace_), which are not
provided out of the box by `System.Logger`.

## Decision

Jaybird will switch from its custom logging facade to `System.Logger`.

Reasons:

1. Plugging in a different logging library becomes easier.

   There is no more need for a Jaybird specific logger implementation and
   configuration, and logging libraries will either already have their own
   implementation (see example of `log4j-jpl` above), or it is easier to find
   an existing third-party library for this, or if you have to write your own,
   it will be applied for all other things using `System.Logger`.

2. `System.Logger` supports "real" parameterized logging, which can reduce the
   overhead of logging.

   Though Jaybird currently does make some effort to not render messages if they
   will not get logged, some logging libraries provide optimizations to reduce
   or prevent extra allocations even when message do get logged.

3. It removes custom code from Jaybird 

NOTE: The change will not remove a dependency on module `java.logging`, as the
JDBC API itself declares a number of methods accepting or returning a `Logger`,
and as a result module `java.sql` and Jaybird itself will retain a dependency on
module `java.logging`.

When replacing the loggers, the opportunity will be taken to review existing
logging, and remove unnecessary or not (very) useful logging, or improve
logging where relevant. The primary concern here is reviewing existing logging,
not introducing logging where it is currently missing. The custom logging
applied (e.g. logging multi-level) will either be removed or simplified, or some
static helpers will be created as a replacement. This type of decision will be
made on the fly as part of the realization, and is not subject to a further JDP.

## Consequences

As `System.Logger` does not have an equivalent of _fatal_, both _error_ and
_fatal_ will be replaced with level `ERROR` (this is similar to what happens in
`JulLogger` of previous versions, which maps _error_ and _fatal_ to `j.u.l`
level `SEVERE`).

The package `org.firebirdsql.logging` will be removed in its entirety, and 
custom implementations of `org.firebirdsql.logging.Logger` will no longer work.

The following system properties will no longer be used by Jaybird.

- `org.firebirdsql.jdbc.forceConsoleLogger`

  Users that want to log to the console will need to configure `j.u.l` (note:
  by default it logs to `System.err`) or other logging library to log to the
  console for the `org.firebirdsql.*` loggers, or provide a custom
  `System.Logger` and `System.LoggerFinder` to do this.

- `org.firebirdsql.jdbc.disableLogging`

  Users that want to disable logging will need to configure `j.u.l` or other
  logging library to disable logging entirely, or for loggers in the
  `org.firebirdsql.*` namespace, or provide a custom `System.Logger` and
  `System.LoggerFinder` which does not log (though this is not advisable, as
  this would also affect others using these loggers!).

- `org.firebirdsql.jdbc.loggerImplementation`

  No direct replacement. Its spiritual successor is using a "standard" (e.g.
  `log4j-jpl`) or custom implementation of `System.Logger` and
  `System.LoggerFinder` with a service loader definition.

The Jaybird manual will need to be revised in this regard.
