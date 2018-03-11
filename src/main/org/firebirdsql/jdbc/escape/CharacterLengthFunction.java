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
package org.firebirdsql.jdbc.escape;

/**
 * Implements the {@code CHAR_LENGTH} and {@code CHARACTER_LENGTH} JDBC escape
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
