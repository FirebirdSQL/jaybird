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
package org.firebirdsql.jdbc;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Firebird implementation of {@link java.sql.Blob}.
 */
public class FBBlob implements FirebirdBlob, TransactionListener, Synchronizable {

    public static final boolean SEGMENTED = true;
    private static final Logger logger = LoggerFactory.getLogger(FBBlob.class);

    /**
     * The size of the buffer for blob input and output streams,
     * also used for the BufferedInputStream/BufferedOutputStream wrappers.
     */
    private final int bufferLength;
    private boolean isNew;
    private long blob_id;
    private volatile GDSHelper gdsHelper;
    private FBObjectListener.BlobListener blobListener;

    private final Collection<FBBlobInputStream> inputStreams = Collections.synchronizedSet(new HashSet<>());
    private FBBlobOutputStream blobOut = null;

    private FBBlob(GDSHelper c, boolean isNew, FBObjectListener.BlobListener blobListener) {
        gdsHelper = c;
        this.isNew = isNew;
        bufferLength = c.getBlobBufferLength();
        this.blobListener = blobListener != null ? blobListener : FBObjectListener.NoActionBlobListener.instance();
    }

    /**
     * Create new Blob instance. This constructor creates new fresh Blob, only
     * writing to the Blob is allowed.
     *
     * @param c connection that will be used to write data to blob
     * @param blobListener Blob listener instance
     */
    public FBBlob(GDSHelper c, FBObjectListener.BlobListener blobListener) {
        this(c, true, blobListener);
    }

    /**
     * Create new Blob instance. This constructor creates new fresh Blob, only
     * writing to the Blob is allowed.
     *
     * @param c connection that will be used to write data to blob.
     */
    public FBBlob(GDSHelper c) {
        this(c, null);
    }

    /**
     * Create instance of this class to access existing Blob.
     *
     * @param c connection that will be used to access Blob.
     * @param blob_id ID of the Blob.
     * @param blobListener Blob listener instance
     */
    public FBBlob(GDSHelper c, long blob_id, FBObjectListener.BlobListener blobListener) {
        this(c, false, blobListener);
        this.blob_id = blob_id;
    }

    /**
     * Create instance of this class to access existing Blob.
     *
     * @param c connection that will be used to access Blob.
     * @param blob_id ID of the Blob.
     */
    public FBBlob(GDSHelper c, long blob_id) {
        this(c, blob_id, null);
    }

    public final Object getSynchronizationObject() {
        final GDSHelper gdsHelper = this.gdsHelper;
        if (gdsHelper == null) {
            return this;
        }
        return gdsHelper.getSynchronizationObject();
    }

    @Override
    public void free() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();

                for (FBBlobInputStream blobIS : new ArrayList<>(inputStreams)) {
                    try {
                        blobIS.close();
                    } catch (IOException ex) {
                        chain.append(new FBSQLException(ex));
                    }
                }
                inputStreams.clear();

                if (blobOut != null) {
                    try {
                        blobOut.close();
                    } catch (IOException ex) {
                        chain.append(new FBSQLException(ex));
                    }
                }

