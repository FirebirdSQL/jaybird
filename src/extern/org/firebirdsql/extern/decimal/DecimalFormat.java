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
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

import static org.firebirdsql.extern.decimal.DenselyPackedDecimalCodec.BITS_PER_GROUP;
import static org.firebirdsql.extern.decimal.DenselyPackedDecimalCodec.DIGITS_PER_GROUP;

/**
 * Constant values for the decimal 32, 64 and 128 formats.
 *
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
 */
enum DecimalFormat {

    Decimal32(32, 7, MathContext.DECIMAL32),
    Decimal64(64, 16, MathContext.DECIMAL64),
    Decimal128(128, 34, MathContext.DECIMAL128);

    private static final int SIGN_BITS = 1;
    private static final int COMBINATION_BITS = 5;

    final int formatBitLength;
    final int formatByteLength;
    final int coefficientDigits;
    final int exponentContinuationBits;
    final int coefficientContinuationBits;
    final int eLimit;
    private final int eMax;
    private final int eMin;
    private final int exponentBias;
    private final BigInteger maxCoefficient;
    private final BigInteger minCoefficient;
    private final MathContext mathContext;

    DecimalFormat(int formatBitLength, int coefficientDigits, MathContext mathContext) {
        this.mathContext = mathContext;
        assert formatBitLength > 0 && formatBitLength % 8 == 0;
        this.formatBitLength = formatBitLength;
        formatByteLength = formatBitLength / 8;
        this.coefficientDigits = coefficientDigits;
        coefficientContinuationBits = calculateCoefficientContinuationBits(coefficientDigits);
        exponentContinuationBits = calculateExponentContinuationBits(formatBitLength, coefficientContinuationBits);
        eLimit = calculateExponentLimit(exponentContinuationBits);
        eMin = -eLimit / 2;
        eMax = -eMin + 1;
        exponentBias = -eMin + coefficientDigits - 1;
        char[] digits = new char[coefficientDigits];
        Arrays.fill(digits, '9');
        maxCoefficient = new BigInteger(new String(digits));
        minCoefficient = maxCoefficient.negate();
    }

    final MathContext getMathContext() {
        return mathContext;
    }

    /**
     * Attempts to round the provided value to fit the requirements of this decimal format.
     * <p>
     * The following steps are taken:
     * </p>
     * <ul>
     * <li>the value is rounded to the precision required</li>
     * <li>the value is rescaled to the scale boundaries if possible, for small values (out of range negative
     * exponents) this may round to a value of zero</li>
     * </ul>
     * <p>
     * The resulting value may still have a scale that is out of range for this decimal format,
     * use {@link #isOutOfRange(BigDecimal)} after rounding to check if the value should be handled as
     * {@code +/-Infinity} instead.
     * </p>
     *
     * @param value
     *         Big decimal value to round
     * @return Big decimal value that may have been rounded to fit this decimal format, use
     * {@link #isOutOfRange(BigDecimal)} to verify.
     */
    final BigDecimal tryRound(BigDecimal value) {
        final BigDecimal roundedToPrecision = value.round(mathContext);
        final int scaleAdjustment = requiredScaleAdjustment(roundedToPrecision);
        if (scaleAdjustment == 0) {
            return roundedToPrecision;
        }
        if (scaleAdjustment < 0) {
            return roundedToPrecision
                    .setScale(roundedToPrecision.scale() + scaleAdjustment, mathContext.getRoundingMode());
        }
        if (roundedToPrecision.compareTo(BigDecimal.ZERO) == 0
                || scaleAdjustment <= coefficientDigits - roundedToPrecision.precision()) {
            return roundedToPrecision
                    .setScale(roundedToPrecision.scale() + scaleAdjustment, RoundingMode.UNNECESSARY);
        }
        return roundedToPrecision;
    }

    /**
     * Validates if the precision and scale of the big decimal value fit the requirements of this decimal format.
     *
     * @param value
     *         Big decimal value
     * @return value if validation succeeded
     * @throws DecimalOverflowException
     *         If the precision of the value exceeds the maximum coefficient digits, or if the
     *         scale is out of range
     */
    final BigDecimal validate(BigDecimal value) {
        final int precision = value.precision();
        if (precision > coefficientDigits) {
            throw new DecimalOverflowException("Precision " + precision + " exceeds the maximum of "
                    + coefficientDigits + " for this type");
        }
        if (requiredScaleAdjustment(value) != 0) {
            throw new DecimalOverflowException("The scale " + value.scale() + " is out of range for this type");
        }
        return value;
    }

    /**
     * Checks if the current precision or scale of the provided value is out of range for this decimal format.
     * <p>
     * This method can be used to check if a big decimal should be handled as {@code +/-Infinity} after
     * {@link #tryRound(BigDecimal)} has been applied.
     * </p>
     *
     * @param value
     *         Big decimal value
     * @return {@code true} if {@code value} is out of range (precision or scale) for this decimal format
     */
    final boolean isOutOfRange(BigDecimal value) {
        return value.precision() > coefficientDigits || requiredScaleAdjustment(value) != 0;
    }

    /**
     * Scale adjustment needed to make the supplied big decimal value fit this decimal format.
     *
     * @param value
     *         Big decimal value
     * @return Scale adjustment (+/-), or {@code 0} if no adjustment is needed.
     */
    private int requiredScaleAdjustment(BigDecimal value) {
        int biasedExponent = biasedExponent(-value.scale());
        if (biasedExponent >= 0) {
            if (biasedExponent <= eLimit) {
                return 0;
            } else {
                return biasedExponent - eLimit;
            }
        } else {
            return biasedExponent;
        }
    }

    final int biasedExponent(int unbiasedExponent) {
        return unbiasedExponent + exponentBias;
    }

    final int unbiasedExponent(int biasedExponent) {
        return biasedExponent - exponentBias;
    }

    /**
     * Validates the format length.
     *
     * @param decBytes
     *         Decimal bytes
     * @throws IllegalArgumentException
     *         If the byte array has the wrong length
     */
    final void validateByteLength(byte[] decBytes) {
        if (decBytes.length != formatByteLength) {
            throw new IllegalArgumentException("decBytes argument must be " + formatByteLength + " bytes");
        }
    }

    private static int calculateCoefficientContinuationBits(int coefficientDigits) {
        return BITS_PER_GROUP * (coefficientDigits - 1) / DIGITS_PER_GROUP;
    }

    private static int calculateExponentContinuationBits(int formatBitLength, int coefficientContinuationBits) {
        return formatBitLength - SIGN_BITS - COMBINATION_BITS - coefficientContinuationBits;
    }

    private static int calculateExponentLimit(int exponentContinuationBits) {
        return 3 * (1 << exponentContinuationBits) - 1;
    }
}
