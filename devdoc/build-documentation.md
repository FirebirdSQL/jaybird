# Jaybird build documentation

NOTE: This document doesn't all aspects of the build. If you miss something,
please contribute using a pull request, or ask on the firebird-java mailing
list.

## Running the build

The build uses Apache Ant, which is included in the repository. The Java version
running the build determines the build configuration (for example Java 7 selects
a different set of source than Java 8).

To run with a specific Java version, we suggest creating a Java-specific launch
script. For example, for windows create a batch file with:

```
@echo off
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0
call build.bat %*
```

### Default build

By default, the `tests-report-html-pure-java` target is executed. This target
compiles everything (including documentation), and runs the pure-Java tests. 

### Clean

To clean the current build output, run

```
build clean
```

Don't forget to execute this when switching between Java versions

### Other targets

For a list of all targets, use

```
build -p -verbose
```

## Build configuration

The build script defines a lot of properties in `build/init.xml`. To override
those properties, you can either specify them on the commandline (using 
`-D<prop>=<value>`), or in a file `build-local.properties` in the root of the
project.

Important properties to override are:

- `pandoc.app` - the path to the pandoc executable used for the FAQ and release
notes (defaults to `/home/${user.name}/.cabal/bin/pandoc`)
- `test.user` - the user to use for tests (defaults to `sysdba`); this user must
have privileges to create databases and users
- `test.password` - the password for user of previous property (defaults to 
`masterkey`)

Less important properties, but relevant for testing remotely:
 
- `test.db.host` - the hostname of Firebird (defaults to `localhost`)
- `test.db.port` - the port of Firebird (defaults to `3050`)
- `test.db.dir` - the path to use for databases (defaults to `${module.output}/db`)

Be aware that change the `test.db.host` can result in test failures as some
tests perform verifications against the local filesystem.

## Version information

Version information of the build is configured through `build.properties`. 
