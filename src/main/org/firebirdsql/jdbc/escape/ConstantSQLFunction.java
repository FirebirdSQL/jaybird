// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
