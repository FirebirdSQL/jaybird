// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a closing curly brace (<code>}</code>) in the token stream.
 * <p>
 * This token shouldn't occur in Firebird syntax, but is used in JDBC escapes.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class CurlyBraceClose extends AbstractSymbolToken implements CloseToken {

    CurlyBraceClose(int position) {
        super(position);
    }

    @Override
    public String text() {
        return "}";
    }

}
