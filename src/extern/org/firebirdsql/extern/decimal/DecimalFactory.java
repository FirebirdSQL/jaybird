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

import java.math.BigDecimal;

/**
 * Factory for decimal values.
 *
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
 */
interface DecimalFactory<T> {

    /**
     * @return Decimal format information of the decimals created by this factory.
     */
    DecimalFormat getDecimalFormat();

    /**
     * Returns the constant for the decimal type and signum.
     *
     * @param signum
     *         Signum ({@code -1}, {@code 0} and {@code 1}, where {@code 0} is handled as {@code 1}.
     * @param decimalType
     *         Decimal type (infinity, NaN, signaling NaN)
     * @return Special constant
     */
    T getSpecialConstant(int signum, DecimalType decimalType);

    /**
     * Creates a decimal.
     * <p>
     * The signum value must be consistent with the big decimal value. It is necessary to distinguish between
     * {@code -0} and {@code +0} cases. Signum {@code 0} is allowed for zero cases and will be handled as {@code 1}.
     * </p>
     * <p>
     * No rounding or other measures for fitting the value will be applied, the provided big decimal value must already
     * fulfill the requirements for this decimal format.
     * </p>
     *
     * @param signum
     *         Signum ({@code -1} or {@code 1}), must be consistent with the big decimal (except for zero cases)
     * @param value
     *         Big decimal value
     * @return Decimal wrapper for this value.
     * @throws DecimalOverflowException
     *         If the coefficient or exponent is out of range for this decimal type.
     */
    T createDecimal(int signum, BigDecimal value);

}