                if (chain.hasException())
                    throw chain.getException();
            } finally {
                gdsHelper = null;
                blobListener = FBObjectListener.NoActionBlobListener.instance();
                blobOut = null;
            }
        }
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        throw new FBDriverNotCapableException("Method getBinaryStream(long, long) is not supported");
    }

    /**
     * Get information about this Blob. This method should be considered as
     * temporary because it provides access to low-level API. More information
     * on how to use the API can be found in "API Guide".
     *
     * @param items items in which we are interested.
     * @param buffer_length buffer where information will be stored.
     *
     * @return array of bytes containing information about this Blob.
     *
     * @throws SQLException if something went wrong.
     */
    public byte[] getInfo(byte[] items, int buffer_length) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkClosed();
            blobListener.executionStarted(this);
            try {
                // TODO Does it make sense to close blob here?
                try (FbBlob blob = gdsHelper.openBlob(blob_id, SEGMENTED)) {
                    return blob.getBlobInfo(items, buffer_length);
                }
            } finally {
                blobListener.executionCompleted(this);
            }
        }
    }

    private static final byte[] BLOB_LENGTH_REQUEST = new byte[] { ISCConstants.isc_info_blob_total_length };

    @Override
    public long length() throws SQLException {
        byte[] info = getInfo(BLOB_LENGTH_REQUEST, 20);
        return interpretLength(info, 0);
    }

    /**
     * Interpret BLOB length from buffer.
     *
     * @param info server response.
     * @param position where to start interpreting.
     *
     * @return length of the blob.
     *
     * @throws SQLException if length cannot be interpreted.
     */
    long interpretLength(byte[] info, int position) throws SQLException {
        if (info[position] != ISCConstants.isc_info_blob_total_length)
            throw new FBSQLException("Length is not available.");

        int dataLength = VaxEncoding.iscVaxInteger(info, position + 1, 2);
        return VaxEncoding.iscVaxLong(info, position + 3, dataLength);
    }

    @Override
    public boolean isSegmented() throws SQLException {
        byte[] info = getInfo(new byte[] { ISCConstants.isc_info_blob_type }, 20);

        if (info[0] != ISCConstants.isc_info_blob_type)
            throw new FBSQLException("Cannot determine BLOB type");

        int dataLength = VaxEncoding.iscVaxInteger(info, 1, 2);
        int type = VaxEncoding.iscVaxInteger(info, 3, dataLength);
        return type == ISCConstants.isc_bpb_type_segmented;
    }

    @Override
    public FirebirdBlob detach() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkClosed();
            return new FBBlob(gdsHelper, blob_id, blobListener);
        }
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (pos < 1)
            throw new FBSQLException("Blob position should be >= 1");

        if (pos > Integer.MAX_VALUE)
            throw new FBSQLException("Blob position is limited to 2^31 - 1 " +
                    "due to isc_seek_blob limitations.",
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);

        synchronized (getSynchronizationObject()) {
            blobListener.executionStarted(this);
            try {
                FirebirdBlob.BlobInputStream in = (FirebirdBlob.BlobInputStream) getBinaryStream();
                try {
                    if (pos != 1)
                        in.seek((int) pos - 1);

                    byte[] result = new byte[length];
                    in.readFully(result);
                    return result;
                } finally {
                    in.close();
                }
            } catch (IOException ex) {
                throw new FBSQLException(ex);
            } finally {
                blobListener.executionCompleted(this);
            }
        }
    }

    @Override
    public InputStream getBinaryStream() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkClosed();
            FBBlobInputStream blobstream = new FBBlobInputStream(this);
            inputStreams.add(blobstream);
            return blobstream;
        }
    }

    @Override
    public long position(byte[] pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException("Method position(byte[], long) is not supported");
    }

    @Override
    public long position(Blob pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException("Method position(Blob, long) is not supported");
    }

    @Override
    public void truncate(long len) throws SQLException {
        throw new FBDriverNotCapableException("Method truncate(long) is not supported");
    }

    @Override
    public int setBytes(long pos, byte[] bytes) throws SQLException {
        return setBytes(pos, bytes, 0, bytes.length);
    }

    @Override
    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        synchronized (getSynchronizationObject()) {
            try (OutputStream out = setBinaryStream(pos)) {
                out.write(bytes, offset, len);
                return len;
            } catch (IOException e) {
                throw new SQLException("IOException writing bytes to blob", e);
            }
        }
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkClosed();
            blobListener.executionStarted(this);

            if (blobOut != null)
                throw new FBSQLException("OutputStream already open. Only one blob output stream can be open at a time.");

            if (pos < 1)
                throw new FBSQLException("You can't start before the beginning of the blob",
                        SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);

            if (isNew && pos > 1)
                throw new FBSQLException("Previous value was null, you must start at position 1",
                        SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);

            blobOut = new FBBlobOutputStream(this);
            if (pos > 1) {
                //copy pos bytes from input to output
                //implement this later
                throw new FBDriverNotCapableException("Offset start positions are not yet supported.");
            }

            return blobOut;
        }
    }

    /**
     * Get the identifier for this <code>Blob</code>
     *
     * @return This <code>Blob</code>'s identifier
     * @throws SQLException if a database access error occurs
     */
    public long getBlobId() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (isNew)
                throw new FBSQLException("No Blob ID is available in new Blob object.");

            return blob_id;
        }
    }

    void setBlobId(long blob_id) {
        synchronized (getSynchronizationObject()) {
            this.blob_id = blob_id;
            if (isNew && gdsHelper != null) {
                FbTransaction currentTransaction = gdsHelper.getCurrentTransaction();
                if (currentTransaction != null) {
                    currentTransaction.addWeakTransactionListener(this);
                }
            }
            isNew = false;
        }
    }

    public void copyBytes(byte[] bytes, int pos, int len) throws SQLException {
        synchronized (getSynchronizationObject()) {
            try (OutputStream out = setBinaryStream(1)) {
                out.write(bytes, pos, len);
            } catch (IOException ex) {
                throw new FBSQLException(ex);
            }
        }
    }

    /**
     * The size of the buffer for blob input and output streams,
     * also used for the BufferedInputStream/BufferedOutputStream wrappers.
     *
     * @return The buffer length
     */
    int getBufferLength() {
        return bufferLength;
    }

    /**
     * Notifies this blob that <code>stream</code> has been closed.
     *
     * @param stream
     *         InputStream that has been closed.
     */
    void notifyClosed(FBBlobInputStream stream) {
        inputStreams.remove(stream);
    }

    /**
     * @return <code>true</code> when this is an uninitialized output blob, <code>false</code> otherwise.
     */
    boolean isNew() {
        synchronized (getSynchronizationObject()) {
            return isNew;
        }
    }

    public GDSHelper getGdsHelper() {
        return gdsHelper;
    }

    /**
     * Copy the contents of an {@code InputStream} into this Blob.
     * <p>
     * Calling with length {@code -1} is equivalent to calling {@link #copyStream(InputStream)}, and will copy
     * the whole stream.
     * </p>
     *
     * @param inputStream the stream from which data will be copied
     * @param length The maximum number of bytes to read from the InputStream, {@code -1} to read whole stream
     * @throws SQLException if a database access error occurs
     */
    public void copyStream(InputStream inputStream, long length) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (length == -1L) {
                copyStream(inputStream);
                return;
            }
            try (OutputStream os = setBinaryStream(1)) {
                final byte[] buffer = new byte[(int) Math.min(bufferLength, length)];
                int chunk;
                while (length > 0
                        && (chunk = inputStream.read(buffer, 0, (int) Math.min(buffer.length, length))) != -1) {
                    os.write(buffer, 0, chunk);
                    length -= chunk;
                }
            } catch (IOException ioe) {
                throw new SQLException(ioe);
            }
        }
    }

    /**
     * Copy the contents of an <code>InputStream</code> into this Blob. Unlike
     * the {@link #copyStream(InputStream, long)} method, this one copies bytes
     * until the EOF is reached.
     *
     * @param inputStream the stream from which data will be copied
     * @throws SQLException if a database access error occurs
     */
    public void copyStream(InputStream inputStream) throws SQLException {
        synchronized (getSynchronizationObject()) {
            try (OutputStream os = setBinaryStream(1)) {
                final byte[] buffer = new byte[bufferLength];
                int chunk;
                while ((chunk = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, chunk);
                }
            } catch (IOException ioe) {
                throw new SQLException(ioe);
            }
        }
    }

    /**
     * Copy data from a character stream into this Blob.
     * <p>
     * Calling with length {@code -1} is equivalent to calling {@link #copyCharacterStream(Reader, Encoding)}.
     * </p>
     *
     * @param reader the source of data to copy
     * @param length The maximum number of bytes to copy, or {@code -1} to read the whole stream
     * @param encoding The encoding used in the character stream
     */
    public void copyCharacterStream(Reader reader, long length, Encoding encoding) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (length == -1L) {
                copyCharacterStream(reader, encoding);
                return;
            }
            try (OutputStream os = setBinaryStream(1);
                 Writer osw = encoding.createWriter(os)) {

                final char[] buffer = new char[(int) Math.min(bufferLength, length)];
                int chunk;
                while (length > 0 && (chunk = reader.read(buffer, 0, (int) Math.min(buffer.length, length))) != -1) {
                    osw.write(buffer, 0, chunk);
                    length -= chunk;
                }
            } catch (UnsupportedEncodingException ex) {
                throw new SQLException("Cannot set character stream because " +
                        "the encoding '" + encoding + "' is unsupported in the JVM. " +
                        "Please report this to the driver developers."
                );
            } catch (IOException ioe) {
                throw new SQLException(ioe);
            }
        }
    }

    /**
     * Copy data from a character stream into this Blob. Unlike
     * the {@link #copyCharacterStream(Reader, long, Encoding)} )} method, this one copies bytes
     * until the EOF is reached.
     *
     * @param reader the source of data to copy
     * @param encoding The encoding used in the character stream
     */
    public void copyCharacterStream(Reader reader, Encoding encoding) throws SQLException {
        synchronized (getSynchronizationObject()) {
            try (OutputStream os = setBinaryStream(1);
                 Writer osw = encoding.createWriter(os)) {
                final char[] buffer = new char[bufferLength];
                int chunk;
                while ((chunk = reader.read(buffer, 0, buffer.length)) != -1) {
                    osw.write(buffer, 0, chunk);
                }
            } catch (UnsupportedEncodingException ex) {
                throw new SQLException("Cannot set character stream because " +
                        "the encoding '" + encoding + "' is unsupported in the JVM. " +
                        "Please report this to the driver developers."
                );
            } catch (IOException ioe) {
                throw new SQLException(ioe);
            }
        }
    }

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        switch (newState) {
        case COMMITTED:
        case ROLLED_BACK:
            synchronized (getSynchronizationObject()) {
                try {
                    free();
                } catch (SQLException e) {
                    logger.error("Error calling free on blob during transaction end", e);
                }
            }
            break;
        default:
            // Do nothing
            break;
        }
    }

    private void checkClosed() throws SQLException {
        if (gdsHelper == null) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_blobClosed).toSQLException();
        }
    }
}
