// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Firebird-specific extensions to the {@link java.sql.ResultSet} interface.
 */
public interface FirebirdResultSet extends ResultSet {

    /**
     * Get execution plan for the specified result set. 
     * 
     * @return execution plan for this query.
     * 
     * @throws SQLException if execution plan cannot be obtained or this result
     * set is already closed.
     * 
     * @see FirebirdPreparedStatement#getExecutionPlan()
     */
    @Nullable String getExecutionPlan() throws SQLException;

    /**
     * Get detailed execution plan for the specified result set.
     *
     * @return detailed execution plan for this query.
     *
     * @throws SQLException if detailed execution plan cannot be obtained or this result
     * set is already closed.
     *
     * @see FirebirdPreparedStatement#getExplainedExecutionPlan()
     */
    @Nullable String getExplainedExecutionPlan() throws SQLException;
}
