// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.jspecify.annotations.Nullable;

/**
 * Implements the {@code POSITION} JDBC escape.
 * <p>
 * This implementation only supports single parameter or two parameter variant with value {@code CHARACTERS}.
 * If second parameter is {@code OCTETS}, no processing is done.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class PositionFunction implements SQLFunction {

    private static final PatternSQLFunction POSITION = new PatternSQLFunction("POSITION({0})");

    @Override
    public @Nullable String apply(String... parameters) throws FBSQLParseException {
        switch (parameters.length) {
        case 1:
            return POSITION.apply(parameters);
        case 2:
            String typeParam = parameters[1].trim();
            if (typeParam.equalsIgnoreCase("CHARACTERS")) {
                return POSITION.apply(parameters);
            } else if (typeParam.equalsIgnoreCase("OCTETS")) {
                // We can't handle OCTETS, returning null to pass original (without {fn ...} decoration to server
                return null;
            } else {
                throw new FBSQLParseException(
                        "Second parameter for POSITION must be OCTETS or CHARACTERS, was " + typeParam);
            }
        default:
            throw new FBSQLParseException("Expected 1 or 2 parameters for POSITION, received " + parameters.length);
        }
    }
}
