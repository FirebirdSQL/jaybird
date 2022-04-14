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

import java.nio.CharBuffer;

import static java.util.Objects.requireNonNull;

/**
 * Common implementation of {@link Token}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
abstract class AbstractToken implements Token {

    private final int pos;
    private final CharSequence srcChars;
    private String cachedText;

    AbstractToken(int pos, char[] srcChars, int start, int end) {
        this(pos, CharBuffer.wrap(requireNonNull(srcChars, "srcChars"), start, end - start));
    }

    AbstractToken(int pos, CharSequence tokenText) {
        this.pos = pos;
        srcChars = tokenText;
    }

    @Override
    public final String text() {
        if (this.cachedText != null) {
            return this.cachedText;
        }
        return this.cachedText = srcChars.toString();
    }

    @Override
    public void appendTo(StringBuilder sb) {
        sb.append(srcChars);
    }

    @Override
    public final int position() {
        return pos;
    }

    @Override
    public int length() {
        return srcChars.length();
    }

    CharSequence subSequence(int start, int end) {
        return srcChars.subSequence(start, end);
    }

    char charAt(int index) {
        return srcChars.charAt(index);
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

    private boolean srcCharsEquals(AbstractToken that) {
        int length = srcChars.length();
        if (length != that.srcChars.length()) return false;

        for (int idx = 0; idx < length; idx++) {
            if (srcChars.charAt(idx) != that.srcChars.charAt(idx)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = pos;
        result = 31 * result + srcCharsHashCode();
        return result;
    }

    private int srcCharsHashCode() {
        int result = 1;
        for (int idx = 0, end = srcChars.length(); idx < end; idx++) {
            result = 31 * result + srcChars.charAt(idx);
        }
        return result;
    }
}
