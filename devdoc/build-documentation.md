<!--
SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
SPDX-License-Identifier: LicenseRef-PDL-1.0
-->
# Jaybird build documentation

NOTE: This document doesn't all aspects of the build. If you miss something,
please contribute using a pull request, or ask on [firebird-java](https://groups.google.com/g/firebird-java).

## Running the build

The build uses Gradle wrapper, which is included in the repository. The current
minimum version is Java 17.

To run with a specific Java version, we suggest creating a Java-specific launch
script. For example, for Windows create a batch file with:

```
@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-19
call gradlew.bat %*
```

### Default build

By default, the `clean` and `build` task is executed with the `PURE_JAVA` GDS
type. This target compiles everything, and runs the pure-Java tests. 

### Clean

To clean the current build output, run

```
./gradlew clean
```

Don't forget to execute this when switching between Java versions

### Documentation

To build documentation (FAQ and release notes), use the `asciidoctor` task:

```
./gradlew asciidoctor
```

### Publishing to Maven

See [Publishing](publish.md)

### Other targets

For a list of all targets, use

```
./gradlew tasks
```

## Build configuration

The build script defines a lot of properties in `gradle.properties`, 
and `build-properties.gradle`. To override those properties, you can either
specify them on the commandline (using `-P<prop>=<value>`), or in a file
`build-local.properties` in the root of the project.

Important properties to override are:

- `test.user` - the user to use for tests (defaults to `sysdba`); this user must
have privileges to create databases and users
- `test.password` - the password for user of previous property (defaults to 
`masterkey`)

Less important properties, but relevant for testing remotely:
 
- `test.db.host` - the hostname of Firebird (defaults to `localhost`)
- `test.db.port` - the port of Firebird (defaults to `3050`)
- `test.db.dir` - the path to use for databases (defaults to `${module.output}/db`)

Be aware that changing the `test.db.host` can result in test failures as some
tests perform verifications against the local filesystem. Some of those tests
can be disabled by using the `test.dbondocker` property set to true.

Properties for varying the type of connection tested:

- `test.gds_type` - the type to test (`PURE_JAVA`, `EMBEDDED`, `NATIVE`)
- `test.jna.library.path` - path containing the Firebird client or a suitable
Firebird embedded for the `NATIVE` and `EMBEDDED` tests

Other test properties:

- `test.db.lc_ctype` - connection character set
- `test.use.firebird.autocommit` - run tests with the experimental Firebird
auto-commit instead of Jaybird-managed auto-commit. 

## Version information

Version information of the build is configured through `build.properties`. 

## License Notice

The contents of this Documentation are subject to the Public Documentation
License Version 1.0 (the “License”); you may only use this Documentation if you
comply with the terms of this License. A copy of the License is available at
<https://firebirdsql.org/en/public-documentation-license/>.

The Original Documentation is "Jaybird build documentation".
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
