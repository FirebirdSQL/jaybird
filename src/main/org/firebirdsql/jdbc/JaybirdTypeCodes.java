// SPDX-FileCopyrightText: Copyright 2018-2020 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.util.Volatile;

/**
 * Type codes specific for Jaybird.
 *
 * @author Mark Rotteveel
 */
@Volatile(reason = "Defined type codes may receive a different value when standardized in JDBC")
public final class JaybirdTypeCodes {

    // TODO Remove when standardized in JDBC

    @Volatile(reason = "To be standardized by future version of JDBC, type code may change")
    public static final int DECFLOAT = -6001;

    private JaybirdTypeCodes() {
        // no instances
    }
}
