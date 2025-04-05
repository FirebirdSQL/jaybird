// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

/**
 * Class with static helper methods for use with blobs.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public final class BlobHelper {

    private BlobHelper() {
        // no instances
    }

    /**
     * Checks if the blob is open.
     *
     * @throws SQLException
     *         when the blob is closed.
     */
    public static void checkBlobOpen(FbBlob blob) throws SQLException {
        if (!blob.isOpen()) {
            // TODO Use more specific exception message?
            throw FbExceptionBuilder.toNonTransientException(ISCConstants.isc_bad_segstr_handle);
        }
    }

    /**
     * @throws SQLException
     *         when the blob is open.
     */
    public static void checkBlobClosed(FbBlob blob) throws SQLException {
        if (blob.isOpen()) {
            throw FbExceptionBuilder.toNonTransientException(ISCConstants.isc_no_segstr_close);
        }
    }

}
