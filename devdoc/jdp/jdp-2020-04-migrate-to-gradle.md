# jdp-2020-04: Migrate to Gradle

## Status

- Published: 2020-03-29
- Implemented in: Jaybird 5

## Type

- Project-Specification

## Context

Since its inception, the Jaybird project has used Ant as its build tool. At the
time, Ant was the only viable option in the Java ecosystem, but these days it
has been superseded by Maven and Gradle.

The current build has the following deficiencies:

- Dependencies are stored inside the repository (in `lib/` and `src/lib`)
- Publishing the Maven Central is a manual process
- Non-standard layout of files

## Decision

Jaybird will switch to Gradle as its build tool starting with Jaybird 5. It
offers a lot of the flexibility that Ant offers, but also supports easy
dependency management, and publishing to Maven.

For now the non-standard layout will be preserved until we find a good way to
get rid of it, or at least bring it more in-line with the standard layout.

### Rejected alternatives

The following alternatives were considered but rejected.

- Use Ant+Ivy. Rejected given our lack of familiarity with Ivy.
- Use Maven. Some of the non-standard aspects of our build are hard to do with
Maven (or at least, we have never done anything similar with Maven)

## Consequences

Jaybird 5 and higher will be built with Gradle. Jaybird 3 and 4 will continue to
use Ant for its build.

All old build related files (including dependencies and ant infrastructure) will
be removed, with the exception of `build-cts.xml` (to be kept as a reference in
case we want to revive this). 
 