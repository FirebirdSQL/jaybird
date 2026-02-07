// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.jspecify.annotations.Nullable;

/**
 * Signals a boolean literal ({@code true}, {@code false} or {@code unknown} in the token stream.
 *
 * @author Mark Rotteveel
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
    public boolean equals(@Nullable Object o) {
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
