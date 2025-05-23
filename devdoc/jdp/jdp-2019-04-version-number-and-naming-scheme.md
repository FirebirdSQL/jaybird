<!--
SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# jdp-2019-04: Version Number and Naming Scheme

## Status

- Published: 2020-01-19
- Implemented in: Jaybird 4
- Replaces: jdp-2019-02
- Replaced by: jdp-2023-01

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

In jdp-2019-02 a new version scheme was proposed which continued to apply the
naming scheme `jaybird-<java-target>-<version>` (for example 
`jaybird-java11-4.0.0` or `jaybird-jdk18-4.0.0`).

It has now become apparent that this schema has downsides with regard to
discoverability. For example, when searching jaybird-jdk15 on https://search.maven.org/,
or using `maven-versions-plugin` it might not be apparent that Jaybird versions
exists beyond 2.2.7. In similar vein, maintaining information with Tidelift is
harder for multiple artifacts. 

## Decision

### Version

Jaybird will switch to using a `<major>.<minor>.<patch>.<java-target>` scheme.
The `<minor>` part should always be `0`. If circumstances make it necessary to
introduce a minor version, this is still possible.

The project will not follow Semantic Versioning as described 
on https://semver.org/, but will try to avoid breaking changes in the same major 
version.

For pre-releases, the version can be suffixed with:

- `-SNAPSHOT` for development snapshots, eg 4.0.0.java11-SNAPSHOT
  - On Maven, the snapshot can contain a timestamp (eg `-SNAPSHOT-<timestamp>`)
- `-alpha-n` for alpha releases, eg 4.0.0.java11-alpha-1
- `-beta-n` for beta releases, eg 4.0.0.java11-beta-1
- `-rc-n` for release candidates, eg 4.0.0.java11-rc-1

### Java target

Moving forward, the current inconsistent identification of Java version (eg 
`JDK_1.8`, `jdk18`, etc) must be `javaXX`, where `XX` is the Java (major) 
version.

The target will be part of the version.

This change will be applied for all supported Java versions.

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

Including the target Java version in the version will reduce ambiguity of 
the targeted Java version, but will prevent drop-in replacement of the jar files; 
that is swapping the `jaybird-4.0.0.java8.jar` of Java 8 with one from Java 7 or vice 
versa without having to update the classpath.

Under this scheme, the artifacts will be named as:

- Download artifacts:
  - jaybird-4.0.0.java8.zip
  - jaybird-4.0.0.java11.zip
- Jar files:
  - jaybird-4.0.0.java8.jar
  - jaybird-4.0.0.java11.jar
  - jaybird-full-4.0.0.java8.jar
  - jaybird-4.0.0.java8-sources.jar
- Maven coordinates
  - org.firebirdsql.jdbc:jaybird:4.0.0.java8
  - org.firebirdsql.jdbc:jaybird:4.0.0.java11 (tentative)
  - org.firebirdsql.jdbc:jaybird-full:4.0.0.java8 (example only)

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "jdp-2019-04: Version Number and Naming Scheme".
The Initial Writer of the Original Documentation is Mark Rotteveel,
Copyright © 2019-2023. All Rights Reserved. (Initial Writer contact(s):
mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>
 