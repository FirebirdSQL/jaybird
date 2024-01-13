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
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbWireInputBlob extends AbstractFbWireBlob {

    private final long blobId;

    protected AbstractFbWireInputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer, long blobId) {
        super(database, transaction, blobParameterBuffer);
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
        throw writeNotSupported();
    }

    @Override
    public final void put(byte[] b, int off, int len) throws SQLException {
        throw writeNotSupported();
    }

    private SQLException writeNotSupported() {
        SQLException e = FbExceptionBuilder.forNonTransientException(ISCConstants.isc_segstr_no_write).toSQLException();
        exceptionListenerDispatcher.errorOccurred(e);
        return e;
    }

}
