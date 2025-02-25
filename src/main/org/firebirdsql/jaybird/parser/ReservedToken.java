// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a reserved word in the token stream that is not represented with a more specific token.
 * <p>
 * Reserved words not represented by this class are (possibly not complete):
 * <dl>
 * <dt>{@code and or not is}</dt>
 * <dd>represented as {@link OperatorToken}</dd>
 * <dt>{@code null}</dt>
 * <dd>represented as {@link NullLiteralToken}</dd>
 * <dt>{@code true false unknown}</dt>
 * <dd>represented as {@link BooleanLiteralToken}</dd>
 * </dl>
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class ReservedToken extends AbstractToken {

    @SuppressWarnings("unused")
    ReservedToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public ReservedToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }

}
