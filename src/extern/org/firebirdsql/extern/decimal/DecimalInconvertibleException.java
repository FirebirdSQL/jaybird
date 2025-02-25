// SPDX-FileCopyright: Copyright 2018 Firebird development team and individual contributors
// SPDX-FileContributor: Mark Rotteveel
// SPDX-License-Identifier: MIT
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
 * @author Mark Rotteveel
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
