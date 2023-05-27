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
package org.firebirdsql.jna.embedded;

import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;

import java.nio.file.Path;

/**
 * Information for locating a Firebird Embedded library.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class TemporaryFirebirdEmbeddedLibrary implements FirebirdEmbeddedLibrary {

    private final Path entryPointPath;
    private final String version;

    TemporaryFirebirdEmbeddedLibrary(Path entryPointPath, String version) {
        this.entryPointPath = entryPointPath;
        this.version = version;
    }

    @Override
    public Path getEntryPointPath() {
        return entryPointPath;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "TemporaryFirebirdEmbeddedLibrary{" +
                "entryPointPath=" + entryPointPath +
                ", version='" + version + '\'' +
                '}';
    }

}
