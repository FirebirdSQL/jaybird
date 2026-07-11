// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Registrar for visitors that allows runtime removal or addition of visitors.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public interface VisitorRegistrar {

    /**
     * Dummy visitor registrar that does nothing.
     * <p>
     * Can be used as a null-safe value.
     * </p>
     *
     * @see #noActionRegistrar()
     * @since 7
     */
    VisitorRegistrar NO_ACTION_REGISTRAR = new VisitorRegistrar() {
        @Override
        public void addVisitor(TokenVisitor tokenVisitor) {
            // do nothing
        }

        @Override
        public void removeVisitor(TokenVisitor tokenVisitor) {
            // do nothing
        }
    };

    /**
     * Returns a dummy visitor registrar that does nothing.
     * <p>
     * Can be used as a null-safe value. Equivalent to using {@link #NO_ACTION_REGISTRAR} directly.
     * </p>
     *
     * @see #NO_ACTION_REGISTRAR
     * @since 7
     */
    static VisitorRegistrar noActionRegistrar() {
        return NO_ACTION_REGISTRAR;
    }

    /**
     * Adds a visitor.
     *
     * @param tokenVisitor
     *         Token visitor
     */
    void addVisitor(TokenVisitor tokenVisitor);

    /**
     * Removes a visitor - if already registered.
     *
     * @param tokenVisitor
     *         Token visitor
     */
    void removeVisitor(TokenVisitor tokenVisitor);

}
