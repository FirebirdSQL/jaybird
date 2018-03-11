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
 * Implements the {@code LOCATE} JDBC escape
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
final class LocateFunction implements SQLFunction {

    private static final SQLFunction POSITION_FROM_START = new PatternSQLFunction("POSITION({0},{1})");
    private static final SQLFunction POSITION_FROM_INDEX = new PatternSQLFunction("POSITION({0},{1},{2})");

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        switch (parameters.length) {
        case 2:
            return POSITION_FROM_START.apply(parameters);
        case 3:
            return POSITION_FROM_INDEX.apply(parameters);
        default:
            throw new FBSQLParseException("Expected 2 or 3 parameters for LOCATE, received " + parameters.length);
        }
    }
}
