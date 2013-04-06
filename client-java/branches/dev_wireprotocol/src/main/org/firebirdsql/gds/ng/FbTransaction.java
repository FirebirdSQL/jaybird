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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbTransaction {
    
    // TODO Evaluate whether this structure matches with needs for JDBC

    /**
     * @return Current transaction state
     */
    TransactionState getState();

    /**
     * Adds a {@link TransactionEventListener} to the list of listeners.
     * <p>
     * The implementation may use {@link WeakReference} for the listeners, so
     * make sure the listener remains strongly reachable for its usefull
     * lifetime.
     * </p>
     * 
     * @param listener
     *            TransactionEventListener to register
     */
    void addTransactionEventListener(TransactionEventListener listener);

    /**
     * Removes the {@link TransactionEventListener} from the list of listeners.
     * 
     * @param listener
     *            TransactionEventListener to remove
     */
    void removeTransactionEventListener(TransactionEventListener listener);

    /**
     * Begin the transaction.
     * 
     * @param tpb
     *            TransactionParameterBuffer with the transaction configuration
     * @throws FbException
     */
    void beginTransaction(TransactionParameterBuffer tpb) throws FbException;

    /**
     * Commit the transaction
     * 
     * @throws FbException
     */
    void commit() throws FbException;

    /**
     * Commit with retaining
     * 
     * @throws FbException
     */
    void commitRetaining() throws FbException;

    /**
     * Roll back the transaction
     * 
     * @throws FbException
     */
    void rollback() throws FbException;

    /**
     * Roll back with retaining
     * 
     * @throws FbException
     */
    void rollbackRetaining() throws FbException;

    // TODO Implement two-phase to match with current Jaybird implementation
    //    void prepare() throws FbException;
    //
    //    void prepare(byte[] buffer) throws FbException;
}
