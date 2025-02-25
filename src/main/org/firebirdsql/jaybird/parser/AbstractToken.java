// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.nio.CharBuffer;

import static java.util.Objects.requireNonNull;

/**
 * Common implementation of {@link Token}.
 *
 * @author Mark Rotteveel
 * @since 5
 */
abstract class AbstractToken implements Token {

    private final int pos;
    private final CharSequence src;
    private String cachedText;

    AbstractToken(int pos, CharSequence src, int start, int end) {
        this.pos = pos;
        if (start == 0 && end == src.length()) {
            this.src = src;
        } else {
            this.src = CharBuffer.wrap(requireNonNull(src, "src"), start, end);
        }
    }

    AbstractToken(int pos, CharSequence tokenText) {
        this.pos = pos;
        src = tokenText;
    }

    @Override
    public final String text() {
        if (this.cachedText != null) {
            return this.cachedText;
        }
        return this.cachedText = src.toString();
    }

    @Override
    public final CharSequence textAsCharSequence() {
        return src;
    }

    @Override
    public void appendTo(StringBuilder sb) {
        sb.append(src);
    }

    @Override
    public final int position() {
        return pos;
    }

    @Override
    public int length() {
        return src.length();
    }

    CharSequence subSequence(int start, int end) {
        return src.subSequence(start, end);
    }

    char charAt(int index) {
        return src.charAt(index);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{" +
                "position=" + pos +
                ", tokenText='" + text() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractToken that = (AbstractToken) o;

        if (pos != that.pos) return false;
        return srcCharsEquals(that);
    }

    @Override
    public boolean equalsIgnoreCase(String tokenText) {
        return CharSequenceComparison.equalsIgnoreCase(src, tokenText);
    }

    private boolean srcCharsEquals(AbstractToken that) {
        int length = src.length();
        if (length != that.src.length()) return false;

        for (int idx = 0; idx < length; idx++) {
            if (src.charAt(idx) != that.src.charAt(idx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = pos;
        result = 31 * result + srcHashCode();
        return result;
    }

    private int srcHashCode() {
        int result = 1;
        for (int idx = 0, end = src.length(); idx < end; idx++) {
            result = 31 * result + src.charAt(idx);
        }
        return result;
    }
}
