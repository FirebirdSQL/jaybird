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

import static java.lang.String.format;

/**
 * Signals a string literal in the token stream.
 * <p>
 * The literal can be a normal string literal, a binary string literal (possibly with syntactically invalid
 * content) or a Q-literal (alternative quote).
 * </p>
 * <p>
 * NOTE: Dialect 1 quoted identifiers are handled through {@link QuotedIdentifierToken}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
final class StringLiteralToken extends AbstractToken implements LiteralToken {

    // <quote><quote>
    private static final int STRING_LITERAL_MIN_SIZE = 2;
    // Q<quote><start><end><quote>
    private static final int Q_LITERAL_MIN_SIZE = 5;
    // X<quote><quote>
    private static final int X_LITERAL_MIN_SIZE = 3;

    StringLiteralToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public StringLiteralToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

    /**
     * The unescaped and unquoted value of the literal.
     * <p>
     * In the case of binary literals, it will return the content without {@code X'} and {@code '} and
     * perform no further processing. The binary literal value might not be a valid binary literal value.
     * </p>
     *
     * @return unescaped and unquoted value of the literal
     * @throws IllegalStateException
     *         if the value of {@link #text()} is not a valid string literal; this
     *         syntax check is not complete, as it is assumed the instance was produced by
     *         {@link SqlTokenizer}, which should produce valid values.
     */
    public String value() {
        // We assume text contains correct literals, so if it starts with ', then it ends with ', etc.
        final int textLength = length();
        if (textLength < STRING_LITERAL_MIN_SIZE) {
            throw new IllegalStateException("String literal too short, full text: " + text());
        }
        final char firstChar = charAt(0);
        switch (firstChar) {
        case '\'': {
            checkEndsInQuote();
            // remove enclosing quotes
            String value = subSequence(1, textLength - 1).toString();
            return value.indexOf('\'') == -1
                    ? value
                    // unescape single quotes
                    : value.replaceAll("''", "'");
        }
        case 'q':
        case 'Q':
            if (textLength < Q_LITERAL_MIN_SIZE) {
                throw new IllegalStateException("String literal too short, full text: " + text());
            }
            checkEndsInQuote();
            // not checking further syntax
            // remove Q<quote><start> and <end><quote>
            return subSequence(3, textLength - 2).toString();
        case 'x':
        case 'X':
            if (textLength < X_LITERAL_MIN_SIZE) {
                throw new IllegalStateException("String literal too short, full text: " + text());
            }
            checkEndsInQuote();
            // not checking further syntax
            // remove X<quote> and <quote>
            return subSequence(2, textLength - 1).toString();
        default:
            throw new IllegalStateException(
                    format("String literal starts with unexpected character '%s', full text: %s", firstChar, text()));
        }
    }

    private void checkEndsInQuote() {
        char lastChar = charAt(length() - 1);
        if (lastChar != '\'') {
            throw new IllegalStateException(
                    format("String literal ends with unexpected character '%s', expected: ''', full text: %s",
                            lastChar, text()));
        }
    }

}
