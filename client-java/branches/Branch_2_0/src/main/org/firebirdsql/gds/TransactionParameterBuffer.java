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

package org.firebirdsql.gds;


/**
 * Instances of this interface represent Transaction Parameter Buffer from the
 * Firebird API.
 */
public interface TransactionParameterBuffer {
    
    int AUTOCOMMIT         = ISCConstants.isc_tpb_autocommit;

    int READ_COMMITTED      = ISCConstants.isc_tpb_read_committed;
    int REC_VERSION         = ISCConstants.isc_tpb_rec_version;
    int NO_REC_VERSION      = ISCConstants.isc_tpb_no_rec_version;

    int CONCURRENCY         = ISCConstants.isc_tpb_concurrency;
    int CONSISTENCY         = ISCConstants.isc_tpb_consistency;

    int SHARED              = ISCConstants.isc_tpb_shared;
    int PROTECTED           = ISCConstants.isc_tpb_protected;
    int EXCLUSIVE           = ISCConstants.isc_tpb_exclusive;

    int WAIT                = ISCConstants.isc_tpb_wait;
    int NOWAIT              = ISCConstants.isc_tpb_nowait;

    int READ                = ISCConstants.isc_tpb_read;
    int WRITE               = ISCConstants.isc_tpb_write;
    
    int LOCK_READ           = ISCConstants.isc_tpb_lock_read;
    int LOCK_WRITE          = ISCConstants.isc_tpb_lock_write;
    
    int VERB_TIME           = ISCConstants.isc_tpb_verb_time;
    int COMMIT_TIME         = ISCConstants.isc_tpb_commit_time;

    int IGNORE_LIMBO        = ISCConstants.isc_tpb_ignore_limbo;
    int RESTART_REQUESTS    = ISCConstants.isc_tpb_restart_requests;
    
    int NO_AUTO_UNDO        = ISCConstants.isc_tpb_no_auto_undo;

    /**
     * Add argument.
     * @param argumentType type of argument.
     */
    void addArgument(int argumentType);

    /**
     * Add string argument.
     * @param argumentType type of argument.
     * @param value string value to add.
     */
    void addArgument(int argumentType, String value);

    /**
     * Add integer argument.
     * @param argumentType type of argument.
     * @param value integer value to add.
     */
    void addArgument(int argumentType, int value);
    
    /**
     * Add array of bytes.
     * @param argumentType type of argument.
     * @param content content of argument.
     */ 
    void addArgument(int argumentType, byte[] content);

    /**
     * Remove specified argument.
     * @param argumentType type of argument to remove.
     */ 
    void removeArgument(int argumentType);

    /**
     * Get argument as string.
     * @param argumentType type of argument to find.
     * @return argument as string or <code>null</code> if nothing found.
     */ 
    String getArgumentAsString(int argumentType);
    
    /**
     * Get argument as int.
     * @param argumentType type of argument to find.
     * @return argument as string or <code>0</code> if nothing found.
     */ 
    int getArgumentAsInt(int argumentType);
    
    /**
     * Check if this parameter buffer has specified argument.
     * @param argumentType type of argument to find.
     * @return <code>true</code> if this buffer contains specified argument.
     */
    boolean hasArgument(int argumentType);

    /**
     * Make a deep copy of this object.
     * @return deep copy of this object.
     */ 
    TransactionParameterBuffer deepCopy();
}
