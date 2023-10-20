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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;

import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireOutputBlob extends AbstractFbWireBlob {

    private long blobId;

    protected AbstractFbWireOutputBlob(FbWireDatabase database, FbWireTransaction transaction,
                                       BlobParameterBuffer blobParameterBuffer) {
        super(database, transaction, blobParameterBuffer);
    }

    @Override
    public final long getBlobId() {
        return blobId;
    }

    /**
     * Sets the blob id.
     *
     * @param blobId
     *         Blob id.
     * @throws SQLException
     *         If this is an input blob, or if this is an output blob whose blobId was already set.
     */
    protected final void setBlobId(long blobId) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (getBlobId() != FbBlob.NO_BLOB_ID) {
                throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_blobIdAlreadySet)
                        .toSQLException();
            }
            this.blobId = blobId;
        }
    }

    @Override
    public final boolean isOutput() {
        return true;
    }

    @Override
    public final byte[] getSegment(int sizeRequested) throws SQLException {
        readNotSupported();
        return null;
    }

    private void readNotSupported() throws SQLException {
        SQLException e = FbExceptionBuilder.forNonTransientException(ISCConstants.isc_segstr_no_read).toSQLException();
        exceptionListenerDispatcher.errorOccurred(e);
        throw e;
    }

    @Override
    protected final int get(byte[] b, int off, int len, int minLen) throws SQLException {
        readNotSupported();
        return -1;
    }

    @Override
    public final void seek(int offset, SeekMode seekMode) throws SQLException {
        readNotSupported();
    }
}
