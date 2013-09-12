/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.fields.FieldValue;

import java.util.List;

/**
 * Listener interface for receiving rows and related information as retrieved by
 * an {@link org.firebirdsql.gds.ng.FbStatement#fetchRows(int)}, or {@link org.firebirdsql.gds.ng.FbStatement#execute(java.util.List)} with a singleton result.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface RowListener {

    /**
     * Method to receive a new row of data.
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @param rowData
     *         The rowData as list. Implementer may choose to use an immutable <code>List</code>.
     */
    void newRow(FbStatement sender, List<FieldValue> rowData);

    /**
     * Method to be notified when all rows have been fetched.
     * <p>
     * This method may also be called when the statement did not produce any rows (or did not open a result set).
     * </p>
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @see #statementExecuted(FbStatement, boolean)
     */
    void allRowsFetched(FbStatement sender);

    /**
     * Method to be notified when a statement has been executed.
     * <p>
     * This event with <code>hasResultSet=true</code> can be seen as the counter part of {@link #allRowsFetched(FbStatement)}.
     * </p>
     *
     * @param sender
     *         The <code>FbStatement</code> that called this method.
     * @param hasResultSet
     *         <code>true</code> there is a result set, <code>false</code> there is no result set
     * @param hasSingletonResult
     *         <code>true</code> singleton result, <code>false</code> statement will produce indeterminate number of rows;
     *         can be ignored when <code>hasResultSet</code> is false.
     */
    void statementExecuted(FbStatement sender, boolean hasResultSet, boolean hasSingletonResult);

    // TODO Statement close, next execute?

}
