// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a quoted identifier (or - for dialect 1 - a string literal) in the token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class QuotedIdentifierToken extends AbstractToken {

    QuotedIdentifierToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public QuotedIdentifierToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

    /**
     * Unescaped, unquoted name represented by this quoted identifier
     *
     * @return Unescaped and unquoted name (e.g. for {@code "name"} returns {@code name},
     * and for {@code "with""double"} returns {@code with"double})
     */
    public String name() {
        // exclude enclosing quotes
        String name = subSequence(1, length() - 1).toString();

        return name.indexOf('"') == -1
                ? name
                // unescape double quotes
                : name.replace("\"\"", "\"");
    }

    @Override
    public boolean isValidIdentifier() {
        // Could contain characters not valid in UNICODE_FSS, we're ignoring that
        return true;
    }
}
