// SPDX-FileCopyright: Copyright 2018-2023 Firebird development team and individual contributors
// SPDX-FileContributor: Mark Rotteveel
// SPDX-License-Identifier: MIT
package org.firebirdsql.extern.decimal;

/**
 * Exception thrown to indicate a coefficient or exponent overflow or underflow.
 *
 * @author Mark Rotteveel
 */
public final class DecimalOverflowException extends ArithmeticException {

    public DecimalOverflowException(String message) {
        super(message);
    }

}
