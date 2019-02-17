# jdp-2019-02: Version Number and Naming Scheme

## Status

- Draft
- Proposed for: Jaybird 4

## Type

- Project-Specification

## Context

Historically, Jaybird has used the `<major>.<minor>.<patch>` scheme, combined 
with the targeted Java version in various combinations.

Some examples:

- Download artifacts:
  - FirebirdSQL-1.5.6JDK_1.3.zip
  - JayBird-2.0.1JDK_1.3.zip
  - Jaybird-3.0.4-JDK_1.7.zip
- Jar files
  - firebirdsql.jar
  - jaybird-2.1.6.jar
  - jaybird-3.0.4.jar
  - jaybird-full-3.0.4.jar
  - jaybird-3.0.4-sources.jar
- Maven coordinates
  - org.firebirdsql.jdbc:jaybird-jdk18:3.0.4

With the advent of the new Java version numbering, the possibility of collision
is now imminent. For example when Java 15 is released, the existing `jdk15` 
artifacts will be ambiguous.

## Decision

### Version

Jaybird will continue to use the `<major>.<minor>.<patch>` scheme. The `<minor>`
part should always be `0`. If circumstances make it necessary to introduce a
minor version, this is still possible.

The project will not follow Semantic Versioning as described 
on https://semver.org/, but will try to avoid breaking changes in the same major 
version.

For pre-releases, the version can be suffixed with:

- `-SNAPSHOT` for development snapshots, eg 4.0.0-SNAPSHOT
  - On Maven, the snapshot can contain a timestamp (eg `-SNAPSHOT-<timestamp>`)
- `-alpha-n` for alpha releases, eg 4.0.0-alpha-1
- `-beta-n` for beta releases, eg 4.0.0-beta-1
- `-rc-n` for release candidates, eg 4.0.0-rc-1

### Java target

Moving forward, the current inconsistent identification of Java version (eg 
`JDK_1.8`, `jdk18`, etc) must be `javaXX`, where `XX` is the Java (major) 
version.

This change will only be applied for newly supported Java versions (that is 
beyond Java 8).

For Java 8 or earlier, the Java identification must be `jdkXX`. This avoids
introducing a breaking change in the Maven coordinates.

### Naming scheme

When referencing a Jaybird version in text, use `Jaybird <major>`. For legacy
versions 2.2 and earlier, use `Jaybird <major>.<minor>`. If the specific patch
version is relevant, use `Jaybird <major>.<minor>.<patch>`.

In the various artifacts, the name Jaybird must be used lowercase (`jaybird`). 
The target Java version must be included in the jar filename.

The naming scheme must be:

    jaybird-<java-target>-<version>
    
For additional jar files, the naming scheme depends on the function of the jar.
If the jar file is an extension (eg full, or - historically - pool), then the 
scheme is:

    jaybird-<name>-<java-target>-<version>

For Maven this would be the equivalent of introducing a new dependency with the 
name `jaybird-<name>-<java-target>`.

If the jar file is a different aspect of the jar (eg sources or javadoc), then
the scheme is:

    jaybird-<java-target>-<version>-<aspect>

## Consequences

In written text, the shortest form of a version will be used, for example 
_Jaybird 4_ or _Jaybird 2.2_. Only when the patch is relevant will we use the 
full version: _Jaybird 3.0.5_.

The version number and naming scheme will improve the consistency of the naming 
of various artifacts.

Including the target Java version in the jar filename will reduce ambiguity of 
the targeted Java version, but will prevent drop-in replacement of the jar files; 
that is swapping the `jaybird-3.0.4.jar` of Java 8 with one from Java 7 or vice 
versa without having to update the classpath.

Under this scheme, the artifacts will be named as:

- Download artifacts:
  - jaybird-jdk18-4.0.0.zip
  - jaybird-java11-4.0.0.zip
- Jar files:
  - jaybird-jdk18-4.0.0.jar
  - jaybird-java11-4.0.0.jar
  - jaybird-full-jdk18-4.0.0.jar
  - jaybird-jdk18-4.0.0-sources.jar
- Maven coordinates
  - org.firebirdsql.jdbc:jaybird-jdk18:4.0.0
  - org.firebirdsql.jdbc:jaybird-java11:4.0.0 (tentative)
  - org.firebirdsql.jdbc:jaybird-full-jdk18:4.0.0 (example only)
 