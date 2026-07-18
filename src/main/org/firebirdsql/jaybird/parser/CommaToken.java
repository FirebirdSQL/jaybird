// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a comma ({@code ,}) in a token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class CommaToken extends AbstractSymbolToken {

    CommaToken(int position) {
        super(position);
    }

    @Override
    public String text() {
        return ",";
    }

}
