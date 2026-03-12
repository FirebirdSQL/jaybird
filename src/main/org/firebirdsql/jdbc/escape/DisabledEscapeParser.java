// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

/**
 * Implementation of {@link JdbcEscapeParser} that does not process JDBC escapes.
 *
 * @since 7
 */
final class DisabledEscapeParser implements JdbcEscapeParser {

    private static final DisabledEscapeParser INSTANCE = new DisabledEscapeParser();

    private DisabledEscapeParser() {
    }

    @Override
    public String toNative(String sql) {
        return sql;
    }

    static JdbcEscapeParser getInstance() {
        return INSTANCE;
    }

}
