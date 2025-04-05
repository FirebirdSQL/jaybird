// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
// SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for BPB (blob parameter buffer) items.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings({ "unused", "java:S115" })
public final class BpbItems {

    public static final int isc_bpb_version1 = 1;
    public static final int isc_bpb_source_type = 1;
    public static final int isc_bpb_target_type = 2;
    public static final int isc_bpb_type = 3;
    public static final int isc_bpb_source_interp = 4;
    public static final int isc_bpb_target_interp = 5;
    public static final int isc_bpb_filter_parameter = 6;

    /**
     * Indicates that the blob cache should not be used for this blob.
     * <p>
     * This only applies to the pure Java wire protocol. This constant is not included in the request sent to
     * the server. In the native protocol, the blob cache is always checked for existence of the requested blob.
     * </p>
     *
     * @since 7
     */
    public static final int jb_bpb_bypass_local_cache = 0x1_00_00;

    private BpbItems() {
        // no instances
    }

    /**
     * Checks if {@code bpbItem} is a local, Jaybird-specific, BPB item that is not to be sent to Firebird.
     *
     * @param bpbItem
     *         bpbItem code
     * @return {@code true} if this is a known local BPB not to be sent to the server, {@code false} otherwise
     * @since 7
     */
    public static boolean isLocalBpbItem(int bpbItem) {
        return bpbItem == jb_bpb_bypass_local_cache;
    }

    /**
     * Constants for values of {@link BpbItems#isc_bpb_type}.
     * 
     * @since 5
     */
    public static final class TypeValues {

        public static final int isc_bpb_type_segmented = 0;
        public static final int isc_bpb_type_stream = 1;

        private TypeValues() {
            // no instances
        }
    }
}
