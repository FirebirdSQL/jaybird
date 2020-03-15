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

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.transaction.xa.Xid;


/**
 * Extention of the {@link javax.resource.spi.LocalTransaction} interface
 * to tell whether the underlying managed connection is currently participating
 * in some transaction or not and to obtain the associated Xid.
 */
public interface FirebirdLocalTransaction extends LocalTransaction {

    /**
     * Check if managed connection is currently participating in transaction.
     * 
     * @return <code>true</code> if managed connection is participating in
     * transaction.
     * 
     * @throws ResourceException if operation cannot be completed.
     */
    boolean inTransaction() throws ResourceException;
    
    /**
     * Get the associated Xid.
     * 
     * @return instance of {@link Xid} representing a transaction ID that is
     * managed by this local transaction.
     */
    Xid getXid();
}
