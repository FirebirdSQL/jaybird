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

import java.text.MessageFormat;

/**
 * Implementation of {@link SQLFunction} with a function pattern.
 * <p>
 * The pattern is a {@link java.text.MessageFormat} pattern string.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
