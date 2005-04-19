package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.IscStmtHandle;


/**
 * 
 */
public abstract class AbstractIscStmtHandle implements IscStmtHandle {

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

}
