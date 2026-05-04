# Jaybird build documentation

NOTE: This document doesn't all aspects of the build. If you miss something,
please contribute using a pull request, or ask on the firebird-java mailing
list.

These instructions apply to Branch_5_0 (Jaybird 5.0.x).

## Running the build

The build uses Gradle wrapper, which is included in the repository. The Gradle
wrapper version itself requires Java 17 minimum. However, the build uses
toolchains to compile and test on a specific Java version, which will also need
to be installed. The build script intentionally does not use solutions to
automatically download Java versions required by the selected toolchain(s).

The build target (`buildProfile`) defaults to Java 11, independent of the Java
version running Gradle.

The target Java version is determined by passing `-PbuildProfile=NN`, where `NN`
is the desired Java version. The default is `11`. Versions other than `8` or
`11` are not published to Maven.  

By default, tests are run with that `buildProfile` version. This can be
overridden with `-PtestProfile=NN`. For historic reasons, `javaNN` is also
accepted by both properties. Running tests with `buildProfile` set to Java 8 and
`testProfile` to Java 11 or higher may fail due to absence of ChaCha support. 

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

Be aware that changing `test.db.host` can result in test failures as some tests
perform verifications against the local filesystem.

Properties for varying the type of connection tested:

- `test.gds_type` - the type to test (`PURE_JAVA`, `EMBEDDED`, `NATIVE`, 
`OOREMOTE`)
- `test.jna.library.path` - path containing the Firebird client or a suitable
Firebird embedded for the `NATIVE` and `EMBEDDED` tests

Other test properties:

- `test.db.lc_ctype` - connection character set
- `test.use_firebird_autocommit` - run tests with the experimental Firebird
auto-commit instead of Jaybird-managed auto-commit. 
- `test.dbondocker` - test runs on Docker/GitHub, meaning some tests are skipped
due to checking local paths and absence of event port mapping

## Version information

Version information of the build is configured through `build.properties`. 
