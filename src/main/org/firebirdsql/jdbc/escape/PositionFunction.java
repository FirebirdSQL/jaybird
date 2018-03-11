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
 * Implements the {@code POSITION} JDBC escape.
 * <p>
 * This implementation only supports single parameter or two parameter variant with value {@code CHARACTERS}.
 * If second parameter is {@code OCTETS}, no processing is done.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
final class PositionFunction implements SQLFunction {

    private static final SQLFunction POSITION = new PatternSQLFunction("POSITION({0})");

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
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
