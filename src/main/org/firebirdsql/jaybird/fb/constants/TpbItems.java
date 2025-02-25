// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
// SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for TPB (transaction parameter buffer) items.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings({ "unused", "java:S115" })
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
