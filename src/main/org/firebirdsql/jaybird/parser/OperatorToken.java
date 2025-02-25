// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals an operator in the token stream.
 * <p>
 * The term operator is taken very broadly, and includes mathematical operators ({@code + - / *}, boolean operators
 * ({@code and or is not} and comparison operators ({@code = <> > < >= <= != ~= ^= !< ~< ^< !> ~> ^>} and the prefix of
 * those operators ({@code ! ~ ^} if they appear individually in the statement (which is a syntax error in Firebird).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class OperatorToken extends AbstractToken {

    OperatorToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public OperatorToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }
}
