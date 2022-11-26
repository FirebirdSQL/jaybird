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
package org.firebirdsql.jdbc.parser;

/**
 * Signals a null in the token stream.
 * <p>
 * The literal {@code UNKNOWN} is signalled as a {@link BooleanLiteralToken}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0.8
 */
final class NullLiteralToken extends AbstractToken implements LiteralToken {

    NullLiteralToken(int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
    }

    public NullLiteralToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }
}
