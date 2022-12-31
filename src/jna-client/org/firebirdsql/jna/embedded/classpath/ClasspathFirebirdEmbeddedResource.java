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
package org.firebirdsql.jna.embedded.classpath;

import java.util.Collection;

/**
 * Defines how to locate the resources of a Firebird Embedded library on the classpath.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public interface ClasspathFirebirdEmbeddedResource {

    /**
     * Relative paths against the provider class of the resources with the Firebird Embedded files.
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
     * @return The relative path of the library entry point (e.g. {@code "fbclient.dll"} or {@code "lib/libfbclient.so"})
     */
    String getLibraryEntryPoint();

}
