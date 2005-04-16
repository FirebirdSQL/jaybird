package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.IscTrHandle;


/**
 * 
 */
public abstract class AbstractIscTrHandle implements IscTrHandle {

    public final static int NOTRANSACTION = 0;
    public final static int TRANSACTIONCOMMITTING = 5;
    public final static int TRANSACTIONPREPARED = 4;
    public final static int TRANSACTIONPREPARING = 3;
    public final static int TRANSACTIONROLLINGBACK = 6;
    public final static int TRANSACTIONSTARTED = 2;
    public final static int TRANSACTIONSTARTING = 1;

    /**
     * Clear all the saved result sets from this handle.
     */
    public abstract void forgetResultSets();

    /**
     * Get the current state of the transaction to which this handle is
     * pointing. The state is equal to one of the <code>TRANSACTION*</code> 
     * constants of this interface, or the <code>NOTRANSACTION</code> constant,
     * also of this interface.
     *
     * @return The corresponding value for the current state
     */
    public abstract int getState();

    /**
     * Register a statement within the transaction to which this handle points.
     * This method allows automated cleanup of the rows fetched within a 
     * transaction on commit or rollback point.
     *
     * @param fbStatement Handle to the statement to be registered.
     */
    public abstract void registerStatementWithTransaction(AbstractIscStmtHandle fbStatement);


}
