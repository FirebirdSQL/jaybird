// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a period ({@code .}) in the token stream.
 * <p>
 * Expected occurrence is as separator between identifiers (e.g. {@code alias.column} or {@code package.function()})
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class PeriodToken extends AbstractSymbolToken {

    public PeriodToken(int position) {
        super(position);
    }

    @Override
    public String text() {
        return ".";
    }

}
