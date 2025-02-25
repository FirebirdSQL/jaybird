// SPDX-FileCopyright: Copyright 2018 Firebird development team and individual contributors
// SPDX-FileContributor: Mark Rotteveel
// SPDX-License-Identifier: MIT
package org.firebirdsql.extern.decimal;

/**
 * Type of decimal.
 *
 * @author Mark Rotteveel
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
