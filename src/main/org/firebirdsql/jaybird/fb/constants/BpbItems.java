// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
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

    private BpbItems() {
        // no instances
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
