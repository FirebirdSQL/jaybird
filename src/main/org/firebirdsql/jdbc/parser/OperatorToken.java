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
 * Signals an operator in the token stream.
 * <p>
 * The term operator is taken very broadly, and includes mathematical operators ({@code + - / *}, boolean operators
 * ({@code and or is not} and comparison operators ({@code = <> > < >= <= != ~= ^= !< ~< ^< !> ~> ^>} and the prefix of
 * those operators ({@code ! ~ ^} if they appear individually in the statement (which is a syntax error in Firebird).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
final class OperatorToken extends AbstractToken {

    OperatorToken(int pos, char[] srcChars, int start, int end) {
        super(pos, srcChars, start, end);
    }

    public OperatorToken(int pos, CharSequence tokenText) {
        super(pos, tokenText);
    }
}
