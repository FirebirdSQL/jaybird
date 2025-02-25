// SPDX-FileCopyright: Copyright 2018 Firebird development team and individual contributors
// SPDX-FileContributor: Mark Rotteveel
// SPDX-License-Identifier: MIT
package org.firebirdsql.extern.decimal;

/**
 * How to handle overflows when rounding (converting) to a target decimal type.
 * <p>
 * Overflow occurs when converting a value to the target type would lead to loss of significant digits, or
 * in other words: if the value doesn't fit in the target type.
 * </p>
 * <p>
 * As an example attempts to store {@code 1.0e300} in a {@link Decimal32} leads to an overflow, as the maximum
 * value it can hold is {@code 9.999999e96}.
 * </p>
 *
 * @author Mark Rotteveel
 */
public enum OverflowHandling {

    /**
     * Overflow will round to +/-Infinity, depending on the signum of the value.
     * <p>
     * Underflow will round to zero.
     * </p>
     */
    ROUND_TO_INFINITY,
    /**
     * Overflow will throw a {@link DecimalOverflowException}.
     * <p>
     * Underflow will round to zero.
     * </p>
     */
    THROW_EXCEPTION

}
