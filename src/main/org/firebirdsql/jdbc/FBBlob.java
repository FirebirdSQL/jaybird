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
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.BlobConfig;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.InternalApi;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.BLOB_SUB_TYPE_TEXT;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.TypeValues.isc_bpb_type_segmented;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.TypeValues.isc_bpb_type_stream;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.isc_bpb_source_interp;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.isc_bpb_source_type;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.isc_bpb_target_interp;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.isc_bpb_target_type;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.isc_bpb_type;

/**
 * Firebird implementation of {@link java.sql.Blob}.
 */
public final class FBBlob implements FirebirdBlob, TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(FBBlob.class);

    private boolean isNew;
    private long blobId;
    private volatile GDSHelper gdsHelper;
    private FBObjectListener.BlobListener blobListener;
    private final Config config;

    private final Collection<FBBlobInputStream> inputStreams = Collections.synchronizedSet(new HashSet<>());
    private FBBlobOutputStream blobOut = null;

    private FBBlob(GDSHelper c, boolean isNew, FBObjectListener.BlobListener blobListener, Config config) {
        gdsHelper = c;
        this.isNew = isNew;
        this.blobListener = blobListener != null ? blobListener : FBObjectListener.NoActionBlobListener.instance();
        // TODO Replace with requireNonNull once deprecated constructors passing null are removed
        IConnectionProperties connectionProperties = c.getConnectionProperties();
        this.config = config != null ? config : createConfig(ISCConstants.BLOB_SUB_TYPE_BINARY,
                connectionProperties.isUseStreamBlobs(), connectionProperties.getBlobBufferSize(),
                c.getCurrentDatabase().getDatatypeCoder());
    }

    /**
     * Create new Blob instance. This constructor creates new fresh Blob, only writing to the Blob is allowed.
     *
     * @param c
     *         connection that will be used to write data to blob
     * @param blobListener
     *         Blob listener instance
     * @deprecated will be removed in Jaybird 6, use {@link #FBBlob(GDSHelper, FBObjectListener.BlobListener, Config)
     */
    @Deprecated
    public FBBlob(GDSHelper c, FBObjectListener.BlobListener blobListener) {
        this(c, true, blobListener, null);
    }

    /**
     * Create new Blob instance. This constructor creates new fresh Blob, only writing to the Blob is allowed.
     *
     * @param c
     *         connection that will be used to write data to blob
     * @param blobListener
     *         Blob listener instance
     * @param config
     *         blob configuration ({@code null} allowed in Jaybird 5, will be disallowed in Jaybird 6)
     * @since 5
     */
    public FBBlob(GDSHelper c, FBObjectListener.BlobListener blobListener, Config config) {
        this(c, true, blobListener, config);
    }

    /**
     * Create new Blob instance. This constructor creates new fresh Blob, only
     * writing to the Blob is allowed.
     *
     * @param c
     *         connection that will be used to write data to blob.
     * @deprecated will be removed in Jaybird 6, use {@link #FBBlob(GDSHelper, FBObjectListener.BlobListener, Config)
     */
    @Deprecated
    public FBBlob(GDSHelper c) {
        this(c, null);
    }

    /**
     * Create instance of this class to access existing Blob.
     *
     * @param c
     *         connection that will be used to access Blob.
     * @param blobId
     *         ID of the Blob.
     * @param blobListener
     *         blob listener instance
     * @deprecated will be removed in Jaybird 6, use {@link #FBBlob(GDSHelper, long, FBObjectListener.BlobListener, Config)}
     */
    @Deprecated
    public FBBlob(GDSHelper c, long blobId, FBObjectListener.BlobListener blobListener) {
        this(c, blobId, blobListener, null);
    }

    /**
     * Create instance of this class to access existing Blob.
     *
     * @param c
     *         connection that will be used to access Blob.
     * @param blobId
     *         ID of the Blob.
     * @param blobListener
     *         blob listener instance
     * @param config
     *         blob configuration ({@code null} allowed in Jaybird 5, will be disallowed in Jaybird 6)
     * @since 5
     */
    public FBBlob(GDSHelper c, long blobId, FBObjectListener.BlobListener blobListener, Config config) {
        this(c, false, blobListener, config);
        this.blobId = blobId;
    }

    /**
     * Create instance of this class to access existing Blob.
     *
     * @param c
     *         connection that will be used to access Blob.
     * @param blobId
     *         ID of the Blob.
     * @deprecated will be removed in Jaybird 6, use {@link #FBBlob(GDSHelper, long, FBObjectListener.BlobListener, Config)}
     */
    @Deprecated
    public FBBlob(GDSHelper c, long blobId) {
        this(c, blobId, null);
    }

    /**
     * @return configuration associated with this blob
     * @since 5
     */
    final Config config() {
        return config;
    }

    /**
     * Opens a blob handle for reading.
     *
     * @return blob handle for reading
     * @throws SQLException
     *         For errors opening the blob
     * @since 5
     */
    final FbBlob openBlob() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkClosed();
            if (isNew) {
                throw new FBSQLException("No Blob ID is available in new Blob object.");
            }
            return gdsHelper.openBlob(blobId, config);
        }
    }

    /**
     * Creates a blob handle for writing.
     *
     * @return blob handle for writing
     * @throws SQLException
     *         For errors creating the blob
     * @since 5
     */
    final FbBlob createBlob() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkClosed();
            // For historic reasons we allow creating an output blob even if this is not a new blob. This may need to
            // be reconsidered in the future
            return gdsHelper.createBlob(config);
        }
    }

    protected final LockCloseable withLock() {
        GDSHelper gdsHelper = this.gdsHelper;
        if (gdsHelper != null) {
            return gdsHelper.withLock();
        }
        return LockCloseable.NO_OP;
    }

    @Override
    public void free() throws SQLException {
        try (LockCloseable ignored = withLock()) {
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
        try (LockCloseable ignored = withLock()) {
            checkClosed();
            blobListener.executionStarted(this);
            // TODO Does it make sense to close blob here?
            try (FbBlob blob = gdsHelper.openBlob(blobId, config)) {
                return blob.getBlobInfo(items, buffer_length);
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
        return type == isc_bpb_type_segmented;
    }

    @Override
    public FirebirdBlob detach() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkClosed();
            return new FBBlob(gdsHelper, blobId, blobListener, config);
        }
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (pos < 1)
            throw new FBSQLException("Blob position should be >= 1");

        if (pos > Integer.MAX_VALUE)
            throw new FBSQLException("Blob position is limited to 2^31 - 1 due to isc_seek_blob limitations.",
                    SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);

        try (LockCloseable ignored = withLock()) {
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
        try (LockCloseable ignored = withLock()) {
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
        try (LockCloseable ignored = withLock();
             OutputStream out = setBinaryStream(pos)) {
            out.write(bytes, offset, len);
            return len;
        } catch (IOException e) {
            throw new SQLException("IOException writing bytes to blob", e);
        }
    }

    @Override
    public OutputStream setBinaryStream(long pos) throws SQLException {
        try (LockCloseable ignored = withLock()) {
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
     * Get the identifier for this {@code Blob}
     *
     * @return This {@code Blob}'s identifier
     * @throws SQLException if a database access error occurs
     */
    public long getBlobId() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (isNew)
                throw new FBSQLException("No Blob ID is available in new Blob object.");

            return blobId;
        }
    }

    void setBlobId(long blobId) {
        try (LockCloseable ignored = withLock()) {
            this.blobId = blobId;
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
        try (LockCloseable ignored = withLock();
             OutputStream out = setBinaryStream(1)) {
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
        return config.blobBufferSize();
    }

    /**
     * Notifies this blob that {@code stream} has been closed.
     *
     * @param stream
     *         InputStream that has been closed.
     */
    void notifyClosed(FBBlobInputStream stream) {
        inputStreams.remove(stream);
    }

    /**
     * @return {@code true} when this is an uninitialized output blob, {@code false} otherwise.
     */
    boolean isNew() {
        try (LockCloseable ignored = withLock()) {
            return isNew;
        }
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
        try (LockCloseable ignored = withLock();
             OutputStream os = setBinaryStream(1)) {
            final byte[] buffer = new byte[(int) Math.min(getBufferLength(), length)];
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

    /**
     * Copy the contents of an {@code InputStream} into this Blob. Unlike
     * the {@link #copyStream(InputStream, long)} method, this one copies bytes
     * until the EOF is reached.
     *
     * @param inputStream the stream from which data will be copied
     * @throws SQLException if a database access error occurs
     */
    public void copyStream(InputStream inputStream) throws SQLException {
        try (LockCloseable ignored = withLock();
             OutputStream os = setBinaryStream(1)) {
            final byte[] buffer = new byte[getBufferLength()];
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
     * Calling with length {@code -1} is equivalent to calling {@link #copyCharacterStream(Reader, Encoding)}.
     * </p>
     *
     * @param reader the source of data to copy
     * @param length The maximum number of bytes to copy, or {@code -1} to read the whole stream
     * @param encoding The encoding used in the character stream
     * @see #copyCharacterStream(Reader, long)
     */
    public void copyCharacterStream(Reader reader, long length, Encoding encoding) throws SQLException {
        if (length == -1L) {
            copyCharacterStream(reader, encoding);
            return;
        }
        try (LockCloseable ignored = withLock();
             OutputStream os = setBinaryStream(1);
             Writer osw = encoding.createWriter(os)) {

            copyTo(reader, osw, length);
        } catch (UnsupportedEncodingException ex) {
            throw new SQLException("Cannot set character stream because the encoding '" + encoding +
                    "' is unsupported in the JVM. Please report this to the driver developers.");
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    private void copyTo(Reader reader, Writer osw, long length) throws IOException {
        final char[] buffer = new char[(int) Math.min(getBufferLength(), length)];
        int chunk;
        while (length > 0 && (chunk = reader.read(buffer, 0, (int) Math.min(buffer.length, length))) != -1) {
            osw.write(buffer, 0, chunk);
            length -= chunk;
        }
    }

    /**
     * Copy data from a character stream into this Blob. This method uses the encoding from the blob config (field
     * character set for subtype TEXT, if known, otherwise connection character set).
     * <p>
     * Calling with length {@code -1} is equivalent to calling {@link #copyCharacterStream(Reader)}.
     * </p>
     *
     * @param reader
     *         the source of data to copy
     * @param length
     *         The maximum number of bytes to copy, or {@code -1} to read the whole stream
     * @since 5
     */
    public void copyCharacterStream(Reader reader, long length) throws SQLException {
        if (length == -1L) {
            copyCharacterStream(reader);
            return;
        }
        try (LockCloseable ignored = withLock();
             OutputStream os = setBinaryStream(1);
             Writer osw = config.createWriter(os)) {
            copyTo(reader, osw, length);
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    /**
     * Copy data from a character stream into this Blob. Unlike the {@link #copyCharacterStream(Reader, long, Encoding)}
     * method, this one copies bytes until the EOF is reached.
     *
     * @param reader the source of data to copy
     * @param encoding The encoding used in the character stream
     * @see #copyCharacterStream(Reader)
     */
    public void copyCharacterStream(Reader reader, Encoding encoding) throws SQLException {
        try (LockCloseable ignored = withLock();
             OutputStream os = setBinaryStream(1);
             Writer osw = encoding.createWriter(os)) {
            copyTo(reader, osw);
        } catch (UnsupportedEncodingException ex) {
            throw new SQLException("Cannot set character stream because the encoding '" + encoding +
                    "' is unsupported in the JVM. Please report this to the driver developers.");
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    private void copyTo(Reader reader, Writer osw) throws IOException {
        final char[] buffer = new char[getBufferLength()];
        int chunk;
        while ((chunk = reader.read(buffer, 0, buffer.length)) != -1) {
            osw.write(buffer, 0, chunk);
        }
    }

    /**
     * Copy data from a character stream into this Blob. Unlike the {@link #copyCharacterStream(Reader, long)} method,
     * this one copies bytes until the EOF is reached. This method uses the encoding from the blob config (field
     * character set for subtype TEXT, if known, otherwise connection character set).
     *
     * @param reader
     *         the source of data to copy
     * @since 5
     */
    public void copyCharacterStream(Reader reader) throws SQLException {
        try (LockCloseable ignored = withLock();
             OutputStream os = setBinaryStream(1);
             Writer osw = config.createWriter(os)) {
            copyTo(reader, osw);
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        switch (newState) {
        case COMMITTED:
        case ROLLED_BACK:
            try (LockCloseable ignored = withLock()) {
                free();
            } catch (SQLException e) {
                logger.error("Error calling free on blob during transaction end", e);
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

    /**
     * Creates a blob configuration from a field descriptor and connection properties.
     *
     * @param fieldDescriptor
     *         field descriptor
     * @param connectionProperties
     *         connection properties
     * @return field based blob configuration
     * @since 5
     */
    @InternalApi
    public static Config createConfig(FieldDescriptor fieldDescriptor,
            DatabaseConnectionProperties connectionProperties) {
        return createConfig(fieldDescriptor.getSubType(), connectionProperties.isUseStreamBlobs(),
                connectionProperties.getBlobBufferSize(), fieldDescriptor.getDatatypeCoder());
    }

    /**
     * Creates a blob configuration from a subtype and connection properties and datatype coder.
     *
     * @param subType
     *         blob subtype (e.g. {@link ISCConstants#BLOB_SUB_TYPE_BINARY} or {@link ISCConstants#BLOB_SUB_TYPE_TEXT})
     * @param connectionProperties
     *         connection properties
     * @param datatypeCoder
     *         data type coder for the connection character set
     * @return field based blob configuration
     * @since 5
     */
    @InternalApi
    public static Config createConfig(int subType, DatabaseConnectionProperties connectionProperties,
            DatatypeCoder datatypeCoder) {
        return createConfig(subType, connectionProperties.isUseStreamBlobs(), connectionProperties.getBlobBufferSize(),
                datatypeCoder);
    }

    /**
     * Create a blob configuration.
     *
     * @param subType
     *         blob subtype (e.g. {@link ISCConstants#BLOB_SUB_TYPE_BINARY} or {@link ISCConstants#BLOB_SUB_TYPE_TEXT})
     * @param useStreamBlob
     *         {@code true} use stream blob, {@code false} use segmented blob
     * @param blobBufferSize
     *         blob buffer size
     * @param datatypeCoder
     *         data type coder for the connection character set
     * @return generic blob configuration
     * @since 5
     */
    @InternalApi
    public static Config createConfig(int subType, boolean useStreamBlob, int blobBufferSize,
            DatatypeCoder datatypeCoder) {
        return new Config(subType, useStreamBlob, blobBufferSize, datatypeCoder);
    }

    /**
     * Standard configuration for blobs.
     *
     * @since 5
     */
    @InternalApi
    public static final class Config implements BlobConfig {

        private final boolean streamBlob;
        private final int subType;
        private final int blobBufferSize;
        private final DatatypeCoder datatypeCoder;

        private Config(int subType, boolean streamBlob, int blobBufferSize, DatatypeCoder datatypeCoder) {
            this.streamBlob = streamBlob;
            this.subType = subType;
            this.blobBufferSize = blobBufferSize;
            this.datatypeCoder = requireNonNull(datatypeCoder, "datatypeCoder");
        }

        public int blobBufferSize() {
            return blobBufferSize;
        }

        public Reader createReader(InputStream inputStream) {
            return datatypeCoder.createReader(inputStream);
        }

        public Writer createWriter(OutputStream outputStream) {
            return datatypeCoder.createWriter(outputStream);
        }

        @Override
        public void writeOutputConfig(BlobParameterBuffer blobParameterBuffer) {
            blobParameterBuffer.addArgument(isc_bpb_type, streamBlob ? isc_bpb_type_stream : isc_bpb_type_segmented);
            /*
             NOTE: Ideally, only target type and interp would need to be written, but that results in errors about
             missing filters between type 0 and the target type (e.g. for user-defined blob sub_type -1:
             "filter not found to convert type 0 to type -1")
            */
            blobParameterBuffer.addArgument(isc_bpb_source_type, subType);
            blobParameterBuffer.addArgument(isc_bpb_target_type, subType);
            if (subType == BLOB_SUB_TYPE_TEXT) {
                int characterSetId = datatypeCoder.getEncodingDefinition().getFirebirdCharacterSetId();
                blobParameterBuffer.addArgument(isc_bpb_source_interp, characterSetId);
                // NOTE: Firebird doesn't seem to store the blob character set id, possibly this causes issues elsewhere
                blobParameterBuffer.addArgument(isc_bpb_target_interp, characterSetId);
            }
        }

        @Override
        public void writeInputConfig(BlobParameterBuffer blobParameterBuffer) {
            blobParameterBuffer.addArgument(isc_bpb_type, streamBlob ? isc_bpb_type_stream : isc_bpb_type_segmented);
            /*
             NOTE: It shouldn't be necessary to set source_type, but it looks like Firebird somehow ignores the actual
             blob subtype, which then causes incorrect transliteration as it tries to convert as if it is
             blob sub_type binary
            */
            blobParameterBuffer.addArgument(isc_bpb_source_type, subType);
            blobParameterBuffer.addArgument(isc_bpb_target_type, subType);
            if (subType == BLOB_SUB_TYPE_TEXT) {
                int characterSetId = datatypeCoder.getEncodingDefinition().getFirebirdCharacterSetId();
                // NOTE: Not writing (source_interp) should result in transliteration from the blob character set
                blobParameterBuffer.addArgument(isc_bpb_target_interp, characterSetId);
            }
        }
    }
}
