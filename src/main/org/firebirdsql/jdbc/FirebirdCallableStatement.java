// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2011-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.CallableStatement;


/**
 * Firebird extension to the {@link java.sql.CallableStatement} interface.
 * 
 * @author Roman Rokytskyy
 */
public interface FirebirdCallableStatement extends FirebirdPreparedStatement, CallableStatement {

    /**
     * Mark this callable statement as a call of the selectable procedure. By
     * default, callable statement uses "EXECUTE PROCEDURE" SQL statement to
     * invoke stored procedures that return single row of output parameters or
     * a result set. In former case it retrieves only the first row of the 
     * result set.
     *  
     * @see #isSelectableProcedure()
     * @param selectable <code>true</code> if the called procedure is selectable.
     */
    void setSelectableProcedure(boolean selectable);

    /**
     * Retrieve if this callable statement has been marked as selectable.
     * <p>
     * Starting from Firebird 2.1, this value is set automatically from metadata stored in the
     * database. Prior to Firebird 2.1, it must be set manually.
     * </p>
     * 
     * @see #setSelectableProcedure(boolean)
     * @return <code>true</code> if the called procedure is selectable, false otherwise
     */
	boolean isSelectableProcedure();
}
