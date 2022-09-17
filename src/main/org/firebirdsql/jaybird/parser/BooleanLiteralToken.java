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
 * Signals a boolean literal ({@code true}, {@code false} or {@code unknown} in the token stream.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
final class BooleanLiteralToken extends AbstractToken implements LiteralToken {

    private final BooleanType type;

    private BooleanLiteralToken(BooleanType type, int pos, CharSequence src, int start, int end) {
        super(pos, src, start, end);
        this.type = type;
    }

    private BooleanLiteralToken(BooleanType type, int pos, CharSequence tokenText) {
        super(pos, tokenText);
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!super.equals(o)) return false;

        BooleanLiteralToken that = (BooleanLiteralToken) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public boolean isTrue() {
        return type == BooleanType.TRUE;
    }

    public boolean isFalse() {
        return type == BooleanType.FALSE;
    }

    public boolean isUnknown() {
        return type == BooleanType.UNKNOWN;
    }

    static BooleanLiteralToken trueToken(int pos, CharSequence src, int start, int end) {
        return new BooleanLiteralToken(BooleanType.TRUE, pos, src, start, end);
    }

    public static BooleanLiteralToken trueToken(int pos, CharSequence tokenText) {
        return new BooleanLiteralToken(BooleanType.TRUE, pos, tokenText);
    }

    static BooleanLiteralToken falseToken(int pos, CharSequence src, int start, int end) {
        return new BooleanLiteralToken(BooleanType.FALSE, pos, src, start, end);
    }

    public static BooleanLiteralToken falseToken(int pos, CharSequence tokenText) {
        return new BooleanLiteralToken(BooleanType.FALSE, pos, tokenText);
    }

    static BooleanLiteralToken unknownToken(int pos, CharSequence src, int start, int end) {
        return new BooleanLiteralToken(BooleanType.UNKNOWN, pos, src, start, end);
    }

    public static BooleanLiteralToken unknownToken(int pos, CharSequence tokenText) {
        return new BooleanLiteralToken(BooleanType.UNKNOWN, pos, tokenText);
    }

    private enum BooleanType {
        TRUE,
        FALSE,
        UNKNOWN
    }

}
