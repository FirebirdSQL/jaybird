// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals a closing parenthesis ({@code )}) in the token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class ParenthesisClose extends AbstractSymbolToken implements CloseToken {

    public ParenthesisClose(int position) {
        super(position);
    }

    @Override
    public String text() {
        return ")";
    }

}
