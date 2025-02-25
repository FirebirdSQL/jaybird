// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

/**
 * Signals an open token in the token stream.
 *
 * @author Mark Rotteveel
 * @since 5
 */
interface OpenToken extends Token {

    /**
     * Is this token closed by the provided close token.
     *
     * @param closeToken
     *         Close token
     * @return {@code true} if {@code closeToken} closes this token, {@code false} otherwise
     */
    boolean closedBy(CloseToken closeToken);

}
