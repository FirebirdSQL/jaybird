// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

/**
 * Implements the {@code LOCATE} JDBC escape
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class LocateFunction implements SQLFunction {

    private static final PatternSQLFunction POSITION_FROM_START = new PatternSQLFunction("POSITION({0},{1})");
    private static final PatternSQLFunction POSITION_FROM_INDEX = new PatternSQLFunction("POSITION({0},{1},{2})");

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        return switch (parameters.length) {
            case 2 -> POSITION_FROM_START.apply(parameters);
            case 3 -> POSITION_FROM_INDEX.apply(parameters);
            default -> throw new FBSQLParseException(
                    "Expected 2 or 3 parameters for LOCATE, received " + parameters.length);
        };
    }
}
