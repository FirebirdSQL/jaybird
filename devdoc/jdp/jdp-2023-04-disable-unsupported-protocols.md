# jdp-2023-04: Disable unsupported protocols

## Status

- Published: 2023-02-25
- Implemented in: Jaybird 6

## Type

- Feature-Specification

## Context

Jaybird 6 supports Firebird 3.0 or higher. Support for earlier Firebird versions
was dropped (see [jdp-2021-03](https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2021-03-drop-firebird-2-5-support.md)).

At the moment of writing, Jaybird still allows connecting to Firebird 1.0 or
higher (probably even InterBase 6.0) because Jaybird has a protocol 10
implementation. This means that even though not supported, connecting to
unsupported versions is still possible.

Protocol version to (minimum) Firebird versions:

| Protocol | Firebird |
|----------|----------|
| 10       | 1.0      |
| 11       | 2.1      |
| 12       | 2.5      |
| 13       | 3.0      |
| 14 (*)   | 3.0.1    |
| 15       | 3.0.2    |
| 16       | 4.0      |
| 17 (*)   | 4.0.1    |
| 18       | 5.0      |

<sub>(*): Protocol version not implemented in Jaybird</sub>

## Decision

Given Jaybird 6 does not support Firebird 2.5 and earlier, we will disable
the older protocol versions (12 and lower) _by default_. Additionally, _"future" 
protocol versions (greater than 18) will also be disabled. This only applies to
the pure Java implementation, as the native client handles protocol support 
internally.

A new connection property will be introduced to _enable_ protocol versions, if
they are still available. The property will be named `enableProtocol` and
accepts a comma-separated list of protocol versions to enable, or `*` to enable
all available versions. The connection property will only enable protocols, so
supported protocol versions (e.g. for Jaybird 6, versions 13, 15, 16, 18) cannot
be disabled this way.

When an unknown protocol version is listed in the property, this is silently 
ignored.

Future JDPs may propose the removal of unsupported protocol versions.

An alternative workaround for connecting with unsupported protocols is to use 
a native connection (e.g `jdbc:firebirdsql:native:...`).

To simplify running tests against unsupported versions, the Jaybird tests will 
enable all available protocols (`enableProtocol=*`) by default. The system 
property `test.enableProtocol`, or from Gradle `-P'test.enableProtocol=<value>` 
can be used to configure alternative behaviour.
