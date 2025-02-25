// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Visitor for tokens.
 * <p>
 * Used by {@link SqlParser} to notify the visitors of tokens.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public interface TokenVisitor {

    /**
     * Notifies the visitor of a token.
     *
     * @param token
     *         Token
     * @param visitorRegistrar
     *         Visitor registrar (can be used to remove itself, or add other visitors)
     */
    void visitToken(Token token, VisitorRegistrar visitorRegistrar);

    /**
     * Signals that the last token was produced and the statement text was fully parsed.
     *
     * @param visitorRegistrar
     *         Visitor registrar (can be used to remove itself, or add other visitors)
     */
    void complete(VisitorRegistrar visitorRegistrar);

}
