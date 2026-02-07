// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.jspecify.annotations.Nullable;

/**
 * Token that is always a single symbol.
 *
 * @author Mark Rotteveel
 * @since 5
 */
abstract class AbstractSymbolToken implements Token {

    private final int pos;

    AbstractSymbolToken(int pos) {
        this.pos = pos;
    }

    @Override
    public final int position() {
        return pos;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSymbolToken that = (AbstractSymbolToken) o;

        return pos == that.pos;
    }

    @Override
    public int hashCode() {
        return pos;
    }

}
