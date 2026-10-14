<!--
SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# Jaybird
Jaybird is a JDBC driver suite to connect to Firebird database servers.

[![Build Status](https://github.com/FirebirdSQL/jaybird/actions/workflows/run-tests.yml/badge.svg?branch=master)](https://github.com/FirebirdSQL/jaybird/actions?query=branch%3Amaster+workflow%3Arun-tests)
[![MavenCentral](https://maven-badges.sml.io/sonatype-central/org.firebirdsql.jdbc/jaybird/badge.svg)](https://maven-badges.sml.io/sonatype-central/org.firebirdsql.jdbc/jaybird/)

## Getting Jaybird

A distribution zip is available from [releases](https://github.com/FirebirdSQL/jaybird/releases) and the Firebird website: <https://firebirdsql.org/en/jdbc-driver/>.

Jaybird is also available from [Maven Central](https://central.sonatype.com/artifact/org.firebirdsql.jdbc/jaybird):

For example:

```xml
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird</artifactId>
    <version>6.0.7</version>
</dependency>
```

If you want to use native or embedded (JDBC Type 2), add `jaybird-native` as a dependency:

```xml
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>jaybird-native</artifactId>
    <version>6.0.7</version>
</dependency>
```

For Windows, Linux, and macOS, you can add the [`org.firebirdsql.jdbc:fbclient`](https://github.com/mrotteveel/jaybird-fbclient) dependency on your classpath to provide the native libraries for the `native` protocol.
This dependency does *not* support Firebird embedded.

To enable "`ChaCha64`" wire encryption support for pure Java connections, add `chacha64-plugin` as a dependency:

```xml
<dependency>
    <groupId>org.firebirdsql.jdbc</groupId>
    <artifactId>chacha64-plugin</artifactId>
    <version>6.0.7</version>
</dependency>
```

## Reporting bugs or improvements

For security vulnerabilities, see [Security Policy](https://github.com/FirebirdSQL/jaybird/security/policy).

For bugs or improvement, go to our [Issue tracker](https://github.com/FirebirdSQL/jaybird/issues/).

## Resources

- [Downloads](https://www.firebirdsql.org/en/jdbc-driver/)
- [Jaybird JDBC Driver Java Programmer’s Manual](https://firebirdsql.github.io/jaybird-manual/jaybird_manual.html)
- [Release notes](src/docs/asciidoc/release_notes.adoc)
- [Issue tracker](https://github.com/FirebirdSQL/jaybird/issues/)
- [Wiki](https://github.com/FirebirdSQL/jaybird/wiki)
- [FAQ](src/docs/asciidoc/faq.adoc)
- [How to contribute](CONTRIBUTING.md)

## License

Jaybird is licensed under LGPL 2.1 or later, with extension interfaces *also*
licensed under the BSD 3-clause. See source headers for the specific license of
a file (if SPDX information is absent, consult [`REUSE.toml`](REUSE.toml)).

<!-- REUSE-IgnoreStart -->

`SPDX-License-Identifier: LGPL-2.1-or-later OR LGPL-2.1-or-later AND BSD-3-Clause`

<!-- Reuse-IgnoreEnd -->

## Support

Looking for professional support of Jaybird? Jaybird is now part of the [Tidelift subscription](https://tidelift.com/subscription/pkg/maven-org-firebirdsql-jdbc-jaybird?utm_source=maven-org-firebirdsql-jdbc-jaybird&utm_medium=referral&utm_campaign=readme).

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "Jaybird" (README.md). The Initial Writer of 
the Original Documentation is Mark Rotteveel, Copyright © 2015-2024. All Rights
Reserved. (Initial Writer contact(s): mark (at) lawinegevaar (dot) nl).

<!--
Contributor(s): ______________________________________.
Portions created by ______ are Copyright © _________ [Insert year(s)]. All Rights Reserved.
(Contributor contact(s): ________________ [Insert hyperlink/alias]).
-->

The exact file history is recorded in our Git repository; see
<https://github.com/FirebirdSQL/jaybird>