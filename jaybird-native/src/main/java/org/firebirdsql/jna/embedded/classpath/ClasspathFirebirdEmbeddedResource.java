// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.embedded.classpath;

import java.util.Collection;

/**
 * Defines how to locate the resources of a Firebird Embedded library on the classpath.
 *
 * @author Mark Rotteveel
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
