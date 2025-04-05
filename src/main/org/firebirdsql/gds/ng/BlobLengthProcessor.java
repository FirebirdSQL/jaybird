// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * Blob information processor for retrieving blob length.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class BlobLengthProcessor implements InfoProcessor<Long> {

    private static final byte[] BLOB_LENGTH_ITEMS = new byte[] {
            ISCConstants.isc_info_blob_total_length, ISCConstants.isc_info_end
    };

    @Override
    public Long process(byte[] infoResponse) throws SQLException {
        if (infoResponse.length == 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                    .messageParameter("blob")
                    .toSQLException();
        }
        if (infoResponse[0] != ISCConstants.isc_info_blob_total_length) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unexpectedInfoResponse)
                    .messageParameter("transaction", "isc_info_blob_total_length",
                            ISCConstants.isc_info_blob_total_length, infoResponse[0])
                    .toSQLException();
        }

        int dataLength = iscVaxInteger2(infoResponse, 1);
        return iscVaxLong(infoResponse, 3, dataLength);
    }

    public byte[] getBlobLengthItems() {
        return BLOB_LENGTH_ITEMS.clone();
    }
}
