// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a semicolon ({@code ;}) in the token stream.
 * <p>
 * Expected occurrence is in PSQL bodies, or at the very end of a statement (for syntax flexibility).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class SemicolonToken extends AbstractSymbolToken {

    public SemicolonToken(int position) {
        super(position);
    }

    @Override
    public String text() {
        return ";";
    }

}
