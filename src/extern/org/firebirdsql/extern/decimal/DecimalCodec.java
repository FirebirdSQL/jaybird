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
 * Encodes and decodes decimal values.
 *
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
 */
final class DecimalCodec<T extends Decimal<T>> {

    /**
     * Byte pattern that signals that the combination field contains 1 bit of the first digit (for value 8 or 9).
     */
    private static final int COMBINATION_2 = 0b0_11000_00;
    private static final int NEGATIVE_BIT = 0b1000_0000;

    private final DecimalFactory<T> decimalFactory;
    private final DecimalFormat decimalFormat;
    private final DenselyPackedDecimalCodec coefficientCoder;

    /**
     * Constructs a decimal codec.
     *
     * @param decimalFactory
     *         Decimal factory to use when encoding and decoding
     */
    DecimalCodec(DecimalFactory<T> decimalFactory) {
        this.decimalFactory = decimalFactory;
        this.decimalFormat = decimalFactory.getDecimalFormat();
        coefficientCoder = new DenselyPackedDecimalCodec(decimalFormat.coefficientDigits);
    }

    /**
     * Parse an IEEE-754 decimal format to a decimal.
     *
     * @param decBytes
     *         byte representation
     * @return Decoded decimal
     * @throws IllegalArgumentException
     *         If the byte array has the wrong length for the decimal type of this codec
     */
    T parseBytes(final byte[] decBytes) {
        decimalFormat.validateByteLength(decBytes);

        final int firstByte = decBytes[0] & 0xff;
        final int signum = -1 * (firstByte >>> 7) | 1;
        final DecimalType decimalType = DecimalType.fromFirstByte(firstByte);
        if (decimalType != DecimalType.FINITE) {
            return decimalFactory.getSpecialConstant(signum, decimalType);
        } else {
            // NOTE: get exponent MSB from combination field and first 2 bits of exponent continuation in one go
            final int exponentMSB;
            final int firstDigit;
            if ((firstByte & COMBINATION_2) != COMBINATION_2) {
                exponentMSB = (firstByte >>> 3) & 0b01100 | (firstByte & 0b011);
                firstDigit = (firstByte >>> 2) & 0b0111;
            } else {
                exponentMSB = (firstByte >>> 1) & 0b01100 | (firstByte & 0b011);
                firstDigit = 0b01000 | ((firstByte >>> 2) & 0b01);
            }
            final int exponentBitsRemaining = decimalFormat.exponentContinuationBits - 2;
            assert exponentBitsRemaining
                    == decimalFormat.formatBitLength - 8 - decimalFormat.coefficientContinuationBits
                    : "Unexpected exponent remaining length " + exponentBitsRemaining;
            final int exponent =
                    decimalFormat.unbiasedExponent(decodeExponent(decBytes, exponentMSB, exponentBitsRemaining));
            final BigInteger coefficient = coefficientCoder.decodeValue(signum, firstDigit, decBytes);

            return decimalFactory.createDecimal(signum, new BigDecimal(coefficient, -exponent));
        }
    }

    /**
     * Encodes a decimal to its IEEE-754 format.
     *
     * @param decimal
     *         Decimal
     * @return Byte array with the encoded decimal
     * @throws DecimalOverflowException
     *         If the exponent or coefficient of the decimal exceeds the supported range of the decimal format
     */
    byte[] encodeDecimal(final T decimal) {
        final byte[] decBytes = new byte[decimalFormat.formatByteLength];

        if (decimal.signum() == Signum.NEGATIVE) {
            decBytes[0] = (byte) NEGATIVE_BIT;
        }

        if (decimal.getType() == DecimalType.FINITE) {
            encodeFinite(
                    decimalFormat.validate(decimal.toBigDecimal()),
                    decBytes);
        } else {
            decBytes[0] |= decimal.getType().getSpecialBits();
        }

        return decBytes;
    }

    private void encodeFinite(BigDecimal decimal, byte[] decBytes) {
        final int biasedExponent = decimalFormat.biasedExponent(-decimal.scale());
        final BigInteger coefficient = decimal.unscaledValue();
        final int mostSignificantDigit = coefficientCoder.encodeValue(coefficient, decBytes);
        final int expMSB = biasedExponent >>> decimalFormat.exponentContinuationBits;
        final int expTwoBitCont = (biasedExponent >>> decimalFormat.exponentContinuationBits - 2) & 0b011;
        if (mostSignificantDigit <= 7) {
            decBytes[0] |= ((expMSB << 5)
                    | (mostSignificantDigit << 2)
                    | expTwoBitCont);
        } else {
            decBytes[0] |= (COMBINATION_2
                    | (expMSB << 3)
                    | ((mostSignificantDigit & 0b01) << 2)
                    | expTwoBitCont);
        }
        encodeExponentContinuation(decBytes, biasedExponent, decimalFormat.exponentContinuationBits - 2);
    }

    private static void encodeExponentContinuation(byte[] decBytes, final int expAndBias, int expBitsRemaining) {
        int expByteIndex = 1;
        while (expBitsRemaining > 8) {
            decBytes[expByteIndex++] = (byte) (expAndBias >>> expBitsRemaining - 8);
            expBitsRemaining -= 8;
        }
        if (expBitsRemaining > 0) {
            decBytes[expByteIndex] |= (expAndBias << 8 - expBitsRemaining);
        }
    }

    static int decodeExponent(final byte[] decBytes, final int exponentMSB, int exponentBitsRemaining) {
        int exponent = exponentMSB;
        int byteIndex = 1;
        while (exponentBitsRemaining > 8) {
            exponent = (exponent << 8) | (decBytes[byteIndex] & 0xFF);
            exponentBitsRemaining -= 8;
            byteIndex += 1;
        }
        if (exponentBitsRemaining > 0) {
            exponent = (exponent << exponentBitsRemaining)
                    | ((decBytes[byteIndex] & 0xFF) >>> (8 - exponentBitsRemaining));
        }
        return exponent;
    }

}
