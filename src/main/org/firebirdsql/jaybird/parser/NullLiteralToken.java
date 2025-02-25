// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a null in the token stream.
 * <p>
 * The literal {@code UNKNOWN} is signalled as a {@link BooleanLiteralToken}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class NullLiteralToken extends AbstractToken implements LiteralToken {

    NullLiteralToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public NullLiteralToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }
}
