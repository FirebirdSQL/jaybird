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

import org.firebirdsql.jaybird.fb.constants.TpbItems;

/**
 * Instances of this interface represent Transaction Parameter Buffer from the
 * Firebird API.
 */
public interface TransactionParameterBuffer extends ParameterBuffer {

    //@formatter:off
    @Deprecated
    int AUTOCOMMIT          = TpbItems.isc_tpb_autocommit;

    @Deprecated
    int READ_COMMITTED      = TpbItems.isc_tpb_read_committed;
    @Deprecated
    int REC_VERSION         = TpbItems.isc_tpb_rec_version;
    @Deprecated
    int NO_REC_VERSION      = TpbItems.isc_tpb_no_rec_version;

    @Deprecated
    int CONCURRENCY         = TpbItems.isc_tpb_concurrency;
    @Deprecated
    int CONSISTENCY         = TpbItems.isc_tpb_consistency;

    @Deprecated
    int SHARED              = TpbItems.isc_tpb_shared;
    @Deprecated
    int PROTECTED           = TpbItems.isc_tpb_protected;
    @Deprecated
    int EXCLUSIVE           = TpbItems.isc_tpb_exclusive;

    @Deprecated
    int WAIT                = TpbItems.isc_tpb_wait;
    @Deprecated
    int NOWAIT              = TpbItems.isc_tpb_nowait;

    @Deprecated
    int READ                = TpbItems.isc_tpb_read;
    @Deprecated
    int WRITE               = TpbItems.isc_tpb_write;

    @Deprecated
    int LOCK_READ           = TpbItems.isc_tpb_lock_read;
    @Deprecated
    int LOCK_WRITE          = TpbItems.isc_tpb_lock_write;

    @Deprecated
    int VERB_TIME           = TpbItems.isc_tpb_verb_time;
    @Deprecated
    int COMMIT_TIME         = TpbItems.isc_tpb_commit_time;

    @Deprecated
    int IGNORE_LIMBO        = TpbItems.isc_tpb_ignore_limbo;
    @Deprecated
    int RESTART_REQUESTS    = TpbItems.isc_tpb_restart_requests;

    @Deprecated
    int NO_AUTO_UNDO        = TpbItems.isc_tpb_no_auto_undo;

    @Deprecated
    int LOCK_TIMEOUT        = TpbItems.isc_tpb_lock_timeout;
    //@formatter:on

    /**
     * Make a deep copy of this object.
     *
     * @return deep copy of this object.
     */
    TransactionParameterBuffer deepCopy();
}
