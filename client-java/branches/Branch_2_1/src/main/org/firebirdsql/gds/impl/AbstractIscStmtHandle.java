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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.IscStmtHandle;


/**
 * Abstract implementation of the {@link org.firebirdsql.gds.IscStmtHandle}
 * interface.
 */
public abstract class AbstractIscStmtHandle implements IscStmtHandle {
    
    private String executionPlan;
    private int statementType = TYPE_UNKNOWN;
    public String statement;

    /**
     * Clear all rows that have been fetched for this statement. This 
     * method differs from {@link #removeRows} in that it only affects rows 
     * that have already been fetched.
     *
     * @see #removeRows
     */
    public abstract void clearRows();

    /**
     * Get the number of rows that were deleted by executing this statement.
     *
     * @return The number of deleted rows
     */
    public abstract int getDeleteCount();

    /**
     * Get the number of rows that were inserted by executing this statement.
     *
     * @return The number of inserted rows
     */
    public abstract int getInsertCount();

    /**
     * Get the rows retrieved by executing this statement.
     *
     * @return Array of rows retrieved
     */
    public abstract byte[][][] getRows();

    /**
     * Get the number of rows that were updated by executing this statement.
     *
     * @return The number of updated rows
     */
    public abstract int getUpdateCount();

    /**
     * Retrieve whether this statement has an open <code>ResultSet</code>.
     *
     * @return <code>true</code> if this statement has an open 
     *         <code>ResultSet</code>, false otherwise
     */
    public abstract boolean hasOpenResultSet();
    
    /**
     * Retrieve whether this statement has singleton result set.
     * 
     * @return <code>true</code> if result set has singleton result set.
     */
    public abstract boolean isSingletonResult();

    /**
     * Retrieve whether or not this statement is valid.
     *
     * @return <code>true</code> if this is a valid statement, 
     *         <code>false</code> otherwise
     */
    public abstract boolean isValid();

    /**
     * Get the number of rows contained in this statement.
     *
     * @return The rowcount for this statement
     */
    public abstract int size();

    /**
     * Remove all rows contained by this statement. This method differs from
     * {@link #clearRows} in that it effectively clears all rows from this
     * statement.
     *
     * @see #clearRows
     */
    public abstract void removeRows();

    /**
     * Register statement for the transaction. This method is used within
     * the <code>GDS.iscDsqlExecXXX</code> methods to keep a reference on
     * current transaction in which statement is executed.
     * 
     * @param trHandle instance of {@link AbstractIscTrHandle}.
     */
    public abstract void registerTransaction(AbstractIscTrHandle trHandle);
    
    /**
     * Get current transaction in which statement is currently executed.
     * 
     * @return instance of {@link AbstractIscTrHandle} or <code>null</code>
     * if statement is not assigned to a transaction.
     */
    public abstract AbstractIscTrHandle getTransaction();
    
    /**
     * Unregister statement from the transaction.
     */
    public abstract void unregisterTransaction();

    public String getExecutionPlan() {
        return executionPlan;
    }
    
    public void setExecutionPlan(String plan) {
        this.executionPlan = plan;
    }

    public int getStatementType() {
        return statementType;
    }
    
    public void setStatementType(int statementType) {
        this.statementType = statementType;
    }
    
}
