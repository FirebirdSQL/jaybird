// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a colon ({@code :}) in the token stream.
 * <p>
 * Expected occurrence is either as the prefix of a named parameter (e.g. in PSQL), or as the separator between
 * array dimensions.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class ColonToken extends AbstractSymbolToken {

    public ColonToken(int position) {
        super(position);
    }

    @Override
    public String text() {
        return ":";
    }

}
