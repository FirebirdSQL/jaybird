/*
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireInputBlob extends AbstractFbWireBlob {

    private final long blobId;

    protected AbstractFbWireInputBlob(FbWireDatabase database, FbWireTransaction transaction,
                                      BlobParameterBuffer blobParameterBuffer, long blobId) {
        super(database, transaction, blobParameterBuffer);
        assert blobId != FbBlob.NO_BLOB_ID : "blobId must be non-zero for an input blob";
        this.blobId = blobId;
    }

    @Override
    public final long getBlobId() {
        return blobId;
    }

    @Override
    public final boolean isOutput() {
        return false;
    }

    @Override
    public final void putSegment(byte[] segment) throws SQLException {
        try {
            throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_write).toSQLException();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }
}
