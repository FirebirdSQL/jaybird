// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals one or more whitespace characters in a token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class WhitespaceToken extends AbstractToken {

    WhitespaceToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public WhitespaceToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

    @Override
    public boolean isWhitespaceOrComment() {
        return true;
    }

}
