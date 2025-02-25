// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals an opening square bracket ({@code [} in the token stream.
 * <p>
 * Expected occurrence is in definition of array dimensions or when dereferencing an array element.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class SquareBracketOpen extends AbstractSymbolToken implements OpenToken {

    public SquareBracketOpen(int position) {
        super(position);
    }

    @Override
    public boolean closedBy(CloseToken closeToken) {
        return closeToken instanceof SquareBracketClose;
    }

    @Override
    public String text() {
        return "[";
    }

}
