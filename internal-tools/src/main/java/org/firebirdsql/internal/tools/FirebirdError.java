/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.internal.tools;

import static java.util.Objects.requireNonNull;

/**
 * Firebird error information as extracted from Firebird sources.
 *
 * @author Mark Rotteveel
 * @since 6
 */
record FirebirdError(
        Facility facility,
        int numberInFacility,
        String symbolName,
        Integer sqlCode,
        String sqlState,
        String message) {

    FirebirdError {
        requireNonNull(facility, "facility");
        requireNonNull(message, "message");
        if (numberInFacility < 0) {
            throw new IllegalArgumentException("numberInFacility must be zero or positive, was " + numberInFacility);
        }
    }

    int errorCode() {
        return facility.toErrorCode(numberInFacility);
    }

    boolean hasSymbolName() {
        return symbolName != null;
    }

    boolean hasSqlCode() {
        return sqlCode != null;
    }

    boolean hasSqlState() {
        return sqlState != null;
    }

}
