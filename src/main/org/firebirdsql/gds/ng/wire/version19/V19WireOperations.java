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
package org.firebirdsql.gds.ng.wire.version19;

import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.InlineBlobResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version16.V16WireOperations;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_invalidStringLength;

/**
 * @author Mark Rotteveel
 * @since 5.0.8
 */
public class V19WireOperations extends V16WireOperations {

    public V19WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    protected InlineBlobResponse readInlineBlobResponse(XdrInputStream xdrIn) throws SQLException, IOException {
        final int transactionHandle = xdrIn.readInt(); // p_tran_id
        final long blobId = xdrIn.readLong(); // p_blob_id
        final byte[] info = xdrIn.readBuffer(); // p_blob_info
        final int dataLength;

        ClumpletReader clumpletReader = new ClumpletReader(ClumpletReader.Kind.InfoResponse, info);
        if (clumpletReader.find(ISCConstants.isc_info_blob_total_length)) {
            dataLength = clumpletReader.getInt();
        } else {
            // Something is wrong, read and throw away blob data buffer before throwing exception
            xdrIn.readBuffer(); // p_blob_data
            throw new SQLException(
                    "Expected to be able to find isc_info_blob_total_length; this is probably an implementation bug",
                    "0F000");
        }

        final byte[] data = new byte[dataLength];
        // Reading p_blob_data and decoding segments directly from stream, instead of using xdrIn.readBuffer() and then
        // decoding segments from byte array
        final int bufferLength = xdrIn.readInt();
        int remainingBuffer = bufferLength;
        int dataPos = 0;
        while (remainingBuffer > 0) {
            int segmentLength = VaxEncoding.decodeVaxInteger2WithoutLength(xdrIn);
            remainingBuffer -= 2;
            if (segmentLength > remainingBuffer) {
                // Something is wrong, read and throw away remaining buffer before throwing exception
                xdrIn.skipFully(remainingBuffer);
                xdrIn.skipPadding(bufferLength);
                throw FbExceptionBuilder.forException(jb_invalidStringLength)
                        .messageParameter("segmentLength", remainingBuffer, segmentLength)
                        .toSQLException();
            }
            xdrIn.readFully(data, dataPos, segmentLength);
            dataPos += segmentLength;
            remainingBuffer -= segmentLength;
        }
        xdrIn.skipPadding(bufferLength);
        return new InlineBlobResponse(transactionHandle, blobId, info, data);
    }

}
