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
