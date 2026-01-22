// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
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

    /**
     * Type code for {@code DECFLOAT}.
     * <p>
     * When using Java 26 or higher, use {@code java.sql.Types.DECFLOAT}. This constant might be deprecated and
     * removed once Jaybird only supports versions after Java 26.
     * </p>
     */
    @Volatile(reason = "Type code changed in 5.0.12/6.0.5/7.0.0 to match JDBC 4.5 value; prefer java.sql.Types.DECFLOAT when using Java 26 or higher")
    public static final int DECFLOAT = 2015;

    private JaybirdTypeCodes() {
        // no instances
    }
}
