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
    String getIscEncoding();
    
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