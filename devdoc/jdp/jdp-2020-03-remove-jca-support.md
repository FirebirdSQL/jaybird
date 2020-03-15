# jdp-2020-03: Remove JCA support

## Status

- Draft
- Proposed for: Jaybird 5

## Type

- Feature-Specification

## Context

Since its inception, Jaybird has been built around the Java Connector
Architecture (JCA). As a result, Jaybird has a hard dependency on JCA for its
internals, as well as being able to use Jaybird as a JCA provider. Jaybird 4 and
earlier rely on the `connector-api-1.5` (JCA 1.5, which is several versions
behind).

The hard dependency on JCA causes deployment problems, either by users
forgetting to include the JCA dependency, or - for example - using the
`jaybird-full` library on JavaEE/JakartaEE application servers which reject
libraries including packages from the `javax.*` namespace.

Given lack of usage of JCA by users of Jaybird (and in general), it is unclear
whether Jaybird correctly implements JCA. It is also making code changes harder,
as it is not always clear if certain behaviour or structure is required or
necessary for JCA.  

With moving the stewardship of Java EE to Eclipse and the JakartaEE project,
next versions of JCA will change their package naming, which could make it
harder to use Jaybird correctly.

## Decision

Jaybird 5 will remove JCA support, and remove `connector-api-1.5` from its
dependencies.

To make a clean break, the current `org.firebirdsql.jca` package will be renamed
to `org.firebirdsql.jaybird.xca`, and the entire package will be classified as
an internal API of Jaybird.

Code changes will be made to remove the dependency on JCA, either by introducing
similar interfaces, exceptions, etc, or by refactoring to eliminate some parts
of JCA where this makes sense (eg replace the use of `ResourceException` with
for example `SQLException`).

### Rejected alternatives

We have considered making JCA an optional dependency of Jaybird by making the
current JCA implementation 'JCA-free' and moving it to a different package (this
is similar to the current decision), and then providing a new JCA implementation
in its place that utilizes this internal 'JCA-free' implementation.

Given the lack of response on the announcement to remove JCA support, and the
overall lack of bugs or questions reported against the JCA provider itself, we
assume that Jaybird is not (or hardly ever) used as a JCA provider. Investing
time to provide a JCA implementation would then seem to be a poor use of
our limited development time.

This is a decision that can be revisited in the future if there appears to be a
demand after all.

## Consequences

Jaybird 5 and higher will not offer JCA support. Users of Jaybird using JCA will
either need to stay on Jaybird 4, or switch to libraries that wrap JCA support
around a JDBC driver.

Jaybird 5 will no longer depend on `connector-api-1.5.jar`, and the
`jaybird-full` artifact will be dropped from the zip-distribution.

Third-party code relying directly on `org.firebirdsql.jca` will break.

This change will introduce some backwards compatibility issues in the
`org.firebirdsql.jdbc` package, as some of the classes and interfaces return.
accept or throw classes from `org.firebirdsql.jca` or `javax.resource`,
however as these are generally intended as internal API, this backwards
incompatibility will be limited.

Removing JCA support will make it simpler to evolve code in Jaybird, and
hopefully allow us to reduce complexity.
