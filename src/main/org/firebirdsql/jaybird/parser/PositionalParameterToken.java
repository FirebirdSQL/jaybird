// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a positional parameter ({@code ?}) in the token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class PositionalParameterToken extends AbstractSymbolToken {

    PositionalParameterToken(int position) {
        super(position);
    }

    @Override
    public String text() {
        return "?";
    }

}
