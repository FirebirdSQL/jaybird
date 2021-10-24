# jdp-2021-03: Drop Firebird 2.5 support

## Status

- Draft
- Proposed for: Jaybird 6

## Type

- Feature-specification

## Context

With the release of Firebird 2.5.9 in June 2019, the Firebird 2.5 line has
become end-of-life.

## Decision

Jaybird 6 will drop formal support for Firebird 2.5.

## Consequences

Jaybird 6 will not be tested against Firebird 2.5, and may use features from
Firebird 3.0. Support for the Firebird 2.5 wire protocol (version 12) will not
be removed, so in general the driver will likely continue to work, but some
features, e.g. `DatabaseMetaData`, may fail if it uses syntax or features
introduced in Firebird 3.0.

In general, we will no longer fix bugs that only occur when Jaybird is used with
Firebird 2.5 or earlier.
