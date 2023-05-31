/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for BPB (blob parameter buffer) items.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings("unused")
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
