/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jna.embedded.spi;

import java.util.Collection;

/**
 * Service provider interface to identify a packaged Firebird Embedded library on the classpath.
 * <p>
 * Implementations that provide a Firebird Embedded library need to implement this interface to provide the necessary
 * information to identify if it is a suitable implementation, and to find the files to load. The implementations of
 * this interface need to be listed in {@code META-INF/services/org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider}
 * inside the jar that provides the implementation.
 * </p>
 * <p>
 * For detailed requirements, see <a href="https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2020-05-firebird-embedded-locator-service-provider.md">jdp-2020-05:
 * Firebird Embedded locator service provider</a>
 * </p>
 * <p>
 * This class will be loaded using {@link java.util.ServiceLoader}. Implementations must provide a no-arg constructor.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public interface FirebirdEmbeddedProvider {

    /**
     * Platform of this Firebird Embedded library.
     * <p>
     * Applies the platform naming conventions of JNA.
     * </p>
     *
     * @return Name of the platform (eg {@code "win32-x86-64"} for Windows 64 bit (x86))
     */
    String getPlatform();

    /**
     * Get the Firebird server version of this provider.
     * <p>
     * Implementations should report a version similar as reported by {@code isc_info_firebird_version} and as expected
     * by {@link org.firebirdsql.gds.impl.GDSServerVersion}, that is a format of
     * {@code <platform>-<type><majorVersion>.<minorVersion>.<variant>.<buildNum>[-<revision>] <serverName>},
     * where {@code platform} is a two-character platform identification string, Windows for example is "WI",
     * {@code type} is one of the three characters: "V" - production version, "T" - beta version, "X" - development
     * version.
     * </p>
     * <p>
     * This is not a hard requirement, but failure to comply may exclude the implementation from being used in
     * features like selecting a suitable Firebird Embedded version based on version requirements (such a feature does
     * not exist yet).
     * </p>
     *
     * @return Firebird version information (eg {@code "WI-V3.0.5.33220 Firebird 3.0"})
     */
    String getVersion();

    /**
     * Relative paths against this provider class of the resources with the Firebird Embedded files.
     * <p>
     * The resources must not try to escape the current context using {@code ..}. Implementations trying to do that
     * will not be loaded.
     * </p>
     *
     * @return Collection of resource paths of the Firebird Embedded instance
     */
    Collection<String> getResourceList();

    /**
     * Entry point of the library.
     *
     * @return The relative path of the library entry point (eg {@code "fbclient.dll"} or
     * {@code "lib/libfbclient.so"})
     */
    String getLibraryEntryPoint();

}
