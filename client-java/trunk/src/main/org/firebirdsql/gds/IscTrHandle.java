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
 * The interface <code>IscTrHandle</code> represents a transaction handle.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface IscTrHandle {

    // TODO Replace with enum
    public static final int NOTRANSACTION = 0;
    public static final int TRANSACTIONSTARTING = 1;
    public static final int TRANSACTIONSTARTED = 2;
    public static final int TRANSACTIONPREPARING = 3;
    public static final int TRANSACTIONPREPARED = 4;
    public static final int TRANSACTIONCOMMITTING = 5;
    public static final int TRANSACTIONROLLINGBACK = 6;

    int getTransactionId();
    
    void setTransactionId(final int rtr_id);
    
    /**
     * Retrieve a handle to the database to which this transaction is linked.
     *
     * @return Handle to the database
     */
    IscDbHandle getDbHandle();
    
    /**
     * Sets a handle to the database to which this transaction is linked.
     *
     * @return Handle to the database
     */
    void setDbHandle(IscDbHandle db);
    
    /**
     * Clears the database handle associated with this transaction
     */
    public void unsetDbHandle();
    
    /**
     * Add a warning to the connection associated with this transaction.
     * 
     * @param warning Warning to add
     */
    void addWarning(GDSException warning);
    
    /**
     * Get the current state of the transaction to which this handle is
     * pointing. The state is equal to one of the <code>TRANSACTION*</code> 
     * constants of this interface, or the <code>NOTRANSACTION</code> constant,
     * also of this interface.
     *
     * @return The corresponding value for the current state
     */
    int getState();
    
    void setState(int state);
    
    void addBlob(IscBlobHandle blob);
    
    void removeBlob(IscBlobHandle blob);
    
    /**
     * Register a statement within the transaction to which this handle points.
     * This method allows automated cleanup of the rows fetched within a 
     * transaction on commit or rollback point.
     *
     * @param stmt Handle to the statement to be registered.
     */
    void registerStatementWithTransaction(IscStmtHandle stmt);
    
    /**
     * Unregister a statement from the transaction in which it was registered.
     *
     * @param stmt Handle to the statement to be unregistered.
     */
    void unregisterStatementFromTransaction(IscStmtHandle stmt);
    
    /**
     * Clear all the saved result sets from this handle.
     */
    void forgetResultSets();
}
