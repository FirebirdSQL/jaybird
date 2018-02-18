/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds;

import java.util.List;
import java.util.Vector;

/**
 * Lookup table for error messages and sql states by error code.
 * <p>
 * See also Firebird {@code src\common\msg_encode.h}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
final class MessageLookup {

    private static final int ISC_MASK = 0x14000000; // Defines the code as a valid ISC code
    private static final int FAC_MASK = 0x00FF0000; // Specifies the facility where the code is located
    private static final int CODE_MASK = 0x0000FFFF; // Specifies the code in the message file
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    // This constant will need to be updated if new facilities are added to Firebird
    private static final int MAX_FACILITY = 26; // Jaybird = 26
    static final int FACILITY_SIZE = MessageLookup.MAX_FACILITY + 1;

    // Lookup from facility + code to message
    private final String[][] messages;
    // Lookup from facility + code to SQL state
    private final String[][] sqlStates;

    MessageLookup(List<Vector<String>> facilityMessages, List<Vector<String>> facilityStates) {
        messages = toLookupArray(facilityMessages);
        sqlStates = toLookupArray(facilityStates);
    }

    /**
     * Retrieves the (error) message for the specified error code.
     *
     * @param errorCode
     *         Error code
     * @return Error message, or {@code null} if not found
     */
    String getErrorMessage(int errorCode) {
        if (isInvalidErrorCode(errorCode)) {
            return null;
        }
        try {
            return messages[getFacility(errorCode)][getCode(errorCode)];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Retrieves the sql state for the specified error code.
     *
     * @param errorCode
     *         Error code
     * @return SQL state, or {@code null} if not found
     */
    String getSqlState(int errorCode) {
        if (isInvalidErrorCode(errorCode)) {
            return null;
        }
        try {
            return sqlStates[getFacility(errorCode)][getCode(errorCode)];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static boolean isInvalidErrorCode(final int errorCode) {
        return (errorCode & ISC_MASK) != ISC_MASK;
    }

    private static String[][] toLookupArray(List<Vector<String>> sourceData) {
        final int facilitySize = sourceData.size();
        final String[][] data = new String[facilitySize][];
        for (int idx = 0; idx < facilitySize; idx++) {
            data[idx] = sourceData.get(idx)
                    .toArray(EMPTY_STRING_ARRAY);
        }
        return data;
    }

    /**
     * Obtain the facility from an error code.
     *
     * @param errorCode
     *         Error code
     * @return Facility
     */
    static int getFacility(int errorCode) {
        return (errorCode & FAC_MASK) >> 16;
    }

    /**
     * Obtain the code within a facility from an error code.
     *
     * @param errorCode
     *         Error code
     * @return Facility
     */
    static int getCode(int errorCode) {
        return (errorCode & CODE_MASK);
    }

}
