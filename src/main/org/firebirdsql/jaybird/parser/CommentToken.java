// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a block (<code>/*...*{@literal /}<code>) or line comment ({@code --...<linebreak>}, excluding
 * the linebreak) in a token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class CommentToken extends AbstractToken {

    CommentToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public CommentToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

    @Override
    public boolean isWhitespaceOrComment() {
        return true;
    }

}
