// SPDX-FileCopyright: Copyright 2018-2026 Firebird development team and individual contributors
// SPDX-FileContributor: Mark Rotteveel
// SPDX-License-Identifier: MIT
package org.firebirdsql.extern.decimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for IEEE-754 decimals.
 *
 * @author Mark Rotteveel
 */
public abstract sealed class Decimal<T extends Decimal<T>> permits Decimal32, Decimal64, Decimal128 {

    private final int signum;
    private final DecimalType type;
    private final BigDecimal bigDecimal;

    Decimal(int signum, DecimalType type) {
        //noinspection ConstantValue
        assert type != null : "Type should not be null";
        assert type != DecimalType.FINITE : "Constructor only suitable for non-FINITE";
        assert -1 == signum || signum == 1 : "Invalid signum, " + signum;
        this.signum = signum;
        this.type = type;
        bigDecimal = BigDecimal.ZERO;
    }

    Decimal(int signum, BigDecimal bigDecimal) {
        assert -1 <= signum && signum <= 1 : "Invalid signum, " + signum;
        this.type = DecimalType.FINITE;
        this.signum = signum != 0 ? signum : Signum.POSITIVE;
        this.bigDecimal = requireNonNull(bigDecimal, "bigDecimal");
        if (bigDecimal.compareTo(BigDecimal.ZERO) != 0 && this.signum != bigDecimal.signum()) {
            throw new IllegalArgumentException("Signum value not consistent with big decimal value, was: "
                    + signum + ", expected: " + bigDecimal.signum());
        }
    }

    /**
     * Converts this decimal to a {@code BigDecimal}.
     *
     * @return Value as BigDecimal
     * @throws DecimalInconvertibleException
     *         If this value is a NaN, sNaN or Infinity, which can't be represented as a {@code BigDecimal}.
     */
    public final BigDecimal toBigDecimal() {
        if (type != DecimalType.FINITE) {
            throw new DecimalInconvertibleException(
                    "Value " + this + " cannot be converted to a BigDecimal", type, signum);
        }
        return bigDecimal;
    }

    /**
     * Converts this decimal to a double value.
     * <p>
     * For normal, finite, decimal values, see {@link BigDecimal#doubleValue()}.
     * </p>
     * <p>
     * For type INFINITY, returns {@code Double.POSITIVE_INFINITY} or {@code Double.NEGATIVE_INFINITY}. For all
     * NaN-specials, returns {@code Double.NaN} (irrespective of signum).
     * </p>
     *
     * @return this decimal converted to a {@code double}
     */
    public final double doubleValue() {
        return switch (type) {
            case FINITE -> bigDecimal.doubleValue();
            case INFINITY -> signum == Signum.NEGATIVE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            // No differentiation between positive/negative and signaling/normal
            case NAN, SIGNALING_NAN -> Double.NaN;
        };
    }

    /**
     * Converts this decimal to its IEEE-754 byte encoding in network byte-order (aka big-endian).
     * <p>
     * This method returns network byte-order (aka big-endian). When you need little-endian order, you will need to
     * reverse the bytes in the array.
     * </p>
     *
     * @return byte array
     */
    @SuppressWarnings("unchecked")
    public final byte[] toBytes() {
        return getDecimalCodec().encodeDecimal((T) this);
    }

    /**
     * Converts this decimal to the requested decimal type, rounding when necessary.
     *
     * @param decimalType
     *         Target decimal type
     * @param <D>
     *         Type parameter of decimal
     * @return This value after conversion, or this if {@code decimalType} is the same as this type
     * @throws IllegalArgumentException
     *         If conversion to {@code decimalType} is not supported
     */
    public final <D extends Decimal<D>> D toDecimal(Class<D> decimalType) {
        return toDecimal(decimalType, OverflowHandling.ROUND_TO_INFINITY);
    }

    /**
     * Converts this decimal to the requested decimal type, rounding when necessary.
     *
     * @param decimalType
     *         Target decimal type
     * @param overflowHandling
     *         Handling of overflows
     * @param <D>
     *         Type parameter of decimal
     * @return This value after conversion, or this if {@code decimalType} is the same as this type
     * @throws IllegalArgumentException
     *         If conversion to {@code decimalType} is not supported
     * @throws DecimalOverflowException
     *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range for the target decimal type.
     */
    public final <D extends Decimal<D>> D toDecimal(Class<D> decimalType, OverflowHandling overflowHandling) {
        if (decimalType == getClass()) {
            return decimalType.cast(this);
        } else if (decimalType == Decimal128.class) {
            return decimalType.cast(Decimal128.valueOf(this, overflowHandling));
        } else if (decimalType == Decimal64.class) {
            return decimalType.cast(Decimal64.valueOf(this, overflowHandling));
        } else if (decimalType == Decimal32.class) {
            return decimalType.cast(Decimal32.valueOf(this, overflowHandling));
        } else {
            throw new IllegalArgumentException("Unsupported conversion to " + decimalType.getName());
        }
    }

    final DecimalType getType() {
        return type;
    }

    final int signum() {
        return signum;
    }

