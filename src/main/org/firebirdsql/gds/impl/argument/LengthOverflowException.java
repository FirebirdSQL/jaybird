// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.argument;

/**
 * Thrown by {@link StringArgument} and {@link ByteArrayArgument} if a value's length exceeds that of their
 * {@link ArgumentType}.
 *
 * @since 5.0.12
 */
public class LengthOverflowException extends IllegalArgumentException {

    private static final long serialVersionUID = 5735116134576091931L;

    LengthOverflowException(String message) {
        super(message);
    }

}
