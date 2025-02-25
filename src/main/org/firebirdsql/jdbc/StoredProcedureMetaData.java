// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;

/**
 * Meta-information on stored procedures in a Firebird database.
 * <p>
 * This interface is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this
 * type.
 * </p>
 */
@InternalApi
public interface StoredProcedureMetaData {

    /**
     * Determine if the "selectability" of procedures is available. 
     * This functionality is only available starting from Firebird 2.1, 
     * and only with databases created by that version or later.
     * 
     * @return {@code true} if selectability information is available,  {@code false} otherwise
     */
    boolean canGetSelectableInformation();
    
    /**
     * Retrieve whether a given stored procedure is selectable.
     * <p>
     * A selectable procedure is one that can return multiple rows of results (i.e. it uses a {@code SUSPEND}
     * statement).
     * </p>
     * 
     * @param procedureName 
     *      The name of the procedure for which selectability information is to be retrieved
     * @return
     *      {@code true} if the procedure is selectable, {@code false} otherwise
     * @throws SQLException If no selectability information is available
     */
    boolean isSelectable(String procedureName) throws SQLException;
    
}
