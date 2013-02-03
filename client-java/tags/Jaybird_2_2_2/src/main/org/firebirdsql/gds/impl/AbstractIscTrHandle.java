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

import org.firebirdsql.gds.IscTrHandle;


/**
 * Abstract implementation of the {@link org.firebirdsql.gds.IscTrHandle} 
 * interface.
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

    /**
     * Unregister a statement from the transaction in which it was registered.
     *
     * @param fbStatement Handle to the statement to be unregistered.
     */
    public abstract void unregisterStatementFromTransaction(AbstractIscStmtHandle fbStatement);
}
