// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
// SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for standard Firebird blob subtypes.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings({ "unused", "java:S115" })
public final class StandardBlobTypes {

    // types less than zero are reserved for user-defined blob types use

    // a.k.a binary
    public static final int isc_blob_untyped = 0;
    public static final int isc_blob_text = 1;

    // internal subtypes

    public static final int isc_blob_blr = 2;
    public static final int isc_blob_acl = 3;
    public static final int isc_blob_ranges = 4;
    public static final int isc_blob_summary = 5;
    public static final int isc_blob_format = 6;
    public static final int isc_blob_tra = 7;
    public static final int isc_blob_extfile = 8;
    public static final int isc_blob_debug_info = 9;

    private StandardBlobTypes() {
        // no instances
    }

}
