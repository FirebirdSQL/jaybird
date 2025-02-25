// SPDX-FileCopyrightText: Copyright 2009 Roman Rokytskyy
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Firebird-specific extensions to the {@link ResultSetMetaData} interface.
 * 
 * @author Roman Rokytskyy
 */
public interface FirebirdResultSetMetaData extends ResultSetMetaData {

    /**
     * Gets the designated column's table alias.
     *
     * @param column the first column is 1, the second is 2, ...
     * @return table alias or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
	String getTableAlias(int column) throws SQLException;
}
