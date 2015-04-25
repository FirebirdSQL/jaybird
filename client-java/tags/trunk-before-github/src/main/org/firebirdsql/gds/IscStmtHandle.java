/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */
package org.firebirdsql.gds;

/**
 * <code>isc_stmt_handle</code> describes a handle to a database statement.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface IscStmtHandle {
    
    int TYPE_UNKNOWN = -1;
    int TYPE_SELECT = ISCConstants.isc_info_sql_stmt_select;
    int TYPE_INSERT = ISCConstants.isc_info_sql_stmt_insert;
    int TYPE_UPDATE = ISCConstants.isc_info_sql_stmt_update;
    int TYPE_DELETE = ISCConstants.isc_info_sql_stmt_delete;
    int TYPE_DDL = ISCConstants.isc_info_sql_stmt_ddl;
    int TYPE_GET_SEGMENT = ISCConstants.isc_info_sql_stmt_get_segment;
    int TYPE_PUT_SEGMENT = ISCConstants.isc_info_sql_stmt_put_segment;
    int TYPE_EXEC_PROCEDURE = ISCConstants.isc_info_sql_stmt_exec_procedure;
    int TYPE_START_TRANS = ISCConstants.isc_info_sql_stmt_start_trans;
    int TYPE_COMMIT = ISCConstants.isc_info_sql_stmt_commit;
    int TYPE_ROLLBACK = ISCConstants.isc_info_sql_stmt_rollback;
    int TYPE_SELECT_FOR_UPDATE = ISCConstants.isc_info_sql_stmt_select_for_upd;
    int TYPE_SET_GENERATOR = ISCConstants.isc_info_sql_stmt_set_generator;
    
    /**
     * Get the {@link IscDbHandle} associated with this statement
     * 
     * @return Instance of {@link IscDbHandle}
     */
    IscDbHandle getRsr_rdb();
    
    /**
     * Sets  the {@link IscDbHandle} associated with this statement
     * @param value Instance of {@link IscDbHandle}
     */
    void setRsr_rdb(IscDbHandle value);
    
    /**
     * Add warning to this statement
     * 
     * @param warning Warning to add
     */
    void addWarning(GDSException warning);

    /**
     * @return The statement text
     */
    String getStatementText();
    
    /**
     * @param statement The statement text
     */
    void setStatementText(String statement);
    
    /**
     * Get the input data structure that contains data that is put into
     * the statement.
     *
     * @return The input data structure
     */
    XSQLDA getInSqlda();
    
    /**
     * Sets the input data structure that contains data that is put into
     * the statement.
     *
     * @param xsqlda The input data structure
     */
    void setInSqlda(XSQLDA xsqlda);

    /**
     * Get the output data structure that contains data that is retrieved
     * from the statement.
     *
     * @return The output data structure
     */
    XSQLDA getOutSqlda();
    
    /**
     * Sets the output data structure that contains data that is retrieved
     * from the statement.
     *
     * @param xsqlda The output data structure
     */
    void setOutSqlda(XSQLDA xsqlda);

    /**
     * Get the execution plan from the statement.
     * 
     * @return execution plan or <code>null</code> if the execution plan was 
     * not fetched from the server.
     */
    String getExecutionPlan();
    
    /**
     * Sets the execution plan from the statement.
     * 
     * @param plan execution plan or <code>null</code> if the execution plan was 
     * not fetched from the server.
     */
    void setExecutionPlan(String plan);
    
    /**
     * @return The Firebird server id for this statement
     */
    int getRsrId();
    
    /**
     * @param value The Firebird server id for this statement
     */
    void setRsrId(int value);
    
    /**
     * Get the statement type.
     * 
     * @return one of the constants defined in this interface or {@link #TYPE_UNKNOWN}
     * when no statement type was received from the server.
     */
    int getStatementType();
    
    /**
     * Sets the statement type
     * @param statementType one of the constants defined in this interface or {@link #TYPE_UNKNOWN}
     * when no statement type was received from the server.
     */
    void setStatementType(int statementType);
    
    // TODO: Move rows / size related methods to separate interface
    
    /**
     * Retrieve whether all rows have been fetched of the rows selected
     * by executing this statement.
     *
     * @return <code>true</code> if all rows have been fetched,
     *         <code>false</code> otherwise
     */
    boolean isAllRowsFetched();
    
    /**
     * Set whether all rows have been fetched.
     * 
     * @param value <code>true</code> if all rows have been fetched, <code>false</code> otherwise
     */
    void setAllRowsFetched(boolean value);
    
    /**
     * Get the number of rows contained in this statement.
     *
     * @return The rowcount for this statement
     */
    int size();
    
    /**
     * Ensure capacity for specified number of rows.
     * 
     * @param maxSize Capacity for rows to be created/reserved
     */
    void ensureCapacity(int maxSize);

    /**
     * Remove all rows contained by this statement. This method differs from
     * {@link #clearRows} in that it effectively clears all rows from this
     * statement.
     *
     * @see #clearRows
     */
    void removeRows();
    
    /**
     * Clear all rows that have been fetched for this statement. This 
     * method differs from {@link #removeRows} in that it only affects rows 
     * that have already been fetched.
     *
     * @see #removeRows
     */
    void clearRows();
    
    /**
     * Get the rows retrieved by executing this statement.
     *
     * @return Array of rows retrieved
     */
    byte[][][] getRows();
    
    /**
     * Adds row to collection of fetched rows
     * 
     * @param row Row to add
     */
    void addRow(byte[][] row);
    
    // TODO Consider moving count methods to separate interface
    
    /**
     * Get the number of rows that were deleted by executing this statement.
     *
     * @return The number of deleted rows
     */
    int getDeleteCount();
    
    /**
     * Sets the number of rows that were deleted by executing this statement.
     *
     * @param deleteCount The number of deleted rows
     */
    void setDeleteCount(int deleteCount);

    /**
     * Get the number of rows that were inserted by executing this statement.
     *
     * @return The number of inserted rows
     */
    int getInsertCount();
    
    /**
     * Sets the number of rows that were inserted by executing this statement.
     *
     * @param insertCount The number of inserted rows
     */
    void setInsertCount(int insertCount);
    
    /**
     * Get the number of rows that were updated by executing this statement.
     *
     * @return The number of updated rows
     */
    int getUpdateCount();
    
    /**
     * Sets the number of rows that were updated by executing this statement.
     *
     * @param updateCount The number of updated rows
     */
    void setUpdateCount(int updateCount);
    
    /**
     * Get the number of rows that were selected by executing this statement.
     *
     * @return The number of selected rows
     */
    int getSelectCount();
    
    /**
     * Get the number of rows that were selected by executing this statement.
     *
     * @param selectCount The number of selected rows
     */
    void setSelectCount(int selectCount);
    
    /**
     * Register statement for the transaction. This method is used within
     * the <code>GDS.iscDsqlExecXXX</code> methods to keep a reference on
     * current transaction in which statement is executed.
     * 
     * @param trHandle instance of {@link IscTrHandle}.
     */
    void registerTransaction(IscTrHandle trHandle);
    
    /**
     * Get current transaction in which statement is currently executed.
     * 
     * @return instance of {@link IscTrHandle} or <code>null</code>
     * if statement is not assigned to a transaction.
     */
    IscTrHandle getTransaction();
    
    /**
     * Unregister statement from the transaction.
     */
    void unregisterTransaction();
    
    /**
     * Retrieve whether this statement has an open <code>ResultSet</code>.
     *
     * @return <code>true</code> if this statement has an open 
     *         <code>ResultSet</code>, false otherwise
     */
    boolean hasOpenResultSet();
    
    /**
     * Notifies this statement that there are open resultsets.
     */
    void notifyOpenResultSet();
    
    /**
     * Retrieve whether this statement has singleton result set.
     * 
     * @return <code>true</code> if result set has singleton result set.
     */
    boolean isSingletonResult();
    
    /**
     * Sets whether this statement has singleton result set.
     * 
     * @param value <code>true</code> if result set has singleton result set.
     */
    void setSingletonResult(boolean value);

    /**
     * Retrieve whether or not this statement is valid.
     *
     * @return <code>true</code> if this is a valid statement, 
     *         <code>false</code> otherwise
     */
    boolean isValid();
}
