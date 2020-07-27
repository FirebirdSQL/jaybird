/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jaybird.xca;

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
     * @param transactionParams instance of {@link TransactionParameterBuffer}
     * representing transaction parameters.
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
