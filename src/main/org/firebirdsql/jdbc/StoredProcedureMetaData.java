// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.util.ObjectReference;
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
public sealed interface StoredProcedureMetaData
        permits DummyStoredProcedureMetaData, PackageAwareStoredProcedureMetaData, SchemaAwareStoredProcedureMetaData {

    /**
     * Determines the selectability of a stored procedure and records it on {@code procedureCall}.
     * <p>
     * On Firebird 6.0 and higher, if the procedure call has ambiguous scope or an unknown schema, the resolved stored
     * procedure will be recorded and the ambiguity removed. We try to replicate the Firebird rules for resolving
     * ambiguity, but it is possible that we diverge. By recording the procedure we resolved, we ensure the executed
     * stored procedure is at least consistent with our decision, and ensure that changes to the search path do not
     * change which procedure is executed. Our implementation of callable statement may internally prepare multiple
     * times over the lifetime of the statement object, while JDBC requires stable schema resolution after
     * {@code prepareCall}.
     * </p>
     * <p>
     * On Firebird versions that do not have selectability information, this will not perform any attempt to resolve
     * selectability. If the procedure cannot be found, it will also be returned as-is, but will then likely fail at
     * execute (or other operations which perform an internal prepare).
     * </p>
     * <p>
     * Implementations may call {@link FBProcedureCall#setObjectReference(ObjectReference)} if not already set, but
     * are <em>not required</em> to do so.
     * </p>
     *
     * @param procedureCall
     *         procedure call information to update
     * @throws SQLException
     *         for failures to query database metadata
     */
    void updateSelectability(FBProcedureCall procedureCall) throws SQLException;
    
}
