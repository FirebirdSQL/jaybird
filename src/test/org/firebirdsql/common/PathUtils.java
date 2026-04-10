// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import java.io.File;
import java.nio.file.Path;

/**
 * Helpers and utilities for working with paths.
 */
public final class PathUtils {

    private PathUtils() {
        // no instances
    }

    /**
     * Returns the equivalent of {@link Path#toString()} with {@link File#separatorChar} replaced by forward slash.
     * <p>
     * This is a naive replacement, so there is no actual guarantee this a valid POSIX path. For example, if
     * {@code path} is a Windows absolute path including a drive letter, the result also contains the drive letter.
     * </p>
     *
     * @param path
     *         path to convert to string
     * @return string representation of {@code path} with forward slash as the separator
     */
    public static String posixPathString(Path path) {
        String pathString = path.toString();
        if (File.separatorChar != '/') {
            pathString = pathString.replace(File.separatorChar, '/');
        }
        return pathString;
    }
}
