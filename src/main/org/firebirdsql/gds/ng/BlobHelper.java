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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;

/**
 * Class with static helper methods for use with blobs.
 *
 * @author Mark Rotteveel
 * @since 6.0.2
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
