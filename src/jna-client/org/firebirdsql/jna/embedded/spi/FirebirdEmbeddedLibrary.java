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

import java.nio.file.Path;

/**
 * Identifies a Firebird Embedded library that Jaybird can use.
 * <p>
 * It is recommend to implement {@link DisposableFirebirdEmbeddedLibrary} for implementations that require additional
 * cleanup on exit.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