    /**
     * @return {@code true} if this value is zero (ignoring scale), {@code false} if this is a special, or not zero.
     */
    final boolean isEquivalentToZero() {
        return type == DecimalType.FINITE
                && BigDecimal.ZERO.compareTo(bigDecimal) == 0;
    }

    /**
     * @return The codec for this decimal type.
     */
    abstract DecimalCodec<T> getDecimalCodec();

    /**
     * @return The decimal factory for this decimal type.
     */
    abstract DecimalFactory<T> getDecimalFactory();

    /**
     * Negates this decimal (positive to negative, negative to positive).
     *
     * @return Negated value
     */
    final T negate() {
        final DecimalFactory<T> decimalFactory = getDecimalFactory();
        if (type != DecimalType.FINITE) {
            return decimalFactory.getSpecialConstant(-1 * signum, type);
        }
        return decimalFactory.createDecimal(-1 * signum, bigDecimal.negate());
    }

    @Override
    public final String toString() {
        return switch (type) {
            case FINITE -> {
                if (signum == Signum.NEGATIVE && isEquivalentToZero()) {
                    yield "-" + bigDecimal;
                }
                yield bigDecimal.toString();
            }
            case INFINITY -> signum == Signum.NEGATIVE ? "-Infinity" : "+Infinity";
            case NAN -> signum == Signum.NEGATIVE ? "-NaN" : "+NaN";
            case SIGNALING_NAN -> signum == Signum.NEGATIVE ? "-sNaN" : "+sNaN";
        };
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Decimal<?> decimal = (Decimal<?>) o;

        if (signum != decimal.signum) return false;
        if (type != decimal.type) return false;
        return Objects.equals(bigDecimal, decimal.bigDecimal);
    }

    @Override
    public final int hashCode() {
        int result = signum;
        result = 31 * result + type.hashCode();
        result = 31 * result + bigDecimal.hashCode();
        return result;
    }

    abstract static class AbstractDecimalFactory<T extends Decimal<T>> implements DecimalFactory<T> {

        private final Class<T> type;
        private final DecimalFormat decimalFormat;
        private final T positiveInfinity;
        private final T negativeInfinity;
        private final T positiveNan;
        private final T negativeNan;
        private final T positiveSignalingNaN;
        private final T negativeSignalingNaN;

        AbstractDecimalFactory(Class<T> type, DecimalFormat decimalFormat,
                T positiveInfinity, T negativeInfinity,
                T positiveNan, T negativeNan,
                T positiveSignalingNaN, T negativeSignalingNaN) {
            this.type = type;
            this.decimalFormat = decimalFormat;
            this.positiveInfinity = positiveInfinity;
            this.negativeInfinity = negativeInfinity;
            this.positiveNan = positiveNan;
            this.negativeNan = negativeNan;
            this.positiveSignalingNaN = positiveSignalingNaN;
            this.negativeSignalingNaN = negativeSignalingNaN;
        }

        @Override
        public final DecimalFormat getDecimalFormat() {
            return decimalFormat;
        }

        /**
         * @return Math context for the decimal type constructed.
         */
        private MathContext getMathContext() {
            return decimalFormat.getMathContext();
        }

        /**
         * @see DecimalFormat#validate(BigDecimal)
         */
        final BigDecimal validateRange(BigDecimal value) {
            return decimalFormat.validate(value);
        }

        /**
         * Creates a decimal from {@code value}, applying rounding where necessary.
         * <p>
         * Values exceeding the range of this type will be handled according to the specified overflow handling.
         * </p>
         *
         * @param value
         *         Big decimal value to convert
         * @param overflowHandling
         *         Handling of overflows
         * @return Decimal equivalent
         * @throws DecimalOverflowException
         *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
         */
        final T valueOf(BigDecimal value, OverflowHandling overflowHandling) {
            final BigDecimal roundedValue = decimalFormat.tryRound(value);
            if (overflowHandling == OverflowHandling.ROUND_TO_INFINITY && decimalFormat.isOutOfRange(roundedValue)) {
                return getSpecialConstant(roundedValue.signum(), DecimalType.INFINITY);
            }
            // OverflowHandling.THROW_EXCEPTION is handled implicitly in createDecimal
            // Using value.signum() as rounding may round to zero, which would lose the signum information
            return createDecimal(value.signum(), roundedValue);
        }

        /**
         * Creates a decimal from {@code value}, applying rounding where necessary.
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
         * @return Decimal equivalent
         * @throws DecimalOverflowException
         *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
         * @see #valueOfExact(BigInteger)
         */
        final T valueOf(BigInteger value, OverflowHandling overflowHandling) {
            return valueOf(new BigDecimal(value, getMathContext()), overflowHandling);
        }

        /**
         * Creates a decimal from {@code value}, rejecting values that would lose precision due to rounding.
         *
         * @param value
         *         Big integer value to convert
         * @return Decimal equivalent
         * @throws DecimalOverflowException
         *         If the value is out of range.
         * @see #valueOf(BigInteger, OverflowHandling)
         */
        final T valueOfExact(BigInteger value) {
            final BigDecimal bigDecimal = new BigDecimal(decimalFormat.validateCoefficient(value));
            return createDecimal(value.signum(), bigDecimal);
        }

