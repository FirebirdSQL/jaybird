// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
 * @author Mark Rotteveel
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
            throw literalTooShort(text());
        }
        final char firstChar = charAt(0);
        return switch (firstChar) {
        case '\'' -> {
            checkEndsInQuote();
            // remove enclosing quotes
            String value = subSequence(1, textLength - 1).toString();
            yield value.indexOf('\'') == -1
                    ? value
                    // unescape single quotes
                    : value.replace("''", "'");
        }
        case 'q', 'Q' -> {
            if (textLength < Q_LITERAL_MIN_SIZE) {
                throw literalTooShort(text());
            }
            checkEndsInQuote();
            // not checking further syntax
            // remove Q<quote><start> and <end><quote>
            yield subSequence(3, textLength - 2).toString();
        }
        case 'x', 'X' -> {
            if (textLength < X_LITERAL_MIN_SIZE) {
                throw literalTooShort(text());
            }
            checkEndsInQuote();
            // not checking further syntax
            // remove X<quote> and <quote>
            yield subSequence(2, textLength - 1).toString();
        }
        default -> throw new IllegalStateException(
                format("String literal starts with unexpected character '%s', full text: %s", firstChar, text()));
        };
    }

    private static IllegalStateException literalTooShort(String text) {
        return new IllegalStateException("String literal too short, full text: " + text);
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
