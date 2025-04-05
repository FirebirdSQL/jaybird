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

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.wire.FbWireTransaction;
import org.firebirdsql.gds.ng.wire.InlineBlob;
import org.firebirdsql.gds.ng.wire.InlineBlobCache;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version18.V18Database;

import java.sql.SQLException;
import java.util.Optional;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 19 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 5.0.8
 */
public class V19Database extends V18Database {

    private final InlineBlobCache blobCache;

    /**
     * Creates a V19Database instance.
     *
     * @param connection
     *         a WireConnection with an established connection to the server.
     * @param descriptor
     *         the ProtocolDescriptor that created this connection (this is used for creating further dependent
     *         objects).
     */
    protected V19Database(WireDatabaseConnection connection, ProtocolDescriptor descriptor) {
        super(connection, descriptor);
        blobCache = new InlineBlobCache(this);
    }

    protected void registerInlineBlob(FbWireTransaction transaction, InlineBlob inlineBlob) {
        blobCache.add(transaction, inlineBlob);
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId)
            throws SQLException {
        boolean useCache = blobParameterBuffer == null || blobParameterBuffer.isEmpty();
        if (useCache) {
            Optional<InlineBlob> cachedBlob = blobCache.getAndRemove(transaction, blobId);
            if (cachedBlob.isPresent()) {
                return cachedBlob.get();
            }
        }
        return super.createBlobForInput(transaction, blobParameterBuffer, blobId);
    }

}
