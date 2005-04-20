/*
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

import java.sql.Connection;
import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;

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
    
    /*

        // Following TPB parameters require additional API
        // for table reservation, to be done

        public final static int isc_tpb_shared                  = 3;
        public final static int isc_tpb_protected               = 4;
        public final static int isc_tpb_exclusive               = 5;
        
        public final static int isc_tpb_lock_read               = 10;
        public final static int isc_tpb_lock_write              = 11;
        

        // Following TPB parameters are not described in documentation

        public final static int isc_tpb_verb_time               = 12;
        public final static int isc_tpb_commit_time             = 13;
        
        public final static int isc_tpb_ignore_limbo            = 14;
        
        public final static int isc_tpb_autocommit              = 16;
        
        public final static int isc_tpb_restart_requests        = 19;
        
        public final static int isc_tpb_no_auto_undo            = 20;
     */
    
    /**
     * Create Blob object.
     * 
     * @return instance of {@link FirebirdBlob}.
     * 
     * @throws SQLException if something went wrong.
     */
    FirebirdBlob createBlob() throws SQLException;
    
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
     */
    void setTransactionParameters(int isolationLevel, int[] parameters)
        throws SQLException;
}