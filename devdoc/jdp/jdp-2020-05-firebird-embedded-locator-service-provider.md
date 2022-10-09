# jdp-2020-05: Firebird Embedded locator service provider

## Status

- Published: 2022-10-09
- Implemented in: Jaybird 5

## Type

- Experimental
- Feature-specification

## Context

Firebird can be run as an embedded database server. Jaybird supports using
Firebird Embedded. However, especially since Firebird 3, using Firebird Embedded
is really cumbersome due to the number of libraries and other files you need to
deploy together with the application. As a consequence it is hard to use
Firebird Embedded with Jaybird.

## Decision

Jaybird will introduce a service provider interface,
`org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider` that will enable 
Jaybird to locate and use Firebird Embedded library files.

Jaybird will use the embedded library of the first provider that was located on
the classpath for the current platform that also installed successfully. Future
versions of Jaybird may introduce more advanced features like selecting a
library based on version requirements.

### Open questions

1. Will this work for using Firebird Embedded on Linux and macOS?

## Consequences

Jaybird provides a service provider interface 
`org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`. Implementations of
this interface will be discovered through `java.util.ServiceLoader`. When
creating an embedded connection, Jaybird will attempt to locate a suitable
Firebird Embedded instance using this service loader. It will then try to load
this library before any other libraries on the search path.

Jaybird will also provide classes to support packaging Firebird Embedded in a
JAR, and load Firebird from the classpath to a temporary folder. This will
provide the basic shared infrastructure for libraries to provide Firebird
Embedded from the classpath.

### Requirements for the provider

A provider must implement `org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`.

This interface defines three methods:

