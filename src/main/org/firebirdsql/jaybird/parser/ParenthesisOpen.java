// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals an opening parenthesis ({@code (}) in the token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class ParenthesisOpen extends AbstractSymbolToken implements OpenToken {

    ParenthesisOpen(int position) {
        super(position);
    }

    @Override
    public boolean closedBy(CloseToken closeToken) {
        return closeToken instanceof ParenthesisClose;
    }

    @Override
    public String text() {
        return "(";
    }

}
