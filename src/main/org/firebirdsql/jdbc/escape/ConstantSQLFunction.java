/*
 * Firebird Open Source JDBC Driver
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
 * Implementation of {@link SQLFunction} for constants or functions without parameters.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class ConstantSQLFunction implements SQLFunction {

    private final String functionConstant;

    ConstantSQLFunction(String functionConstant) {
        this.functionConstant = functionConstant;
    }

    @Override
    public String apply(String... parameters) throws FBSQLParseException {
        if (parameters.length > 0) {
            throw new FBSQLParseException(
                    "Invalid number of arguments, expected no arguments, received " + parameters.length);
        }
        return functionConstant;
    }
}
