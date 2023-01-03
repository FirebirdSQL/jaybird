# jdp-2023-01: Version Number and Naming Scheme

## Status

- Published: 2022-01-03
- Implemented in: Jaybird 6
- Replaces: jdp-2019-04

## Type

- Project-Specification

## Context

In jdp-2019-04, a naming and version scheme was introduced which includes 
the target Java version in the version number.

With jdp-2022-03, the decision was made that from Jaybird 6, Jaybird will only 
support Java 17 or higher. The expectation is that only a Java 17 build will be
sufficient to cover all supported Java versions.

## Decision

### Version

Jaybird will switch to using a `<major>.<minor>.<patch>` scheme. The `<minor>` 
part should always be `0`. If circumstances make it necessary to introduce 
a minor version, this is still possible.

The project will not follow Semantic Versioning as described
on https://semver.org/, but will try to avoid breaking changes in the same major
version.

For pre-releases, the version can be suffixed with:

- `-SNAPSHOT` for development snapshots, e.g. 6.0.0-SNAPSHOT
    - On Maven, the snapshot can contain a timestamp (e.g. `-SNAPSHOT-<timestamp>`)
- `-alpha-n` for alpha releases, e.g. 6.0.0-alpha-1
- `-beta-n` for beta releases, e.g. 6.0.0-beta-1
- `-rc-n` for release candidates, e.g. 6.0.0-rc-1

### Java target

Moving forward, the Java target is no longer reported in the version or 
artifactId.

### Naming scheme

When referencing a Jaybird version in text, use `Jaybird <major>`. For legacy
versions 2.2 and earlier, use `Jaybird <major>.<minor>`. If the specific patch
version is relevant, use `Jaybird <major>.<minor>.<patch>`. If the pre-release
status is relevant, but not the Java version, use
`Jaybird <major>.<minor>.<patch>-<pre-release>`.

In the various artifacts, the name Jaybird must be used lowercase (`jaybird`).

The naming scheme must be:

    jaybird-<version>

For additional jar files, the naming scheme depends on the function of the jar.
If the jar file is an extension (e.g. full, or - historically - pool), then the
scheme is:

    jaybird-<name>-<version>

For Maven this would be the equivalent of introducing a new dependency with the
name `jaybird-<name>`.

If the jar file is a different aspect of the jar (e.g. sources or javadoc), then
the scheme is:

    jaybird-<version>-<aspect>

## Consequences

In written text, the shortest form of a version will be used, for example
_Jaybird 4_ or _Jaybird 2.2_. Only when the patch is relevant will we use the
full version: _Jaybird 3.0.5_. Only when the pre-release status is relevant, but
not the Java target, use : _Jaybird 4.0.0-beta-2_.

The version number and naming scheme will improve the consistency of the naming
of various artifacts.

Removal of the target Java version simplifies version numbering. We accept
the risk of having to reintroduce a Java version if future Java versions cannot
be covered with a single build.

Under this scheme, the artifacts will be named as:

- Download artifacts:
    - jaybird-6.0.0.zip
- Jar files:
    - jaybird-6.0.0.jar
    - jaybird-6.0.0-sources.jar
- Maven coordinates
    - org.firebirdsql.jdbc:jaybird:6.0.0
    - org.firebirdsql.jdbc:jaybird-full:6.0.0 (example only)
