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
 * An IEEE-754 Decimal64.
 *
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
 */
public final class Decimal64 extends Decimal<Decimal64> {

    public static final Decimal64 POSITIVE_INFINITY = new Decimal64(Signum.POSITIVE, DecimalType.INFINITY);
    public static final Decimal64 NEGATIVE_INFINITY = new Decimal64(Signum.NEGATIVE, DecimalType.INFINITY);
    public static final Decimal64 POSITIVE_NAN = new Decimal64(Signum.POSITIVE, DecimalType.NAN);
    public static final Decimal64 NEGATIVE_NAN = new Decimal64(Signum.NEGATIVE, DecimalType.NAN);
    public static final Decimal64 POSITIVE_SIGNALING_NAN = new Decimal64(Signum.POSITIVE, DecimalType.SIGNALING_NAN);
    public static final Decimal64 NEGATIVE_SIGNALING_NAN = new Decimal64(Signum.NEGATIVE, DecimalType.SIGNALING_NAN);

    private static final Decimal64Factory DECIMAL_64_FACTORY = new Decimal64Factory();
    private static final DecimalCodec<Decimal64> DECIMAL_64_CODEC = new DecimalCodec<>(DECIMAL_64_FACTORY);

    private Decimal64(int signum, DecimalType decimalType) {
        super(signum, decimalType);
    }

    private Decimal64(int signum, BigDecimal bigDecimal) {
        super(signum, bigDecimal);
    }

    @Override
    DecimalCodec<Decimal64> getDecimalCodec() {
        return DECIMAL_64_CODEC;
    }

    @Override
    DecimalFactory<Decimal64> getDecimalFactory() {
        return DECIMAL_64_FACTORY;
    }

    public static Decimal64 parseBytes(final byte[] decBytes) {
        return DECIMAL_64_CODEC.parseBytes(decBytes);
    }

    /**
     * Creates a {@code Decimal64} from {@code value}, applying rounding where necessary.
     * <p>
     * Values exceeding the range of this type will be returned as +/-Infinity.
     * </p>
     *
     * @param value
     *         Big decimal value to convert
     * @return Decimal64 equivalent
     */
    public static Decimal64 valueOf(final BigDecimal value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal64} from {@code value}, applying rounding where necessary.
     * <p>
     * Values exceeding the range of this type will be handled according to the specified overflow handling.
     * </p>
     *
     * @param value
     *         Big decimal value to convert
     * @param overflowHandling
     *         Overflow handling to apply
     * @return Decimal64 equivalent
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     */
    public static Decimal64 valueOf(final BigDecimal value, final OverflowHandling overflowHandling) {
        return DECIMAL_64_FACTORY.valueOf(value, overflowHandling);
    }

    /**
     * Creates a {@code Decimal64} from {@code value}, applying rounding where necessary.
     * <p>
     * {@code Double.NaN} is mapped to positive NaN, the infinities to their equivalent +/- infinity.
     * </p>
     * <p>
     * For normal, finite, values, this is equivalent to {@code valueOf(BigDecimal.valueOf(value))}.
     * </p>
     *
     * @param value
     *         Double value
     * @return Decimal equivalent
     */
    public static Decimal64 valueOf(final double value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal64} from {@code value}, applying rounding where necessary.
     * <p>
     * {@code Double.NaN} is mapped to positive NaN, the infinities to their equivalent +/- infinity.
     * </p>
     * <p>
     * For normal, finite, values, this is equivalent to {@code valueOf(BigDecimal.valueOf(value), overflowHandling)}.
     * </p>
     *
     * @param value
     *         Double value
     * @param overflowHandling
     *         Overflow handling to apply
     * @return Decimal equivalent
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     */
    public static Decimal64 valueOf(final double value, final OverflowHandling overflowHandling) {
        return DECIMAL_64_FACTORY.valueOf(value, overflowHandling);
    }

    /**
     * Converts a decimal to Decimal64.
     * <p>
     * For normal, finite, decimals, this behaves like {@code valueOf(decimal.toBigDecimal())}, see
     * {@link #valueOf(BigDecimal)}.
     * </p>
     *
     * @param decimal
     *         Decimal to convert
     * @return Decimal converted to Decimal64, or {@code decimal} itself if it already is Decimal64
     */
    public static Decimal64 valueOf(Decimal<?> decimal) {
        return valueOf(decimal, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Converts a decimal to Decimal64.
     * <p>
     * For normal, finite, decimals, this behaves like {@code valueOf(decimal.toBigDecimal(), overflowHandling)}, see
     * {@link #valueOf(BigDecimal, OverflowHandling)}.
     * </p>
     *
     * @param decimal
     *         Decimal to convert
     * @param overflowHandling
     *         Overflow handling to apply
     * @return Decimal converted to Decimal64, or {@code decimal} itself if it already is Decimal64
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     */
    public static Decimal64 valueOf(Decimal<?> decimal, OverflowHandling overflowHandling) {
        if (decimal instanceof Decimal64) {
            return (Decimal64) decimal;
        }
        return DECIMAL_64_FACTORY.valueOf(decimal, overflowHandling);
    }

    /**
     * Creates a {@code Decimal64} from {@code value}, applying rounding where necessary.
     * <p>
     * Except for the special values [+/-]Inf, [+/-]Infinity, [+/-]NaN and [+/-]sNaN (case insensitive), the rules
     * of {@link BigDecimal#BigDecimal(String)} apply, with special handling in place to discern between positive
     * and negative zero.
     * </p>
     * <p>
     * Values exceeding the range of this type will be returned as +/-Infinity.
     * </p>
     *
     * @param value
     *         String value to convert
     * @return Decimal equivalent
     * @throws NumberFormatException
     *         If the provided string is not valid numeric string.
     */
    public static Decimal64 valueOf(final String value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal64} from {@code value}, applying rounding where necessary.
     * <p>
     * Except for the special values [+/-]Inf, [+/-]Infinity, [+/-]NaN and [+/-]sNaN (case insensitive), the rules
     * of {@link BigDecimal#BigDecimal(String)} apply, with special handling in place to discern between positive
     * and negative zero.
     * </p>
     * <p>
     * Values exceeding the range of this type will be handled according to the specified overflow handling.
     * </p>
     *
     * @param value
     *         String value to convert
     * @param overflowHandling
     *         Overflow handling to apply
     * @return Decimal equivalent
     * @throws NumberFormatException
     *         If the provided string is not valid numeric string.
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     */
    public static Decimal64 valueOf(final String value, final OverflowHandling overflowHandling) {
        return DECIMAL_64_FACTORY.valueOf(value, overflowHandling);
    }

    private static class Decimal64Factory extends AbstractDecimalFactory<Decimal64> {

        private Decimal64Factory() {
            super(Decimal64.class, DecimalFormat.Decimal64,
                    POSITIVE_INFINITY, NEGATIVE_INFINITY,
                    POSITIVE_NAN, NEGATIVE_NAN,
                    POSITIVE_SIGNALING_NAN, NEGATIVE_SIGNALING_NAN);
        }

        @Override
        public Decimal64 createDecimal(int signum, BigDecimal value) {
            return new Decimal64(signum, validateRange(value));
        }

    }

}
