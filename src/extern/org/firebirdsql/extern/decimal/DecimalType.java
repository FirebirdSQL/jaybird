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
 * Type of decimal.
 *
 * @author <a href="mailto:mark@lawinegevaar.nl">Mark Rotteveel</a>
 */
public enum DecimalType {

    FINITE {
        @Override
        int getSpecialBits() {
            throw new IllegalStateException("Type FINITE has no special bits");
        }
    },
    INFINITY {
        @Override
        int getSpecialBits() {
            return INFINITY_0;
        }
    },
    NAN {
        @Override
        int getSpecialBits() {
            return NAN_QUIET;
        }
    },
    SIGNALING_NAN {
        @Override
        int getSpecialBits() {
            return NAN_SIGNAL;
        }
    };

    private static final int TYPE_MASK = (byte) 0b0_11111_10;
    private static final int INFINITY_0 = 0b0_11110_00;
    private static final int INFINITY_2 = 0b0_11110_10;
    private static final int NAN_QUIET = 0b0_11111_00;
    private static final int NAN_SIGNAL = 0b0_11111_10;

    /**
     * @return Bit combination of this special.
     * @throws IllegalStateException
     *         If this is type FINITE instead
     */
    abstract int getSpecialBits();

    /**
     * @see #fromFirstByte(int)
     */
    static DecimalType fromFirstByte(byte firstByte) {
        // Not masking lower bits, as it is not relevant due to masking in fromFirstByte(int)
        return fromFirstByte((int) firstByte);
    }

    /**
     * Determines the decimal type from the first byte.
     *
     * @param firstByte
     *         First byte of the encoded decimal
     * @return Decimal type
     */
    static DecimalType fromFirstByte(int firstByte) {
        final int type = firstByte & TYPE_MASK;
        switch (type) {
        case INFINITY_0:
        case INFINITY_2:
            return INFINITY;

        case NAN_QUIET:
            return NAN;

        case NAN_SIGNAL:
            return SIGNALING_NAN;

        default:
            assert (firstByte & 0b0_11110_00) != 0b0_11110_00 : "Invalid special " + firstByte;
            return FINITE;
        }
    }

}
