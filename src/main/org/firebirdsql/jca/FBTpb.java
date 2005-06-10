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

package org.firebirdsql.jca;

import java.io.Serializable;

import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * The <code>FBTpb</code> class represents the Firebird Transaction Parameter
 * Block (TPB), which contains Firebird-specific information about transaction
 * isolation.
 * 
 * Created: Wed Jun 19 10:12:22 2002
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks </a>
 */

public class FBTpb implements Serializable {

    private TransactionParameterBuffer transactionParams;

    /**
     * Create a new Transaction Parameters Block instance based around a
     * <code>FBTpbMapper</code>.
     * 
     * @param mapper
     *            The <code>FBTpbMapper</code> to be used with this
     *            <code>FBTpb</code>
     */
    public FBTpb(TransactionParameterBuffer transactionParams) {
        this.transactionParams = transactionParams;
    }

    public boolean equals(Object other) {
        if (other == this) 
            return true; 
        
        if (!(other instanceof FBTpb)) 
            return false;
        
        return transactionParams.equals(((FBTpb) other).transactionParams);
    }

    public int hashCode() {
        return transactionParams.hashCode();
    }

//    /**
//     * Attempts to change the transaction isolation level to the one given. The
//     * constants defined in the interface <code>Connection</code> are the
//     * possible transaction isolation levels.
//     * 
//     * <P>
//     * <B>Note: </B> This method cannot be called while in the middle of a
//     * transaction.
//     * 
//     * @param level
//     *            one of the TRANSACTION_* isolation values with the exception
//     *            of TRANSACTION_NONE; some databases may not support other
//     *            values
//     * @throws SQLException
//     *             if the isolation level is invalid
//     */
//    public void setTransactionIsolation(int level) throws ResourceException {
//
//        switch (level) {
//            case Connection.TRANSACTION_SERIALIZABLE:
//            case Connection.TRANSACTION_REPEATABLE_READ:
//            case Connection.TRANSACTION_READ_COMMITTED:
//
//                transactionParams = mapper.getMapping(level);
//                txIsolation = level;
//
//                // apply read-only flag cached locally
//                setReadOnly(readOnly);
//
//                break;
//
//            // promote to the higher isolation level,
//            // because this one is not supported
//            case Connection.TRANSACTION_READ_UNCOMMITTED:
//
//                transactionParams = mapper.getMapping(Connection.TRANSACTION_READ_COMMITTED);
//                txIsolation = Connection.TRANSACTION_READ_COMMITTED;
//
//                // apply read-only flag cached locally
//                setReadOnly(readOnly);
//
//                break;
//
//            default:
//                throw new FBResourceException(
//                        "Unsupported transaction isolation level");
//        }
//    }
//
//    /**
//     * Gets this Connection's current transaction isolation level.
//     * 
//     * @return the current TRANSACTION_* mode value
//     * @exception SQLException
//     *                if a database access error occurs, should not be possible
//     *                for this to be thrown
//     */
//    public int getTransactionIsolation() throws ResourceException {
//        return txIsolation;
//    }

    /**
     * Set the read-only flag on this TPB.
     * 
     * @param readOnly
     *            If <code>true</code>, this TPB will be set to read-only,
     *            otherwise it will be be read-write
     */
    public void setReadOnly(boolean readOnly) {
        
        transactionParams.removeArgument(TransactionParameterBuffer.READ);
        transactionParams.removeArgument(TransactionParameterBuffer.WRITE);
        
        transactionParams.addArgument(readOnly ? TransactionParameterBuffer.READ
                : TransactionParameterBuffer.WRITE);
    }

    /**
     * Determine whether this TPB is set to read-only.
     * 
     * @return <code>true</code> if this TPB is read-only, otherwise false
     */
    public boolean isReadOnly() {
        return transactionParams.hasArgument(TransactionParameterBuffer.READ);
    }
    
    public TransactionParameterBuffer getTransactionParameterBuffer() {
        return transactionParams;
    }
    
    public void setTransactionParameterBuffer(TransactionParameterBuffer tpb) {
        this.transactionParams = tpb;
    }
}
