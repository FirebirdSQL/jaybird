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
import org.firebirdsql.gds.JaybirdErrorCodes;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * Blob information processor for retrieving blob length.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class BlobLengthProcessor implements InfoProcessor<Long> {

    private static final byte[] BLOB_LENGTH_ITEMS = new byte[] {
            ISCConstants.isc_info_blob_total_length
    };

    @Override
    public Long process(byte[] infoResponse) throws SQLException {
        if (infoResponse.length == 0) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                    .messageParameter("blob")
                    .toSQLException();
        }
        if (infoResponse[0] != ISCConstants.isc_info_blob_total_length) {
            throw new FbExceptionBuilder().exception(JaybirdErrorCodes.jb_unexpectedInfoResponse)
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
