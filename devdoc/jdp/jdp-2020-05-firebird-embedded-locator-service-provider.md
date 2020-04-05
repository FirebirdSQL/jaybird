# jdp-2020-05: Firebird Embedded locator service provider

## Status

- Draft
- Implemented in: Jaybird 5

## Type

- Feature-specification

## Context

Firebird can be run as an embedded database server. Jaybird supports using
Firebird Embedded. However - especially since Firebird 3 - using Firebird
Embedded is really cumbersome due to the number of libraries and other files you
need to deploy together with the application. As a consequence it is hard to use
Firebird Embedded with Jaybird.

## Decision

Jaybird will introduce a service provider interface,
`org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`, with packaging
requirements that will enable Jaybird to locate and load Firebird Embedded
library files from the classpath.

Jaybird will load the embedded library of the first provider that was located on
the classpath for the current platform that also installed successfully. Future
versions of Jaybird may introduce more advanced features like selecting a
library based on version requirements.

### Open questions

1. Will this work for using Firebird Embedded on Linux and MacOS?

## Consequences

Jaybird provides a service provider interface 
`org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`. Implementations of
this interface will be discovered through `java.util.ServiceLoader`. When
creating an embedded connection, Jaybird will attempt to locate a suitable
Firebird Embedded instance on the classpath and install it to a temporary
location. It will then try and load this library before any other libraries on
the search path.

### Requirements for packaging Firebird Embedded

Libraries or applications packaging Firebird Embedded for use by Jaybird should
follow these requirements and recommendations:

- The package name of the provider should be sufficiently unique for the version
and platform. It is recommended that the Firebird version and target platform
are included in the package name. This will allow multiple versions and
platforms to coexist on the classpath, allowing for future extensions like
selecting a specific Firebird version to load, and avoiding potential resource
conflicts (loading the wrong resource). \
For example for a Firebird 3.0.5 targeting Windows 64 bit, a suitable package
name would be `org.example.firebird_3_0_5.win32_x86_64`.
- The files of the library must be inside the folder identified by the package
of the provider class. \
For example, given the provider class 
`org.example.firebird_3_0_5.win32_x86_64.FirebirdEmbeddedProvider`, the files
must be located in `org/example/firebird_3_0_5/win32_x86_64` or a sub-folder of
that location.
- The platform information reported by `FirebirdEmbeddedProvider.getPlaform`
must use the conventions of [JNA](https://github.com/java-native-access/jna).
The returned value must match the value of `com.sun.jna.Platform.RESOURCE_PREFIX`,
for example for Windows 64 bit (x86), the platform is `win32-x86-64`, for Linux
64 bit (x86), it is `linux-x86-64`.
- The version information reported by `FirebirdEmbeddedProvider.getVersion()`
should have the format as reported by `isc_info_firebird_version` and as
expected by `GDSServerVersion`. That is, its format should be 
`<platform>-<type><majorVersion>.<minorVersion>.<variant>.<buildNum>[-<revision>] <serverName>`, 
where platform is a two-character platform identification string, Windows for
example is "WI", type is one of the three characters: "V" - production version,
"T" - beta version, "X" - development version. \
This is not a hard requirement, but failure to comply may exclude the
implementation from being used in features like selecting a suitable Firebird
Embedded version based on version requirements (such a feature does not exist
yet).

Some of the requirements for this service provider interface are documented
in the interface `org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`.

The files Jaybird installs from the classpath will be marked for deletion on
exit, but this is a best effort cleanup only. On Windows it is not possible to
delete files that are in use, and it appears that Firebird does not close all
libraries on shutdown.

TODO: Clean up needs further refinement.

### Example provider

As an example, a provider for Firebird 3.0.5 on Windows 64 bit can be packaged
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
            |--ib_util.dll
            |--icudt52.dll
            |--icudt52l.dat
            |--icuin52.dll
            |--icuuc52.dll
            \--plugins.conf
```

Where `META-INF/services/org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider`
would contain:

```
org.example.firebird_3_0_5.win32_x86_64.FirebirdEmbeddedProvider
```

and `org.example.firebird_3_0_5.win32_x86_64.FirebirdEmbeddedProvider` would be:

```java
package org.example.firebird3_0_5.win32_x86_64;

import java.util.Arrays;
import java.util.Collection;

public class FirebirdEmbeddedProvider 
        implements org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider {

    @Override
    public String getLibraryEntryPoint() {
        return "fbclient.dll";
    }

    @Override
    public String getPlatform() {
        return "win32-x86-64";
    }

    @Override
    public String getVersion() {
        return "WI-V3.0.5.33220 Firebird 3.0";
    }

    @Override
    public Collection<String> getResourceList() {
        return Arrays.asList(
            "intl/fbintl.conf",
            "intl/fbintl.dll",
            "plugins/engine12.dll",
            "plugins/legacy_auth.dll",
            "plugins/srp.dll",
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
``` 