// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import java.sql.SQLException;

/**
 * Interface for JDBC escape parsing.
 * <p>
 * The intent of this interface is to provide either a parser that converts JDBC escapes to native syntax, or not
 * perform any transformation at all (JDBC escape processing disabled).
 * </p>
 *
 * @since 7
 */
public sealed interface JdbcEscapeParser permits DisabledEscapeParser, FBEscapedParser {

    String toNative(String sql) throws SQLException;

    /**
     * Returns an instance with the appropriate call escape handling.
     * <p>
     * If call escape handling is already as configured, or if call escape handling is irrelevant, the same instance may
     * be returned.
     * </p>
     *
     * @param callEscapeHandling
     *         desired call escape handling
     * @return appropriate instance
     */
    JdbcEscapeParser with(CallEscapeHandling callEscapeHandling);

    static JdbcEscapeParser noEscapeParser() {
        return DisabledEscapeParser.getInstance();
    }

}
