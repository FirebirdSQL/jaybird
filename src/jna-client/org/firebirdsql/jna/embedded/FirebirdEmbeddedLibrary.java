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

import java.nio.file.Path;

/**
 * Information for locating a Firebird Embedded library.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class FirebirdEmbeddedLibrary {

    private final Path entryPointPath;
    private final String version;

    FirebirdEmbeddedLibrary(Path entryPointPath, String version) {
        this.entryPointPath = entryPointPath;
        this.version = version;
    }

    /**
     * @return Path of the Firebird Embedded main library file
     */
    public Path getEntryPointPath() {
        return entryPointPath;
    }

    /**
     * Version of the Firebird Embedded library.
     * <p>
     * This version should be parseable by {@link org.firebirdsql.gds.impl.GDSServerVersion}, but this is not
     * guaranteed.
     * </p>
     *
     * @return Version of the Firebird Embedded library
     */
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "FirebirdEmbeddedLibrary{" +
                "entryPointPath=" + entryPointPath +
                ", version='" + version + '\'' +
                '}';
    }

}
