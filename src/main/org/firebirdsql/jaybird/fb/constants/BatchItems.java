// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
// SPDX-FileComment: The constants listed here were obtained from the Firebird sources, which are licensed under the IPL (InterBase Public License) and/or IDPL (Initial Developer Public License), both are variants of the Mozilla Public License version 1.1
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for batch items and values (batch config, blob policy and information items).
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings("unused")
public final class BatchItems {

    public static final int BATCH_VERSION_1 = 1;
    
    // Tags
    public static final int TAG_MULTIERROR = 1;
    public static final int TAG_RECORD_COUNTS = 2;
    public static final int TAG_BUFFER_BYTES_SIZE = 3;
    public static final int TAG_BLOB_POLICY = 4;
    public static final int TAG_DETAILED_ERRORS = 5;

    // Information items
    public static final int INF_BUFFER_BYTES_SIZE = 10;
    public static final int INF_DATA_BYTES_SIZE = 11;
    public static final int INF_BLOBS_BYTES_SIZE = 12;
    public static final int INF_BLOB_ALIGNMENT = 13;
    public static final int INF_BLOB_HEADER = 14;

    // Blob policy values
    public static final int BLOB_NONE = 0;
    public static final int BLOB_ID_ENGINE = 1;
    public static final int BLOB_ID_USER = 2;
    public static final int BLOB_STREAM = 3;

    public static final int BLOB_SEGHDR_ALIGN = 2;

    public static final int BATCH_EXECUTE_FAILED = -1;
    public static final int BATCH_SUCCESS_NO_INFO = -2;

    private BatchItems() {
        // no instances
    }

}
