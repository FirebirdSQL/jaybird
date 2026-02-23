// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.argument;

import java.io.Serial;

/**
 * Thrown by {@link StringArgument} and {@link ByteArrayArgument} if a value's length exceeds that of their
 * {@link ArgumentType}.
 *
 * @since 7
 */
public class LengthOverflowException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 5735116134576091931L;

    LengthOverflowException(String message) {
        super(message);
    }

}
