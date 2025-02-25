// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
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
