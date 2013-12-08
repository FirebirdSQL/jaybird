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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.wire.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V10Blob extends AbstractFbWireBlob implements FbWireBlob, DatabaseListener {

    public V10Blob(FbWireDatabase database, FbWireTransaction transaction, long blobId, boolean output) {
        super(database, transaction, blobId, output);
    }

    // TODO Need blob specific warning callback?

    @Override
    public void open() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkDatabaseAttached();
            checkTransactionActive();
            if (isOpen()) {
                // TODO isc_no_segstr_close instead?
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_op).toSQLException();
            }
            final long blobId = getBlobId();
            if (isOutput() && blobId != 0) {
                // TODO Custom error instead? (eg "Attempting to reopen output blob")
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_op).toSQLException();
            }

            final int operation;
            // TODO Update for blob parameter buffer and op_create_blob2 / op_open_blob2
            if (isOutput()) {
                operation = op_create_blob;
            } else {
                operation = op_open_blob;
            }

            final FbWireDatabase database = getDatabase();
            synchronized (database.getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                    xdrOut.writeInt(operation);
                    // TODO Support blob parameter buffer
                    xdrOut.writeInt(getTransaction().getHandle());
                    xdrOut.writeLong(blobId);
                    xdrOut.flush();
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(e).toSQLException();
                }
                try {
                    final GenericResponse genericResponse = database.readGenericResponse(null);
                    setHandle(genericResponse.getObjectHandle());
                    final long receivedBlobId = genericResponse.getBlobId();
                    if (isOutput()) {
                        setBlobId(receivedBlobId);
                    } else if (receivedBlobId != blobId) {
//                        throw new SQLNonTransientException(String.format("Attempt to open blobId %s returned blobId %s",
//                                blobId, receivedBlobId));
                    }
                    setOpen(true);
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                }
                // TODO Request information on the blob?
            }
        }
    }

    @Override
    public byte[] getSegment(final int sizeRequested) throws SQLException {
        if (sizeRequested <= 0) {
            // TODO Add SQL State, make non transient?
            throw new SQLException(String.format("getSegment called with sizeRequested %d, should be > 0", sizeRequested));
        }
        // TODO Is this actually a real limitation, or are larger sizes possible?
        int actualSize = 2 + Math.min(sizeRequested, getMaximumSegmentSize());
        synchronized (getSynchronizationObject()) {
            checkDatabaseAttached();
            checkTransactionActive();
            if (isOutput()) {
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_read).toSQLException();
            }
            if (!isOpen()) {
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_bad_segstr_handle).toSQLException();
            }

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
                    GenericResponse response = database.readGenericResponse(null);
                    if (response.getObjectHandle() == 2) {
                        // TODO what if I seek on a stream blob?
                        setEof();
                    }

                    final byte[] responseBuffer = response.getData();
                    if (responseBuffer.length == 0) {
                        return responseBuffer;
                    }

                    final ByteArrayOutputStream bos = new ByteArrayOutputStream(actualSize);
                    int position = 0;
                    while (position < responseBuffer.length) {
                        int segmentLength = database.iscVaxInteger2(responseBuffer, position);
                        position += 2;
                        bos.write(responseBuffer, position, segmentLength);
                        position += segmentLength;
                    }
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(e).toSQLException();
                }
            }
        }
    }

    @Override
    public void putSegment(byte[] segment) throws SQLException {
        // TODO Handle exceeding max segment size?
        if (segment.length == 0) {
            // TODO Add SQL State, make non transient?
            throw new SQLException(String.format("putSegment called with segment length %d, should be > 0", segment.length));
        }
        synchronized (getSynchronizationObject()) {
            checkDatabaseAttached();
            checkTransactionActive();
            if (!isOutput()) {
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_write).toSQLException();
            }
            if (!isOpen()) {
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_bad_segstr_handle).toSQLException();
            }

            final FbWireDatabase database = getDatabase();
            synchronized (database.getSynchronizationObject()) {
                try {
                    final XdrOutputStream xdrOut = database.getXdrStreamAccess().getXdrOut();
                    // TODO Using op_batch_segments over op_put_segment doesn't seem to provide a real benefit in current implementation (see XdrOutputStream)
                    // TODO Is there any actual benefit possible, like sending multiple segments of maximum size?
                    xdrOut.writeInt(op_batch_segments);
                    xdrOut.writeInt(getHandle());
                    xdrOut.writeBlobBuffer(segment);
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
        }
    }

    @Override
    public int getMaximumSegmentSize() {
        // TODO Max size in FB 3 is 2^16, not 2^15 - 1, is that for all versions, or only for newer protocols?
        // Subtracting 2 because the maximum size includes length TODO: verify if true
        return Short.MAX_VALUE - 2;
    }

    @Override
    protected void releaseBlob(int releaseOperation) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkDatabaseAttached();
            checkTransactionActive();
            try {
                getDatabase().releaseObject(releaseOperation, getHandle());
            } finally {
                setOpen(false);
            }
        }
    }
}
