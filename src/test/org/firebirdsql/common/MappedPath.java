// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import java.nio.file.Path;

/**
 * A mapped path represents the pair of a mapped local filesystem path and its equivalent server-side path.
 *
 * @param local
 *         local (mapped) path
 * @param server
 *         server-side equivalent path
 * @see FBTestProperties#hasMappedDatabaseDirectory()
 */
public record MappedPath(Path local, Path server) {

    /**
     * @return string form of {@link #server()} for use by Firebird
     */
    public String toServerPath() {
        return FBTestProperties.getDatabasePath(server);
    }

}
