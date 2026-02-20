// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.embedded.spi;

import java.io.Serial;

/**
 * Exception to signal errors when loading or identifying a Firebird Embedded library.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class FirebirdEmbeddedLoadingException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public FirebirdEmbeddedLoadingException(String message) {
        super(message);
    }

    public FirebirdEmbeddedLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

}
