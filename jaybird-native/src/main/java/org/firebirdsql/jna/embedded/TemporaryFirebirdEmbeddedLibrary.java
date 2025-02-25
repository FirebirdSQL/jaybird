// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
