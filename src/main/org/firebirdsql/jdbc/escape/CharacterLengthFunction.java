// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

/**
 * Implements the {@code CHAR_LENGTH} and {@code CHARACTER_LENGTH} JDBC escape
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class CharacterLengthFunction implements SQLFunction {

    private static final SQLFunction CHAR_LENGTH_FUNCTION = new PatternSQLFunction("CHAR_LENGTH({0})");
    private static final SQLFunction OCTET_LENGTH_FUNCTION = new PatternSQLFunction("OCTET_LENGTH({0})");

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        switch (parameters.length) {
        case 1:
            return CHAR_LENGTH_FUNCTION.apply(parameters);
        case 2:
            String typeParam = parameters[1].trim();
            if ("CHARACTERS".equalsIgnoreCase(typeParam)) {
                return CHAR_LENGTH_FUNCTION.apply(parameters);
            } else if ("OCTETS".equalsIgnoreCase(typeParam)) {
                return OCTET_LENGTH_FUNCTION.apply(parameters);
            } else {
                throw new FBSQLParseException(
                        "Second parameter for CHAR(ACTER)_LENGTH must be OCTETS or CHARACTERS, was " + parameters[1]);
            }
        default:
            throw new FBSQLParseException(
                    "Expected 1 or 2 parameters for CHAR(ACTER)_LENGTH, received " + parameters.length);
        }
    }
}
