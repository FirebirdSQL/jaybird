/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2001 Boix i Oltra, S.L.
 SPDX-FileContributor: Alejandro Alberola (Boix i Oltra, S.L.)
 SPDX-FileCopyrightText: Copyright 2002-2008 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002 Mark O'Donohue
 SPDX-FileCopyrightText: Copyright 2003 Nikolay Samofatov
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2016 Adriano dos Santos Fernandes
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.BlobConfig;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.wire.InlineBlob;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.util.InternalApi;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link java.sql.Blob} and {@link FirebirdBlob} interfaces.
 * </p>
 */
@InternalApi
public final class FBBlob implements FirebirdBlob, TransactionListener {

    private static final System.Logger logger = System.getLogger(FBBlob.class.getName());

    private boolean isNew;
    private long blobId;
    @SuppressWarnings("java:S3077")
    private volatile GDSHelper gdsHelper;
    private FBObjectListener.BlobListener blobListener;
    private final Config config;

    private final Collection<FBBlobInputStream> inputStreams = Collections.synchronizedSet(new HashSet<>());
    private FBBlobOutputStream blobOut;
    private InlineBlob cachedInlineBlob;

    private FBBlob(GDSHelper c, boolean isNew, FBObjectListener.BlobListener blobListener, Config config) {
        gdsHelper = c;
        this.isNew = isNew;
        this.blobListener = blobListener != null ? blobListener : FBObjectListener.NoActionBlobListener.instance();
        this.config = requireNonNull(config, "config");
    }

    /**
     * Create new Blob instance. This constructor creates a new fresh Blob, only writing to the Blob is allowed.
     *
     * @param c
     *         connection that will be used to write data to blob
     * @param blobListener
     *         Blob listener instance
     * @param config
     *         blob configuration (cannot be {@code null})
     * @since 5
     */
    public FBBlob(GDSHelper c, FBObjectListener.BlobListener blobListener, Config config) {
        this(c, true, blobListener, config);
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
     *         blob configuration (cannot be {@code null})
     * @since 5
     */
    public FBBlob(GDSHelper c, long blobId, FBObjectListener.BlobListener blobListener, Config config) {
        this(c, false, blobListener, config);
        this.blobId = blobId;
    }

    /**
     * @return configuration associated with this blob
     * @since 5
     */
    Config config() {
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
    FbBlob openBlob() throws SQLException {
        try (var ignored = withLock()) {
            checkClosed();
            if (isNew) {
                throw new SQLException("No Blob ID is available in new Blob object",
                        SQLStateConstants.SQL_STATE_GENERAL_ERROR);
            }

            if (cachedInlineBlob != null) {
                InlineBlob copy = cachedInlineBlob.copy();
                copy.open();
                return copy;
            }

            FbBlob blob = gdsHelper.openBlob(blobId, config);
            if (blob instanceof InlineBlob inlineBlob) {
                this.cachedInlineBlob = inlineBlob;
            }
            return blob;
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
    FbBlob createBlob() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkClosed();
            // For historic reasons we allow creating an output blob even if this is not a new blob. This may need to
            // be reconsidered in the future
            return gdsHelper.createBlob(config);
        }
    }

    LockCloseable withLock() {
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
                var chain = new SQLExceptionChainBuilder();

                for (FBBlobInputStream blobIS : new ArrayList<>(inputStreams)) {
                    try {
                        blobIS.close();
                    } catch (IOException e) {
                        chain.append(new SQLException(e.toString(), SQLStateConstants.SQL_STATE_GENERAL_ERROR, e));
                    }
                }
                inputStreams.clear();

                if (blobOut != null) {
                    try {
                        blobOut.close();
                    } catch (IOException e) {
                        chain.append(new SQLException(e.toString(), SQLStateConstants.SQL_STATE_GENERAL_ERROR, e));
                    }
                }

                chain.throwIfPresent();
            } finally {
                gdsHelper = null;
                blobListener = FBObjectListener.NoActionBlobListener.instance();
                blobOut = null;
                cachedInlineBlob = null;
            }
        }
    }

    @Override
    public InputStream getBinaryStream(long pos, long length) throws SQLException {
        throw new FBDriverNotCapableException("Method getBinaryStream(long, long) is not supported");
    }

