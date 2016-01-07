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
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.wire.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobGetSegmentNegative;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10InputBlob extends AbstractFbWireInputBlob implements FbWireBlob, DatabaseListener {

    // TODO V10OutputBlob and V10InputBlob share some common behavior and information (eg in open() and getMaximumSegmentSize()), find a way to unify this

    public V10InputBlob(FbWireDatabase database, FbWireTransaction transaction,
                        BlobParameterBuffer blobParameterBuffer, long blobId) {
        super(database, transaction, blobParameterBuffer, blobId);
    }

    // TODO Need blob specific warning callback?

    @Override
    public void open() throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobClosed();

                final FbWireDatabase database = getDatabase();
                synchronized (database.getSynchronizationObject()) {
                    try {
                        final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                        final BlobParameterBuffer blobParameterBuffer = getBlobParameterBuffer();
                        if (blobParameterBuffer == null) {
                            xdrOut.writeInt(op_open_blob);
                        } else {
                            xdrOut.writeInt(op_open_blob2);
                            xdrOut.writeTyped(blobParameterBuffer);
                        }
                        xdrOut.writeInt(getTransaction().getHandle());
                        xdrOut.writeLong(getBlobId());
                        xdrOut.flush();
                    } catch (IOException e) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                    }
                    try {
                        final GenericResponse genericResponse = database.readGenericResponse(null);
                        setHandle(genericResponse.getObjectHandle());
                        setOpen(true);
                        resetEof();
                    } catch (IOException e) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                    }
                    // TODO Request information on the blob?
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public byte[] getSegment(final int sizeRequested) throws SQLException {
        try {
            if (sizeRequested <= 0) {
                // TODO make non transient?
                throw new FbExceptionBuilder().exception(jb_blobGetSegmentNegative)
                        .messageParameter(sizeRequested)
                        .toSQLException();
            }
            // TODO Is this actually a real limitation, or are larger sizes possible?
            int actualSize = 2 + Math.min(sizeRequested, getMaximumSegmentSize());
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();
                checkBlobOpen();

                final GenericResponse response;
                final FbWireDatabase database = getDatabase();
                synchronized (database.getSynchronizationObject()) {
                    try {
                        final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                        xdrOut.writeInt(op_get_segment);
                        xdrOut.writeInt(getHandle());
                        xdrOut.writeInt(actualSize);
                        xdrOut.writeInt(0); // length of segment send buffer (always 0 in get)
                        xdrOut.flush();
                    } catch (IOException e) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                    }
                    try {
                        response = database.readGenericResponse(null);
                        // TODO Meaning of 2
                        if (response.getObjectHandle() == 2) {
                            // TODO what if I seek on a stream blob?
                            setEof();
                        }
                    } catch (IOException e) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                    }
                }

                final byte[] responseBuffer = response.getData();
                if (responseBuffer.length == 0) {
                    return responseBuffer;
                }

                final ByteArrayOutputStream bos = new ByteArrayOutputStream(actualSize);
                int position = 0;
                while (position < responseBuffer.length) {
                    final int segmentLength = iscVaxInteger2(responseBuffer, position);
                    position += 2;
                    bos.write(responseBuffer, position, segmentLength);
                    position += segmentLength;
                }
                return bos.toByteArray();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void seek(int offset, SeekMode seekMode) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkDatabaseAttached();
                checkTransactionActive();

                final FbWireDatabase database = getDatabase();
                synchronized (database.getSynchronizationObject()) {
                    try {
                        final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                        xdrOut.writeInt(op_seek_blob);
                        xdrOut.writeInt(getHandle());
                        xdrOut.writeInt(seekMode.getSeekModeId());
                        xdrOut.writeInt(offset);
                        xdrOut.flush();
                    } catch (IOException e) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                    }
                    try {
                        database.readResponse(null);
                        // object handle in response is the current position in the blob (see .NET provider source)
                    } catch (IOException e) {
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                    }
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }
}
