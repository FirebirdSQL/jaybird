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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.wire.*;

import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentEmpty;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobPutSegmentTooLong;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10OutputBlob extends AbstractFbWireOutputBlob implements FbWireBlob, DatabaseListener {

    // TODO V10OutputBlob and V10InputBlob share some common behavior and information (eg in open() and getMaximumSegmentSize()), find a way to unify this

    public V10OutputBlob(FbWireDatabase database, FbWireTransaction transaction,
            BlobParameterBuffer blobParameterBuffer) {
        super(database, transaction, blobParameterBuffer);
    }

    // TODO Need blob specific warning callback?

    @Override
    public void open() throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobClosed();

                if (getBlobId() != FbBlob.NO_BLOB_ID) {
                    throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_op).toSQLException();
                }

                final FbWireDatabase database = getDatabase();
                try {
                    final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                    final BlobParameterBuffer blobParameterBuffer = getBlobParameterBuffer();
                    if (blobParameterBuffer == null) {
                        xdrOut.writeInt(op_create_blob);
                    } else {
                        xdrOut.writeInt(op_create_blob2);
                        xdrOut.writeTyped(blobParameterBuffer);
                    }
                    xdrOut.writeInt(getTransaction().getHandle());
                    xdrOut.writeLong(FbBlob.NO_BLOB_ID);
                    xdrOut.flush();
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                }
                try {
                    final GenericResponse genericResponse = database.readGenericResponse(null);
                    setHandle(genericResponse.getObjectHandle());
                    setBlobId(genericResponse.getBlobId());
                    setOpen(true);
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                }
                // TODO Request information on the blob?
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void putSegment(byte[] segment) throws SQLException {
        try {
            if (segment.length == 0) {
                throw new FbExceptionBuilder().exception(jb_blobPutSegmentEmpty).toSQLException();
            }
            // TODO Handle by performing multiple puts?
            if (segment.length > getMaximumSegmentSize()) {
                throw new FbExceptionBuilder().exception(jb_blobPutSegmentTooLong).toSQLException();
            }
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobOpen();

                final FbWireDatabase database = getDatabase();
                try {
                    final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                    xdrOut.writeInt(op_put_segment);
                    xdrOut.writeInt(getHandle());
                    xdrOut.writeInt(segment.length);
                    xdrOut.writeBuffer(segment);
                    xdrOut.flush();
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                }
                try {
                    database.readResponse(null);
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }
}