        /**
         * Creates a decimal from {@code value}, applying rounding where necessary.
         * <p>
         * {@code Double.NaN} is mapped to positive NaN, the infinities to their equivalent +/- infinity.
         * </p>
         * <p>
         * For normal, finite, values, this is equivalent to
         * {@code valueOf(BigDecimal.valueOf(value), overflowHandling)}.
         * </p>
         *
         * @param value
         *         Double value
         * @param overflowHandling
         *         Handling of overflows
         * @return Decimal equivalent
         * @throws DecimalOverflowException
         *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
         */
        final T valueOf(double value, OverflowHandling overflowHandling) {
            if (Double.isNaN(value)) {
                return getSpecialConstant(Signum.POSITIVE, DecimalType.NAN);
            } else if (value == Double.POSITIVE_INFINITY) {
                return getSpecialConstant(Signum.POSITIVE, DecimalType.INFINITY);
            } else if (value == Double.NEGATIVE_INFINITY) {
                return getSpecialConstant(Signum.NEGATIVE, DecimalType.INFINITY);
            }

            return valueOf(new BigDecimal(Double.toString(value), getMathContext()), overflowHandling);
        }

        /**
         * Converts a decimal to this type.
         * <p>
         * For normal, finite, decimals, this behaves like {@code valueOf(decimal.toBigDecimal(), overflowHandling)},
         * see {@link #valueOf(BigDecimal, OverflowHandling)}.
         * </p>
         *
         * @param decimal
         *         Decimal to convert
         * @param overflowHandling
         *         Handling of overflows
         * @return Decimal converted to this type, or {@code decimal} itself if it already is of this type
         * @throws DecimalOverflowException
         *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
         */
        final T valueOf(Decimal<?> decimal, OverflowHandling overflowHandling) {
            if (decimal.getClass() == type) {
                return type.cast(decimal);
            } else if (decimal.type == DecimalType.FINITE) {
                return valueOf(decimal.bigDecimal, overflowHandling);
            } else {
                return getSpecialConstant(decimal.signum, decimal.type);
            }
        }

        /**
         * Creates a decimal from {@code value}, applying rounding where necessary.
         * <p>
         * Except for the special values [+/-]Inf, [+/-]Infinity, [+/-]NaN and [+/-]sNaN (case-insensitive), the rules
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
         *         Handling of overflows
         * @return Decimal equivalent
         * @throws NumberFormatException
         *         If the provided string is not valid numeric string.
         * @throws DecimalOverflowException
         *         If {@code OverflowHandling#THROW_EXCEPTION} and the value is out of range.
         */
        final T valueOf(String value, OverflowHandling overflowHandling) {
            if (value.length() > 2) {
                char checkChar = value.charAt(0);
                if (checkChar == '+' || checkChar == '-') {
                    checkChar = value.charAt(1);
                }
                if (checkChar == 'i' || checkChar == 'I'
                        || checkChar == 'n' || checkChar == 'N'
                        || checkChar == 's' || checkChar == 'S') {
                    return valueOfSpecial(value);
                }
            }
            BigDecimal bdValue = new BigDecimal(value, getMathContext());
            T decimalValue = valueOf(bdValue, overflowHandling);
            if (decimalValue.isEquivalentToZero()
                && value.charAt(0) == '-'
                && bdValue.signum() != Signum.NEGATIVE) {
                return decimalValue.negate();
            }
            return decimalValue;
        }

        private T valueOfSpecial(String special) {
            return switch (special.toLowerCase()) {
                case "inf", "infinity", "+inf", "+infinity" ->
                        getSpecialConstant(Signum.POSITIVE, DecimalType.INFINITY);
                case "-inf", "-infinity" -> getSpecialConstant(Signum.NEGATIVE, DecimalType.INFINITY);
                case "nan", "+nan" -> getSpecialConstant(Signum.POSITIVE, DecimalType.NAN);
                case "-nan" -> getSpecialConstant(Signum.NEGATIVE, DecimalType.NAN);
                case "snan", "+snan" -> getSpecialConstant(Signum.POSITIVE, DecimalType.SIGNALING_NAN);
                case "-snan" -> getSpecialConstant(Signum.NEGATIVE, DecimalType.SIGNALING_NAN);
                default -> throw new NumberFormatException("Invalid value " + special);
            };
        }

        @Override
        public final T getSpecialConstant(int signum, DecimalType decimalType) {
            return switch (decimalType) {
                case INFINITY -> signum == Signum.NEGATIVE
                        ? negativeInfinity
                        : positiveInfinity;
                case NAN -> signum == Signum.NEGATIVE
                        ? negativeNan
                        : positiveNan;
                case SIGNALING_NAN -> signum == Signum.NEGATIVE
                        ? negativeSignalingNaN
                        : positiveSignalingNaN;
                default -> throw new AssertionError("Invalid special value for decimalType " + decimalType);
            };
        }

    }

}
