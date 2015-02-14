/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * Blob information processor for retrieving blob length.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class BlobLengthProcessor implements InfoProcessor<Long> {

    private static final byte[] BLOB_LENGTH_ITEMS = new byte[] {
            ISCConstants.isc_info_blob_total_length
    };

    private final FbBlob blob;

    public BlobLengthProcessor(FbBlob blob) {
        this.blob = blob;
    }

    @Override
    public Long process(byte[] infoResponse) throws SQLException {
        if (infoResponse.length == 0 || infoResponse[0] != ISCConstants.isc_info_blob_total_length)
            throw new FbExceptionBuilder().exception(ISCConstants.isc_req_sync).toSQLException();

        int dataLength = iscVaxInteger2(infoResponse, 1);
        return iscVaxLong(infoResponse, 3, dataLength);
    }

    public byte[] getBlobLengthItems() {
        return BLOB_LENGTH_ITEMS.clone();
    }
}
