// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.embedded.spi;

import java.nio.file.Path;

/**
 * Identifies a Firebird Embedded library that Jaybird can use.
 * <p>
 * It is recommend to implement {@link DisposableFirebirdEmbeddedLibrary} for implementations that require additional
 * cleanup on exit.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface FirebirdEmbeddedLibrary {

    /**
     * @return Path of the Firebird Embedded main library file
     */
    Path getEntryPointPath();

    /**
     * Version of the Firebird Embedded library.
     *
     * @return Version of the Firebird Embedded library
     * @see FirebirdEmbeddedLibrary#getVersion()
     */
    String getVersion();
    
}