    /**
     * Get information about this Blob. This method should be considered as temporary because it provides access to
     * low-level API. More information on how to use the API can be found in "API Guide".
     *
     * @param items
     *         items in which we are interested
     * @param bufferLength
     *         buffer where information will be stored
     * @return array of bytes containing information about this Blob
     * @throws SQLException
     *         if something went wrong
     */
    public byte[] getInfo(byte[] items, int bufferLength) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkClosed();
            blobListener.executionStarted(this);
            // TODO Does it make sense to close blob here?
            try (FbBlob blob = gdsHelper.openBlob(blobId, config)) {
                return blob.getBlobInfo(items, bufferLength);
            } finally {
                blobListener.executionCompleted(this);
            }
        }
    }

    @Override
    public long length() throws SQLException {
        try (var ignored = withLock()) {
            checkClosed();
            blobListener.executionStarted(this);
            try (FbBlob blob = openBlob()) {
                return blob.length();
            } finally {
                blobListener.executionCompleted(this);
            }
        }
    }

    @Override
    public boolean isSegmented() throws SQLException {
        byte[] info = getInfo(new byte[] { ISCConstants.isc_info_blob_type, ISCConstants.isc_info_end }, 20);
        var clumpletReader = new ClumpletReader(ClumpletReader.Kind.InfoResponse, info);
        if (!clumpletReader.find(ISCConstants.isc_info_blob_type)) {
            throw new SQLException("Cannot determine BLOB type", SQLStateConstants.SQL_STATE_GENERAL_ERROR);
        }
        return clumpletReader.getInt() == isc_bpb_type_segmented;
    }

    @Override
    public FBBlob detach() throws SQLException {
        try (var ignored = withLock()) {
            checkClosed();
            var blobCopy = new FBBlob(gdsHelper, blobId, blobListener, config);
            if (cachedInlineBlob != null) {
                blobCopy.cachedInlineBlob = cachedInlineBlob.copy();
            }
            return blobCopy;
        }
    }

    @Override
    public byte[] getBytes(long pos, int length) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Expected value of pos > 0, got " + pos,
                    SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        } else if (pos > Integer.MAX_VALUE) {
            throw new SQLException("Blob position is limited to 2^31 - 1 due to isc_seek_blob limitations",
                    SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        } else if (length < 0) {
            throw new SQLException("Expected value of length >= 0, got " + length,
                    SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        }

        try (LockCloseable ignored = withLock()) {
            blobListener.executionStarted(this);
            try (FirebirdBlob.BlobInputStream in = (FirebirdBlob.BlobInputStream) getBinaryStream()) {
                if (pos != 1) {
                    in.seek((int) pos - 1);
                }

                // We optimize for the case where we can read all data
                byte[] result = new byte[length];
                int read = in.readNBytes(result, 0, length);
                return read == length ? result : Arrays.copyOf(result, read);
            } catch (IOException e) {
                if (e.getCause() instanceof SQLException sqle) {
                    throw sqle;
                }
                throw new SQLException(e.toString(), SQLStateConstants.SQL_STATE_GENERAL_ERROR, e);
            } finally {
                blobListener.executionCompleted(this);
            }
        }
    }

    @Override
    public byte[] getBytes() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            blobListener.executionStarted(this);
            try (FirebirdBlob.BlobInputStream in = (FirebirdBlob.BlobInputStream) getBinaryStream()) {
                long length = in.length();
                if (length > Integer.MAX_VALUE - 8) {
                    // TODO Externalize?
                    throw new SQLNonTransientException(
                            "Blob size of %d bytes exceeds maximum safe array size, use a stream to read this blob"
                                    .formatted(length),
                            SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
                }
                byte[] bytes = new byte[(int) length];
                in.readFully(bytes);
                return bytes;
            } catch (IOException e) {
                if (e.getCause() instanceof SQLException sqle) {
                    throw sqle;
                }
                throw new SQLException(e.toString(), SQLStateConstants.SQL_STATE_GENERAL_ERROR, e);
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

            if (blobOut != null) {
                throw new SQLException("OutputStream already open. Only one blob output stream can be open at a time",
                        SQLStateConstants.SQL_STATE_GENERAL_ERROR);
            } else if (pos < 1) {
                throw new SQLException("You can't start before the beginning of the blob",
                        SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
            } else if (isNew && pos > 1) {
                throw new SQLException("Previous value was null, you must start at position 1",
                        SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
            }

            blobOut = new FBBlobOutputStream(this);
            if (pos > 1) {
                //copy pos bytes from input to output
                //implement this later
                throw new FBDriverNotCapableException("Offset start positions are not yet supported");
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
            if (isNew) {
                throw new SQLException("No Blob ID is available in new Blob object",
                        SQLStateConstants.SQL_STATE_GENERAL_ERROR);
            }

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
        } catch (IOException e) {
            throw new SQLException(e.toString(), SQLStateConstants.SQL_STATE_GENERAL_ERROR, e);
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
        if (newState == TransactionState.COMMITTED || newState == TransactionState.ROLLED_BACK) {
            try (LockCloseable ignored = withLock()) {
                free();
            } catch (SQLException e) {
                logger.log(System.Logger.Level.ERROR, "Error calling free on blob during transaction end", e);
            }
        }
    }

    private void checkClosed() throws SQLException {
        if (gdsHelper == null) {
            throw FbExceptionBuilder.toException(JaybirdErrorCodes.jb_blobClosed);
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
    public static Config createConfig(int subType, boolean useStreamBlob, int blobBufferSize,
            DatatypeCoder datatypeCoder) {
        return new Config(subType, useStreamBlob, blobBufferSize, datatypeCoder);
    }

    /**
     * Standard configuration for blobs.
     *
     * @since 5
     */
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
            // We use an empty buffer so the "default" blob is provided without type conversion or transliteration.
            // This also ensures the inline blob cache is used (both for native and pure Java).
            // NOTE: We are assuming that the parameter buffer passed in is empty to begin with.
        }

    }
}
