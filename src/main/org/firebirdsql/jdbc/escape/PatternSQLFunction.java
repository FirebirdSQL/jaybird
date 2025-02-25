// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import java.text.MessageFormat;

/**
 * Implementation of {@link SQLFunction} with a function pattern.
 * <p>
 * The pattern is a {@link java.text.MessageFormat} pattern string.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
final class PatternSQLFunction implements SQLFunction {

    private final String functionPattern;

    /**
     * Creates the pattern-based SQL function.
     *
     * @param functionPattern
     *         {@link MessageFormat} pattern for the function
     */
    PatternSQLFunction(String functionPattern) {
        this.functionPattern = functionPattern;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will not throw a {@code FBSQLParseException} if the wrong number of parameters are passed.
     * </p>
     */
    @Override
    public String apply(String... parameters) {
        return MessageFormat.format(functionPattern, (Object[]) parameters);
    }
}
