/*
 * Firebird Open Source J2ee connector - jdbc driver
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

/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds;


/**
 * The interface <code>isc_tr_handle</code> represents a transaction handle.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface isc_tr_handle {

    public final static int NOTRANSACTION = 0;
    public final static int TRANSACTIONSTARTING = 1;
    public final static int TRANSACTIONSTARTED = 2;
    public final static int TRANSACTIONPREPARING = 3;
    public final static int TRANSACTIONPREPARED = 4;
    public final static int TRANSACTIONCOMMITTING = 5;
    public final static int TRANSACTIONROLLINGBACK = 6;

    /**
     * Retrieve a handle to the database to which this transaction is linked.
     *
     * @return Handle to the database
     */
    isc_db_handle getDbHandle();

    /**
     * Get the current state of the transaction to which this handle is
     * pointing. The state is equal to one of the <code>TRANSACTION*</code> 
     * constants of this interface, or the <code>NOTRANSACTION</code> constant,
     * also of this interface.
     *
     * @return The corresponding value for the current state
     */
    int getState();

    /**
     * Register a statement within the transaction to which this handle points.
     *
     * @param fbStatement Handle to the statement to be registered
     */
    void registerStatementWithTransaction(isc_stmt_handle fbStatement);

    /**
     * Clear all the saved result sets from this handle.
     */
    void forgetResultSets();
}