- `getPlatform()` to identify the target platform of the library. \
This must use the conventions of [JNA](https://github.com/java-native-access/jna).
The returned value must match the value of `com.sun.jna.Platform.RESOURCE_PREFIX`
of the targeted platform. For example for Windows 64-bit (x86), the platform is
`win32-x86-64`, for Linux 64-bit (x86), it is `linux-x86-64`.
- `getVersion()` to identify the version of Firebird embedded. \
The version should have the format as reported by `isc_info_firebird_version`
and as expected by `GDSServerVersion`. That is, its format should be 
`<platform>-<type><majorVersion>.<minorVersion>.<variant>.<buildNum>[-<revision>] <serverName>`, 
where platform is a two-character platform identification string, Windows for
example is "WI", type is one of the three characters: "V" - production version,
"T" - beta version, "X" - development version. \
This is not a hard requirement, but failure to comply may exclude the
implementation from being used in features like selecting a suitable Firebird
Embedded version based on version requirements (such a feature does not exist
yet).
- `getFirebirdEmbeddedLibrary()` to return information on the location of the
Firebird Embedded library provided (or found) by this provider. \
If the provider has to perform initialization before the embedded library is
usable (eg copy resources from the classpath to a temporary location), this must
be done in this method. \
Implementations must be able to handle multiple calls to this method. It is
allowed to return the same library instance on subsequent invocations.

### Requirements for FirebirdEmbeddedLibrary

Implementations of `org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary`
point to a Firebird Embedded instance.

The interface defines two methods:

- `getEntryPointPath()` to point to the library entry point on the filesystem. \
This should point to a Firebird client library that provides Firebird Embedded.
The library and supporting files should already exist on the filesystem.
- `getVersion()` to identify the version of Firebird embedded. \
This should provide the same information as `FirebirdEmbeddedProvider.getVersion()`.

If an implementation requires additional cleanup (eg deletion of files), then
the interface `org.firebirdsql.jna.embedded.spi.DisposableFirebirdEmbeddedLibrary`
should be implemented. This interface provides one additional method:

- `dispose()` to perform any cleanup actions necessary. \
This method will be called by the native resource tracker - if it is enabled -
when the JVM exits. \
As file deletion is not always possible when `dispose()` is called,
implementations should consider implementing a strategy to retry cleanup of old
files on a next run.

### Packaging Firebird Embedded for loading from the classpath

Jaybird provides an implementation of `FirebirdEmbeddedLibrary` that can load
a Firebird Embedded library from the classpath, 
`org.firebirdsql.jna.embedded.classpath.ClasspathFirebirdEmbeddedLibrary`.

To use this implementation, a provider must call
`ClasspathFirebirdEmbeddedLibrary.load` and pass itself and an implementation of
`org.firebirdsql.jna.embedded.classpath.ClasspathFirebirdEmbeddedResource`.
`ClasspathFirebirdEmbeddedResource` identifies the resources to load from the
classpath, and the entry point of the library.

#### Requirements for packaging Firebird Embedded

Libraries or applications packaging Firebird Embedded using 
`ClasspathFirebirdEmbeddedLibrary` should follow these requirements and
recommendations:

- The package name of the provider should be sufficiently unique for the version
and platform. We recommended that the Firebird version and target platform
are part of the package name. This will allow multiple versions and
platforms to coexist on the classpath, allowing for future features like
selecting a specific Firebird version to load, and avoiding potential resource
conflicts (loading the wrong resource). \
For example for a Firebird 3.0.5 targeting Windows 64-bit, a suitable package
name would be `org.example.firebird_3_0_5.win32_x86_64`.
- The files of the library must be inside the folder identified by the package
of the provider class. \
For example, given the provider class 
`org.example.firebird_3_0_5.win32_x86_64.FirebirdEmbeddedProvider`, the files
must be located in `org/example/firebird_3_0_5/win32_x86_64` or a sub-folder of
that location.

Some additional requirements can be found in the documentation of the interface
`org.firebirdsql.jna.embedded.classpath.ClasspathFirebirdEmbeddedResource`.

The files that Jaybird installs through `ClasspathFirebirdEmbeddedLibrary` will
be automatically deleted. In some cases this might only happen on the next run
of a Jaybird application. 

#### Example provider

As an example, a provider for Firebird 3.0.5 on Windows 64-bit can be packaged
as:

```
/--META-INF
|  |--services
|  |   \--org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider
|  \--MANIFEST.MF
|--org
   \--example
      \--firebird_3_0_5
         \--win32_x86_64
            |--fbintl
            |  |--fbintl.conf
            |  \--fbintl.dll
            |--plugins
            |  |--engine12.dll
            |  |--udr_engine.conf
            |  \--udr_engine.dll
            |--fbclient.dll
            |--firebird.conf
            |--firebird.msg
            |--FirebirdEmbeddedProvider.class             
            |--FirebirdEmbeddedProvider$ResourceInfo.class
            |--ib_util.dll
            |--icudt52.dll
            |--icudt52l.dat
            |--icuin52.dll
            |--icuuc52.dll
            \--plugins.conf
```

Where `META-INF/services/org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`
contains:

```
org.example.firebird_3_0_5.win32_x86_64.FirebirdEmbeddedProvider
```

and `org.example.firebird_3_0_5.win32_x86_64.FirebirdEmbeddedProvider` is:

```java
package org.example.firebird_3_0_5.win32_x86_64;

import java.util.Arrays;
import java.util.Collection;

import org.firebirdsql.jna.embedded.classpath.ClasspathFirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.classpath.ClasspathFirebirdEmbeddedResource;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLoadingException;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;

public class FirebirdEmbeddedProvider 
        implements org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider {

    @Override
    public String getPlatform() {
        return "win32-x86-64";
    }

    @Override
    public String getVersion() {
        return "WI-V3.0.5.33220 Firebird 3.0";
    }

    @Override
    public FirebirdEmbeddedLibrary getFirebirdEmbeddedLibrary() 
            throws FirebirdEmbeddedLoadingException {
        return ClasspathFirebirdEmbeddedLibrary.load(this, new ResourceInfo());
    }

    private static class ResourceInfo implements ClasspathFirebirdEmbeddedResource {

        @Override
        public String getLibraryEntryPoint() {
            return "fbclient.dll";
        }

        @Override
        public Collection<String> getResourceList() {
            return Arrays.asList(
                "intl/fbintl.conf",
                "intl/fbintl.dll",
                "plugins/engine12.dll",
                "plugins/udr_engine.conf",
                "plugins/udr_engine.dll",
                "fbclient.dll",
                "firebird.conf",
                "firebird.msg",
                "ib_util.dll",
                "icudt52.dll",
                "icudt52l.dat",
                "icuin52.dll",
                "icuuc52.dll",
                "plugins.conf"
            );
        }
    }
}
``` 