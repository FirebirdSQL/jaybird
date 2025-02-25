// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
