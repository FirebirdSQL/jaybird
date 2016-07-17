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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Firebird implementation of {@link java.sql.Blob}.
 */
public class FBBlob implements FirebirdBlob, Synchronizable {

    public static final boolean SEGMENTED = true;

    /**
     * The size of the buffer for blob input and output streams,
     * also used for the BufferedInputStream/BufferedOutputStream wrappers.
     */
    private final int bufferLength;
    private boolean isNew;
    private long blob_id;
    private final GDSHelper gdsHelper;
    private final FBObjectListener.BlobListener blobListener;

    private final Collection<FBBlobInputStream> inputStreams = Collections.synchronizedSet(new HashSet<FBBlobInputStream>());
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
        return gdsHelper.getSynchronizationObject();
    }

    /**
     * This method frees the <code>Blob</code> object and releases the resources that
     * it holds. The object is invalid once the <code>free</code>
     * method is called.
     *<p>
     * After <code>free</code> has been called, any attempt to invoke a
     * method other than <code>free</code> will result in a <code>SQLException</code>
     * being thrown.  If <code>free</code> is called multiple times, the subsequent
     * calls to <code>free</code> are treated as a no-op.
     *<p>
     *
     * @throws SQLException if an error occurs releasing
     * the Blob's resources
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since 1.6
     */
    public void free() throws SQLException {
        synchronized (getSynchronizationObject()) {
            SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();

            for (FBBlobInputStream blobIS : new ArrayList<>(inputStreams)) {
                try {
                    blobIS.close();
                } catch (IOException ex) {
                    chain.append(new FBSQLException(ex));
                }
            }
            inputStreams.clear();

            if (chain.hasException())
                throw chain.getException();
        }
    }

    /**
     * Returns an <code>InputStream</code> object that contains a partial <code>Blob</code> value,
     * starting  with the byte specified by pos, which is length bytes in length.
     *
     * @param pos the offset to the first byte of the partial value to be retrieved.
     *  The first byte in the <code>Blob</code> is at position 1
     * @param length the length in bytes of the partial value to be retrieved
     * @return <code>InputStream</code> through which the partial <code>Blob</code> value can be read.
     * @throws SQLException if pos is less than 1 or if pos is greater than the number of bytes
     * in the <code>Blob</code> or if pos + length is greater than the number of bytes
     * in the <code>Blob</code>
     *
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since 1.6
     */
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        throw new FBDriverNotCapableException();
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
            blobListener.executionStarted(this);
            try {
                FbBlob blob = gdsHelper.openBlob(blob_id, SEGMENTED);
                try {
                    return blob.getBlobInfo(items, buffer_length);
                } finally {
                    // TODO Does it make sense to close blob here?
                    blob.close();
                }
            } finally {
                blobListener.executionCompleted(this);
            }
        }
    }

    public static final byte[] BLOB_LENGTH_REQUEST = new byte[] { ISCConstants.isc_info_blob_total_length };

    /**
     * Returns the number of bytes in the <code>BLOB</code> value
     * designated by this <code>Blob</code> object.
     * @return length of the <code>BLOB</code> in bytes
     * @exception SQLException if there is an error accessing the
     * length of the <code>BLOB</code>
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @since 1.2
     */
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

    /**
     * Check if blob is segmented.
     *
     * @return <code>true</code> if this blob is segmented,
     * otherwise <code>false</code>
     *
     * @throws SQLException if something went wrong.
     */
    public boolean isSegmented() throws SQLException {
        byte[] info = getInfo(new byte[] { ISCConstants.isc_info_blob_type }, 20);

        if (info[0] != ISCConstants.isc_info_blob_type)
            throw new FBSQLException("Cannot determine BLOB type");

        int dataLength = VaxEncoding.iscVaxInteger(info, 1, 2);
        int type = VaxEncoding.iscVaxInteger(info, 3, dataLength);
        return type == ISCConstants.isc_bpb_type_segmented;
    }

    /**
     * Detach this blob. This method creates new instance of the same blob
     * database object that is not under result set control. When result set
     * is closed, all associated resources are also released, including open
     * blob streams. This method creates an new instance of blob object with
     * the same blob ID that can be used even when result set is closed.
     * <p>
     * Note, detached blob will not remember the stream position of this object.
     * This means that you cannot start reading data from the blob, then detach
     * it, and then continue reading. Reading from detached blob will begin at
     * the blob start.
     *
     * @return instance of {@link FBBlob} that is not under result set control.
     *
     * @throws SQLException if Blob cannot be detached.
     */
    public FirebirdBlob detach() throws SQLException {
        return new FBBlob(gdsHelper, blob_id, blobListener);
    }

    /**
     * Retrieves all or part of the <code>BLOB</code>
     * value that this <code>Blob</code> object represents, as an array of
     * bytes.  This <code>byte</code> array contains up to <code>length</code>
     * consecutive bytes starting at position <code>pos</code>.
     *
     * @param pos the ordinal position of the first byte in the
     *        <code>BLOB</code> value to be extracted; the first byte is at
     *        position 1
     * @param length the number of consecutive bytes to be copied; the value
     * for length must be 0 or greater
     * @return a byte array containing up to <code>length</code>
     *         consecutive bytes from the <code>BLOB</code> value designated
     *         by this <code>Blob</code> object, starting with the
     *         byte at position <code>pos</code>
     * @exception SQLException if there is an error accessing the
     *            <code>BLOB</code> value; if pos is less than 1 or length is
     * less than 0
     * @exception SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     * @see #setBytes
     * @since 1.2
     */
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

    public InputStream getBinaryStream() throws SQLException {
        synchronized (getSynchronizationObject()) {
            FBBlobInputStream blobstream = new FBBlobInputStream(this);
            inputStreams.add(blobstream);
            return blobstream;
        }
    }

    public long position(byte pattern[], long start) throws SQLException {
        throw new FBDriverNotCapableException("Method position(byte[], long) is not supported");
    }

    public long position(Blob pattern, long start) throws SQLException {
        throw new FBDriverNotCapableException("Method position(Blob, long) is not supported");
    }

    public void truncate(long len) throws SQLException {
        throw new FBDriverNotCapableException("Method truncate(long) is not supported");
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        return setBytes(pos, bytes, 0, bytes.length);
    }

    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
        throw new FBDriverNotCapableException("Method setBytes(long, byte[], int, int) is not supported");
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
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

    //package methods

    /**
     * Get the identifier for this <code>Blob</code>
     *
     * @return This <code>Blob</code>'s identifier
     * @throws SQLException if a database access error occurs
     */
    public long getBlobId() throws SQLException {
        if (isNew)
            throw new FBSQLException("No Blob ID is available in new Blob object.");

        return blob_id;
    }

    void setBlobId(long blob_id) {
        this.blob_id = blob_id;
        this.isNew = false;
    }

    public void copyBytes(byte[] bytes, int pos, int len) throws SQLException {
        try (OutputStream out = setBinaryStream(1)) {
            out.write(bytes, pos, len);
        } catch (IOException ex) {
            throw new FBSQLException(ex);
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
        return isNew;
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
        if (length == -1L) {
            copyStream(inputStream);
            return;
        }
        try (OutputStream os = setBinaryStream(1)) {
            final byte[] buffer = new byte[(int) Math.min(bufferLength, length)];
            int chunk;
            while (length > 0 && (chunk = inputStream.read(buffer, 0, (int) Math.min(buffer.length, length))) != -1) {
                os.write(buffer, 0, chunk);
                length -= chunk;
            }
        } catch (IOException ioe) {
            throw new SQLException(ioe);
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

    /**
     * Copy data from a character stream into this Blob.
     * <p>
     * Calling with length {@code -1} is equivalent to calling {@link #copyCharacterStream(Reader, String)}.
     * </p>
     *
     * @param reader the source of data to copy
     * @param length The maximum number of bytes to copy, or {@code -1} to read the whole stream
     * @param encoding The encoding used in the character stream
     */
    public void copyCharacterStream(Reader reader, long length, String encoding) throws SQLException {
        if (length == -1L) {
            copyCharacterStream(reader, encoding);
            return;
        }
        try (OutputStream os = setBinaryStream(1);
             OutputStreamWriter osw = encoding != null
                     ? new OutputStreamWriter(os, encoding)
                     : new OutputStreamWriter(os)) {

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

    /**
     * Copy data from a character stream into this Blob. Unlike
     * the {@link #copyCharacterStream(Reader, long, String)} )} method, this one copies bytes
     * until the EOF is reached.
     *
     * @param reader the source of data to copy
     * @param encoding The encoding used in the character stream
     */
    public void copyCharacterStream(Reader reader, String encoding) throws SQLException {
        try (OutputStream os = setBinaryStream(1);
             OutputStreamWriter osw = encoding != null
                ? new OutputStreamWriter(os, encoding)
                : new OutputStreamWriter(os)) {
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
