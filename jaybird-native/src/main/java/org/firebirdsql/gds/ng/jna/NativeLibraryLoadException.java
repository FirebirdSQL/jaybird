// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

/**
 * Thrown when a native library could not be loaded.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class NativeLibraryLoadException extends RuntimeException {

    public NativeLibraryLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
