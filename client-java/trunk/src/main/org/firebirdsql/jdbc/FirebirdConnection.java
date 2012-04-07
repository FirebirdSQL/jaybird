/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver, public Firebird-specific 
 * JDBC extensions.
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
package org.firebirdsql.jdbc;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * Extension of {@link Connection} interface providing access to Firebird
 * specific features. 
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdConnection extends Connection {
    
    int TPB_READ_COMMITTED = ISCConstants.isc_tpb_read_committed;
    int TPB_CONCURRENCY = ISCConstants.isc_tpb_concurrency;
    int TPB_CONSISTENCY = ISCConstants.isc_tpb_consistency;
    
    int TPB_READ = ISCConstants.isc_tpb_read;
    int TPB_WRITE = ISCConstants.isc_tpb_write;
    
    int TPB_WAIT = ISCConstants.isc_tpb_wait;
    int TPB_NOWAIT = ISCConstants.isc_tpb_nowait;
    
    int TPB_REC_VERSION = ISCConstants.isc_tpb_rec_version;
    int TPB_NO_REC_VERSION = ISCConstants.isc_tpb_no_rec_version;
    
    /**
     * Create Blob object.
     * 
     * @return instance of {@link FirebirdBlob}.
     * 
     * @throws SQLException if something went wrong.
     */
    Blob createBlob() throws SQLException;
    
    /**
     * Get current ISC encoding.
     * 
     * @return current ISC encoding.
     */
    String getIscEncoding() throws SQLException;
    
    /**
     * Set transaction parameters for the specified isolation level. They will 
     * take effect only on the newly started transaction.
     * 
     * @param isolationLevel JDBC isolation level.
     * @param parameters array of TPB parameters, see all TPB_* constants.
     * 
     * @throws SQLException if specified transaction parameters cannot be set.
     * 
     * @deprecated use {@link #setTransactionParameters(int, TransactionParameterBuffer)}
     * instead.
     */
    void setTransactionParameters(int isolationLevel, int[] parameters)
        throws SQLException;
    
    
    /**
     * Get transaction parameters for the specified transaction isolation level.
     * 
     * @param isolationLevel isolation level defined in the {@link Connection}
     * interface.
     * 
     * @return instance of {@link TransactionParameterBuffer} containing current
     * transaction parameters.
     * 
     * @throws SQLException if error occured obtaining transaction parameters.
     */
    TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException;
    
    /**
     * Create new instance of {@link TransactionParameterBuffer}.
     * 
     * @return empty instance of {@link TransactionParameterBuffer}.
     * 
     * @throws SQLException if error occured during this operation.
     */
    TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException;
    
    /**
     * Set transaction parameters for the specified transaction isolation level.
     * This method replaces the default TPB mapping with the specified one, 
     * changes will be effective from the next transaction start.
     * 
     * @param isolationLevel isolation level defined in the {@link Connection}
     * interface.
     * 
     * @param tpb instance of {@link TransactionParameterBuffer} with parameters
     * to set.
     * 
     * @throws SQLException if error occured during this operation.
     */
    void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb)
        throws SQLException;
    
    /**
     * Set transaction parameters for the next transactions. This method does
     * not change the TPB mapping, but replaces the mapping for the current
     * transaction isolation until {@link Connection#setTransactionIsolation(int)}
     * is called.
     * <p>
     * Method cannot be called when transaction has already started. 
     * 
     * @param tpb instance of {@link TransactionParameterBuffer} with new
     * transaction parameters.
     * 
     * @throws SQLException if method is called within a transaction.
     */
    void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException;

    
    /**
     * Creates an unnamed savepoint in the current transaction and 
     * returns the new {@link FirebirdSavepoint} object that represents it.
     * <p>
     * This method corresponds to the <code>Connection.setSavepoint()</code>
     * method in JDBC 3.0.
     *
     * @return instance of {@link FirebirdSavepoint}
     * @throws SQLException if a an error occured.
     * @deprecated This method will be removed in Jaybird 2.3, use {@link java.sql.Connection#setSavepoint()}
     */
    @Deprecated
    FirebirdSavepoint setFirebirdSavepoint() throws SQLException;

    /**
     * Creates a named savepoint in the current transaction and 
     * returns the new {@link FirebirdSavepoint} object that represents it.
     * <p>
     * This method corresponds to the <code>Connection.setSavepoint(String)</code>
     * method in JDBC 3.0.
     *
     * @param name a <code>String</code> containing the name of the savepoint
     * 
     * @return instance of {@link FirebirdSavepoint}
     * @throws SQLException if a an error occured.
     * @deprecated This method will be removed in Jaybird 2.3, use {@link java.sql.Connection#setSavepoint(String)}
     */
    @Deprecated
    FirebirdSavepoint setFirebirdSavepoint(String name) throws SQLException;

    /**
     * Undoes all changes made after the given {@link FirebirdSavepoint} object
     * was set.
     * <p>
     * This method corresponds to the <code>Connection.rollback(Savepoint)</code>
     * method in JDBC 3.0.
     *  
     * @param savepoint the {@link FirebirdSavepoint} object to roll back to
     * 
     * @exception SQLException if a database access error occurs.
     * @deprecated This method will be removed in Jaybird 2.3, use {@link java.sql.Connection#rollback(java.sql.Savepoint)}
     */
    @Deprecated
    void rollback(FirebirdSavepoint savepoint) throws SQLException;

    /**
     * Removes the given {@link FirebirdSavepoint} object from the current 
     * transaction. Any reference to the savepoint after it have been removed 
     * will cause an <code>SQLException</code> to be thrown.
     * <p>
     * This method corresponds to the <code>Connection.releaseSavepoint(Savepoint)</code>
     * method in JDBC 3.0.
     *
     * @param savepoint the <code>FirebirdSavepoint</code> object to be removed
     * @exception SQLException if a database access error occurs
     * @deprecated This method will be removed in Jaybird 2.3, use {@link java.sql.Connection#releaseSavepoint(java.sql.Savepoint)}
     */
    @Deprecated
    void releaseSavepoint(FirebirdSavepoint savepoint) throws SQLException;
}