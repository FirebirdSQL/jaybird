/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Visitor for tokens.
 * <p>
 * Used by {@link SqlParser} to notify the visitors of tokens.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0.8
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
