// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
