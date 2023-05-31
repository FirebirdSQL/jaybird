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

/**
 * An IEEE-754 Decimal32.
 *
 * @author Mark Rotteveel
 */
public final class Decimal32 extends Decimal<Decimal32> {

    public static final Decimal32 POSITIVE_INFINITY = new Decimal32(Signum.POSITIVE, DecimalType.INFINITY);
    public static final Decimal32 NEGATIVE_INFINITY = new Decimal32(Signum.NEGATIVE, DecimalType.INFINITY);
    public static final Decimal32 POSITIVE_NAN = new Decimal32(Signum.POSITIVE, DecimalType.NAN);
    public static final Decimal32 NEGATIVE_NAN = new Decimal32(Signum.NEGATIVE, DecimalType.NAN);
    public static final Decimal32 POSITIVE_SIGNALING_NAN = new Decimal32(Signum.POSITIVE, DecimalType.SIGNALING_NAN);
    public static final Decimal32 NEGATIVE_SIGNALING_NAN = new Decimal32(Signum.NEGATIVE, DecimalType.SIGNALING_NAN);

    private static final Decimal32Factory DECIMAL_32_FACTORY = new Decimal32Factory();
    private static final DecimalCodec<Decimal32> DECIMAL_32_CODEC = new DecimalCodec<>(DECIMAL_32_FACTORY);

    private Decimal32(int signum, DecimalType decimalType) {
        super(signum, decimalType);
    }

    private Decimal32(int signum, BigDecimal bigDecimal) {
        super(signum, bigDecimal);
    }

    @Override
    DecimalCodec<Decimal32> getDecimalCodec() {
        return DECIMAL_32_CODEC;
    }

    @Override
    DecimalFactory<Decimal32> getDecimalFactory() {
        return DECIMAL_32_FACTORY;
    }

    /**
     * Parses the provided byte array to a {@code Decimal32}.
     * <p>
     * This method parses network byte-order (aka big-endian). When using little-endian order, you will need to
     * reverse the bytes in the array first.
     * </p>
     *
     * @param decBytes
     *         Bytes of the Decimal32 value in network byte-order (aka big-endian)
     * @return Instance of {@code Decimal32}
     * @throws IllegalArgumentException
     *         When {@code decBytes} is not 4 bytes long
     */
    public static Decimal32 parseBytes(final byte[] decBytes) {
        return DECIMAL_32_CODEC.parseBytes(decBytes);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
     * <p>
     * Values exceeding the range of this type will be returned as +/-Infinity.
     * </p>
     *
     * @param value
     *         Big decimal value to convert
     * @return Decimal32 equivalent
     */
    public static Decimal32 valueOf(final BigDecimal value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
     * <p>
     * Values exceeding the range of this type will be handled according to the specified overflow handling.
     * </p>
     *
     * @param value
     *         Big decimal value to convert
     * @param overflowHandling
     *         Overflow handling to apply
     * @return Decimal32 equivalent
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     */
    public static Decimal32 valueOf(final BigDecimal value, final OverflowHandling overflowHandling) {
        return DECIMAL_32_FACTORY.valueOf(value, overflowHandling);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
     * <p>
     * Values exceeding the range of this type will be returned as +/-Infinity.
     * </p>
     *
     * @param value
     *         Big integer value to convert
     * @return Decimal32 equivalent
     */
    public static Decimal32 valueOf(final BigInteger value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
     * <p>
     * Values exceeding the range of this type will be handled according to the specified overflow handling.
     * </p>
     * <p>
     * Calling this method is equivalent to {@code valueOf(new BigDecimal(value), overflowHandling)}.
     * </p>
     *
     * @param value
     *         Big integer value to convert
     * @param overflowHandling
     *         Handling of overflows
     * @return Decimal32 equivalent
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     * @see #valueOfExact(BigInteger)
     */
    public static Decimal32 valueOf(final BigInteger value, final OverflowHandling overflowHandling) {
        return DECIMAL_32_FACTORY.valueOf(value, overflowHandling);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, rejecting values that would lose precision due to rounding.
     *
     * @param value Big integer value to convert
     * @throws DecimalOverflowException
     *         If the value is out of range.
     * @return Decimal32 equivalent
     * @see #valueOf(BigInteger, OverflowHandling)
     */
    public static Decimal32 valueOfExact(final BigInteger value) {
        return DECIMAL_32_FACTORY.valueOfExact(value);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
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
    public static Decimal32 valueOf(final double value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
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
    public static Decimal32 valueOf(final double value, final OverflowHandling overflowHandling) {
        return DECIMAL_32_FACTORY.valueOf(value, overflowHandling);
    }

    /**
     * Converts a decimal to Decimal32.
     * <p>
     * For normal, finite, decimals, this behaves like {@code valueOf(decimal.toBigDecimal())}, see
     * {@link #valueOf(BigDecimal)}.
     * </p>
     *
     * @param decimal
     *         Decimal to convert
     * @return Decimal converted to Decimal32, or {@code decimal} itself if it already is Decimal32
     */
    public static Decimal32 valueOf(Decimal<?> decimal) {
        return valueOf(decimal, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Converts a decimal to Decimal32.
     * <p>
     * For normal, finite, decimals, this behaves like {@code valueOf(decimal.toBigDecimal(), overflowHandling)}, see
     * {@link #valueOf(BigDecimal, OverflowHandling)}.
     * </p>
     *
     * @param decimal
     *         Decimal to convert
     * @param overflowHandling
     *         Overflow handling to apply
     * @return Decimal converted to Decimal32, or {@code decimal} itself if it already is Decimal32
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
     */
    public static Decimal32 valueOf(Decimal<?> decimal, OverflowHandling overflowHandling) {
        if (decimal instanceof Decimal32) {
            return (Decimal32) decimal;
        }
        return DECIMAL_32_FACTORY.valueOf(decimal, overflowHandling);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
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
    public static Decimal32 valueOf(final String value) {
        return valueOf(value, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Creates a {@code Decimal32} from {@code value}, applying rounding where necessary.
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
    public static Decimal32 valueOf(final String value, final OverflowHandling overflowHandling) {
        return DECIMAL_32_FACTORY.valueOf(value, overflowHandling);
    }

    private static class Decimal32Factory extends AbstractDecimalFactory<Decimal32> {

        private Decimal32Factory() {
            super(Decimal32.class, DecimalFormat.Decimal32,
                    POSITIVE_INFINITY, NEGATIVE_INFINITY,
                    POSITIVE_NAN, NEGATIVE_NAN,
                    POSITIVE_SIGNALING_NAN, NEGATIVE_SIGNALING_NAN);
        }

        @Override
        public Decimal32 createDecimal(int signum, BigDecimal value) {
            return new Decimal32(signum, validateRange(value));
        }

    }

}
