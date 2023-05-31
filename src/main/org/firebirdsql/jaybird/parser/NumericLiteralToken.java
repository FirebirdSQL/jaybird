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
package org.firebirdsql.jaybird.parser;

/**
 * Signals a numeric literal token in the token stream.
 * <p>
 * To keep the parser simple and avoid errors that are better delegated to Firebird server, it is possible that
 * literals are not syntactically valid (they might be incomplete, like {@code 1.0e} or {@code 1.0e+}). Negative
 * numbers are handled as an {@link OperatorToken} followed by a {@code NumericLiteralToken} (possibly separated by a
 * {@link WhitespaceToken}).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class NumericLiteralToken extends AbstractToken implements LiteralToken {

    NumericLiteralToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public NumericLiteralToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

    /**
     * The value of the literal.
     * <p>
     * Be aware, some literals might be incomplete or syntactically invalid.
     * </p>
     *
     * @return value of the literal
     */
    public String value() {
        return text();
    }
}
