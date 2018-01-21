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
 * Thrown to indicate a decimal value could not be converted to a target type (usually {@link java.math.BigDecimal}).
 * <p>
 * This exception should only be thrown for cases when the {@link DecimalType} other than {@link DecimalType#FINITE}
 * cannot be represented in the target type. That is, if the target type does not support Infinity and/or NaN.
 * </p>
 * <p>
 * This exception should not be thrown for cases where the target type supports NaN, but does not support
 * signalling NaN. In that situation, NaN should be returned.
 * </p>
 *
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
 */
public class DecimalInconvertibleException extends ArithmeticException {

    private final DecimalType decimalType;
    private final int signum;

    public DecimalInconvertibleException(String message, DecimalType decimalType, int signum) {
        super(message);
        this.decimalType = decimalType;
        this.signum = signum;
    }

    /**
     * @return Decimal type of the value that could not be converted.
     */
    public DecimalType getDecimalType() {
        return decimalType;
    }

    /**
     * @return Signum of the value that could not be converted.
     */
    public int getSignum() {
        return signum;
    }
}
