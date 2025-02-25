// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals an opening curly brace (<code>{</code>) in the token stream.
 * <p>
 * This token shouldn't occur in Firebird syntax, but is used in JDBC escapes.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class CurlyBraceOpen extends AbstractSymbolToken implements OpenToken {

    public CurlyBraceOpen(int position) {
        super(position);
    }

    @Override
    public boolean closedBy(CloseToken closeToken) {
        return closeToken instanceof CurlyBraceClose;
    }

    @Override
    public String text() {
        return "{";
    }

}
