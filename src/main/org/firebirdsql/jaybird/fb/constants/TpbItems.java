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
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for TPB (transaction parameter buffer) items.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings("unused")
public class TpbItems {

    public static final int isc_tpb_consistency = 1;
    public static final int isc_tpb_concurrency = 2;
    public static final int isc_tpb_shared = 3;
    public static final int isc_tpb_protected = 4;
    public static final int isc_tpb_exclusive = 5;
    public static final int isc_tpb_wait = 6;
    public static final int isc_tpb_nowait = 7;
    public static final int isc_tpb_read = 8;
    public static final int isc_tpb_write = 9;
    public static final int isc_tpb_lock_read = 10;
    public static final int isc_tpb_lock_write = 11;
    public static final int isc_tpb_verb_time = 12;
    public static final int isc_tpb_commit_time = 13;
    public static final int isc_tpb_ignore_limbo = 14;
    public static final int isc_tpb_read_committed = 15;
    public static final int isc_tpb_autocommit = 16;
    public static final int isc_tpb_rec_version = 17;
    public static final int isc_tpb_no_rec_version = 18;
    public static final int isc_tpb_restart_requests = 19;
    public static final int isc_tpb_no_auto_undo = 20;
    public static final int isc_tpb_lock_timeout = 21;
    public static final int isc_tpb_read_consistency = 22;
    public static final int isc_tpb_at_snapshot_number = 23;

    private TpbItems() {
        // no instances
    }

}
