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
package org.firebirdsql.jaybird.parser;

/**
 * Simplified abstraction for token visitors that are not interested in the visitor registrar.
 * <p>
 * Implementations should override {@link #visitToken(Token)} and/or {@link #complete()}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
abstract class AbstractTokenVisitor implements TokenVisitor {

    @Override
    public final void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        visitToken(token);
    }

    protected void visitToken(Token token) {
        // do nothing
    }

    @Override
    public final void complete(VisitorRegistrar visitorRegistrar) {
        complete();
    }

    protected void complete() {
        // do nothing
    }
}
