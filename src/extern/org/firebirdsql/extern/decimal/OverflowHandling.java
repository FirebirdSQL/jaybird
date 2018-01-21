/*
 * Copyright (c) 2018 Firebird development team and individual contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
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
