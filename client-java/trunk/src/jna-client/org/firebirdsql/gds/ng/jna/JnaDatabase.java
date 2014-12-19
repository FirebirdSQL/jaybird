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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.jni.BlobParameterBufferImp;
import org.firebirdsql.gds.impl.jni.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.jni.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabase} for native client access.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaDatabase extends AbstractFbDatabase implements TransactionListener {

    // TODO Find out if there are any exception from JNA that we need to be prepared to handle.

    private static final Logger log = LoggerFactory.getLogger(JnaDatabase.class, false);
    private static final ParameterConverter PARAMETER_CONVERTER = new JnaParameterConverter();
    private static final DatatypeCoder datatypeCoder =
            java.nio.ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
                    ? LittleEndianDatatypeCoder.getInstance()
                    : BigEndianDatatypeCoder.getInstance();
    public static final int STATUS_VECTOR_SIZE = 20;
    public static final int MAX_STATEMENT_LENGTH = 64 * 1024;

    private final JnaConnection jnaConnection;
    private final FbClientLibrary clientLibrary;
    // TODO Clear on disconnect?
    private final IntByReference handle = new IntByReference(0);
    private final ISC_STATUS[] statusVector = new ISC_STATUS[STATUS_VECTOR_SIZE];

    public JnaDatabase(JnaConnection jnaConnection) {
        this.jnaConnection = jnaConnection;
        clientLibrary = jnaConnection.getClientLibrary();
    }

    /**
     * @return The client library instance associated with the database.
     */
    protected final FbClientLibrary getClientLibrary() {
        return clientLibrary;
    }

    @Override
    protected void checkConnected() throws SQLException {
        if (!isAttached()) {
            throw new SQLException("The connection is not attached to a database", FBSQLException.SQL_STATE_CONNECTION_ERROR);
        }
    }

    @Override
    protected void internalDetach() throws SQLException {
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_detach_database(statusVector, handle);
                processStatusVector();
            } catch (SQLException ex) {
                throw ex;
            } catch (Exception ex) {
                // TODO Replace with specific error (eg native client error)
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_network_error)
                        .messageParameter(jnaConnection.getServerName())
                        .cause(ex)
                        .toSQLException();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void attach() throws SQLException {
        DatabaseParameterBuffer dpb = ((DatabaseParameterBufferExtension) PARAMETER_CONVERTER
                .toDatabaseParameterBuffer(jnaConnection.getConnectionProperties(), jnaConnection.getEncodingFactory()))
                .removeExtensionParams();
        attachOrCreate(dpb, false);
    }

    protected void attachOrCreate(DatabaseParameterBuffer dpb, boolean create) throws SQLException {
        if (isAttached()) {
            throw new SQLException("Already attached to a database");
        }
        if (!(dpb instanceof DatabaseParameterBufferImp)) {
            DatabaseParameterBuffer tempDpb = new DatabaseParameterBufferImp();
            for (Parameter parameter : dpb) {
                parameter.copyTo(tempDpb, getEncoding());
            }
            dpb = tempDpb;
        }
        final byte[] dbName = getEncoding().encodeToCharset(getDatabaseUrl());
        final byte[] dpbArray = ((DatabaseParameterBufferImp) dpb).getBytesForNativeCode();

        synchronized (getSynchronizationObject()) {
            try {
                if (create) {
                    clientLibrary.isc_create_database(statusVector, (short) dbName.length, dbName, handle,
                            (short) dpbArray.length, dpbArray, getConnectionDialect());
                } else {
                    clientLibrary.isc_attach_database(statusVector, (short) dbName.length, dbName, handle,
                            (short) dpbArray.length, dpbArray);
                }
                processStatusVector();
            } catch (SQLException ex) {
                safelyDetach();
                throw ex;
            } catch (Exception ex) {
                safelyDetach();
                // TODO Replace with specific error (eg native client error)
                throw new FbExceptionBuilder()
                        .exception(ISCConstants.isc_network_error)
                        .messageParameter(jnaConnection.getServerName())
                        .cause(ex)
                        .toSQLException();
            }
            setAttached();
            afterAttachActions();
        }
    }

    /**
     * Additional tasks to execute directly after attach operation.
     * <p>
     * Implementation retrieves database information like dialect ODS and server
     * version.
     * </p>
     *
     * @throws SQLException
     *         For errors reading or writing database information.
     */
    protected void afterAttachActions() throws SQLException {
        getDatabaseInfo(getDescribeDatabaseInfoBlock(), 1024, getDatabaseInformationProcessor());
    }

    @Override
    public void createDatabase(DatabaseParameterBuffer dpb) throws SQLException {
        attachOrCreate(dpb, true);
    }

    @Override
    public void dropDatabase() throws SQLException {
        checkConnected();
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.isc_drop_database(statusVector, handle);
                processStatusVector();
            } finally {
                setDetached();
            }
        }
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        checkConnected();
        // TODO Test what happens with 2.1 and earlier client library
        synchronized (getSynchronizationObject()) {
            try {
                clientLibrary.fb_cancel_operation(statusVector, handle, (short) kind);
            } finally {
                if (kind == fb_cancel_abort) {
                    setDetached();
                }
            }
        }
    }

    @Override
    public JnaTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        checkConnected();
        if (!(tpb instanceof TransactionParameterBufferImpl)) {
            TransactionParameterBufferImpl tempTpb = new TransactionParameterBufferImpl();
            for (Parameter parameter : tpb) {
                parameter.copyTo(tempTpb, getEncoding());
            }
            tpb = tempTpb;
        }
        final IntByReference transactionHandle = new IntByReference(0);
        byte[] tpbArray = ((TransactionParameterBufferImpl) tpb).getBytesForNativeCode();
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_start_transaction(statusVector, transactionHandle, (short) 1, handle, (short) tpbArray.length, tpbArray);
            processStatusVector();

            final JnaTransaction transaction = new JnaTransaction(this, transactionHandle, TransactionState.ACTIVE);
            transactionAdded(transaction);
            return transaction;
        }
    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        checkConnected();
        byte[] transactionIdBuffer = new byte[4];
        // Note: This uses an atypical encoding (as this is actually a TPB without a type)
        for (int i = 0; i < 4; i++) {
            transactionIdBuffer[i] = (byte) (transactionId >>> (i * 8));
        }
        final IntByReference transactionHandle = new IntByReference(0);
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_reconnect_transaction(statusVector, handle, transactionHandle,
                    (short) transactionIdBuffer.length, transactionIdBuffer);
            processStatusVector();

            final JnaTransaction transaction = new JnaTransaction(this, transactionHandle, TransactionState.PREPARED);
            transactionAdded(transaction);
            return transaction;
        }
    }

    @Override
    public JnaStatement createStatement(FbTransaction transaction) throws SQLException {
        checkConnected();
        JnaStatement stmt = new JnaStatement(this);
        stmt.setTransaction(transaction);
        return stmt;
    }

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer)
            throws SQLException {
        return new JnaBlob(this, (JnaTransaction) transaction, blobParameterBuffer);
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer,
            long blobId) throws SQLException {
        return new JnaBlob(this, (JnaTransaction) transaction, blobParameterBuffer, blobId);
    }

    @Override
    public BlobParameterBuffer createBlobParameterBuffer() {
        return new BlobParameterBufferImp();
    }

    @Override
    public byte[] getDatabaseInfo(final byte[] requestItems, final int maxBufferLength) throws SQLException {
        final ByteBuffer responseBuffer = ByteBuffer.allocateDirect(maxBufferLength);
        synchronized (getSynchronizationObject()) {
            clientLibrary.isc_database_info(statusVector, handle, (short) requestItems.length, requestItems,
                    (short) maxBufferLength, responseBuffer);
            processStatusVector();
        }
        byte[] responseArray = new byte[maxBufferLength];
        responseBuffer.get(responseArray);
        return responseArray;
    }

    @Override
    public void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        // TODO also implement op_exec_immediate2
        if (isAttached()) {
            if (transaction == null) {
                // TODO SQLState and/or Firebird specific error
                throw new SQLException("executeImmediate requires a transaction when attached");
            } else if (!(transaction instanceof JnaTransaction)) {
                // TODO SQLState and/or Firebird specific error
                throw new SQLNonTransientException(String.format("Invalid transaction handle type: %s, expected: %s",
                        transaction.getClass(), JnaTransaction.class));
            }
            checkTransactionActive(transaction);
        } else if (transaction != null) {
            // TODO SQLState and/or Firebird specific error
            throw new SQLException("executeImmediate when not attached should have no transaction");
        }

        final byte[] statementArray = getEncoding().encodeToCharset(statementText);
        synchronized (getSynchronizationObject()) {
            // Information in tempXSqlDa is ignored
            final org.firebirdsql.jna.fbclient.XSQLDA tempXSqlDa = new org.firebirdsql.jna.fbclient.XSQLDA();
            clientLibrary.isc_dsql_execute_immediate(statusVector, handle,
                    transaction != null ? ((JnaTransaction) transaction).getJnaHandle() : new IntByReference(),
                    (short) statementArray.length, statementArray, getConnectionDialect(), tempXSqlDa);
            processStatusVector();

            if (!isAttached()) {
                setAttached();
                afterAttachActions();
            }
        }
    }

    @Override
    public short getConnectionDialect() {
        return jnaConnection.getConnectionDialect();
    }

    @Override
    public int getHandle() {
        return handle.getValue();
    }

    public IntByReference getJnaHandle() {
        return handle;
    }

    @Override
    public final IEncodingFactory getEncodingFactory() {
        return jnaConnection.getEncodingFactory();
    }

    @Override
    public final Encoding getEncoding() {
        return jnaConnection.getEncoding();
    }

    public final EncodingDefinition getEncodingDefinition() {
        return jnaConnection.getEncodingDefinition();
    }

    @Override
    public final DatatypeCoder getDatatypeCoder() {
        return JnaDatabase.datatypeCoder;
    }

    /**
     * Builds the database URL for the library.
     *
     * @return Database URL
     */
    protected String getDatabaseUrl() {
        StringBuilder sb = new StringBuilder();
        if (jnaConnection.getServerName() != null) {
            sb.append(jnaConnection.getServerName())
                    .append('/');
        }
        sb.append(jnaConnection.getPortNumber())
                .append(':')
                .append(jnaConnection.getDatabaseName());
        return sb.toString();
    }

    private void processStatusVector() throws SQLException {
        processStatusVector(statusVector, getDatabaseWarningCallback());
    }

    public void processStatusVector(ISC_STATUS[] statusVector,
            WarningMessageCallback warningMessageCallback) throws SQLException {
        if (warningMessageCallback == null) {
            warningMessageCallback = getDatabaseWarningCallback();
        }
        boolean debug = log.isDebugEnabled();
        final FbExceptionBuilder builder = new FbExceptionBuilder();
        int vectorIndex = 0;
        processingLoop:
        while (vectorIndex < statusVector.length) {
            int arg = statusVector[vectorIndex++].intValue();
            int errorCode;
            switch (arg) {
            case isc_arg_gds:
                errorCode = statusVector[vectorIndex++].intValue();
                if (debug) log.debug("readStatusVector arg:isc_arg_gds int: " + errorCode);
                if (errorCode != 0) {
                    builder.exception(errorCode);
                }
                break;
            case isc_arg_warning:
                errorCode = statusVector[vectorIndex++].intValue();
                if (debug) log.debug("readStatusVector arg:isc_arg_warning int: " + errorCode);
                if (errorCode != 0) {
                    builder.warning(errorCode);
                }
                break;
            case isc_arg_interpreted:
            case isc_arg_string:
            case isc_arg_sql_state:
                long stringPointerAddress = statusVector[vectorIndex++].longValue();
                Pointer stringPointer = new Pointer(stringPointerAddress);
                String stringValue = stringPointer.getString(0, getEncodingDefinition().getJavaEncodingName());
                if (arg != isc_arg_sql_state) {
                    if (debug) log.debug("readStatusVector string: " + stringValue);
                    builder.messageParameter(stringValue);
                } else {
                    // TODO Is this actually returned from server?
                    if (debug) log.debug("readStatusVector sqlstate: " + stringValue);
                    builder.sqlState(stringValue);
                }
                break;
            case isc_arg_cstring:
                int stringLength = statusVector[vectorIndex++].intValue();
                long cStringPointerAddress = statusVector[vectorIndex++].longValue();
                Pointer cStringPointer = new Pointer(cStringPointerAddress);
                byte[] stringData = cStringPointer.getByteArray(0, stringLength);
                String cStringValue = getEncoding().decodeFromCharset(stringData);
                builder.messageParameter(cStringValue);
                break;
            case isc_arg_number:
                int intValue = statusVector[vectorIndex++].intValue();
                if (debug) log.debug("readStatusVector arg:isc_arg_number int: " + intValue);
                builder.messageParameter(intValue);
                break;
            case isc_arg_end:
                break processingLoop;
            default:
                int e = statusVector[vectorIndex++].intValue();
                if (debug) log.debug("readStatusVector arg: " + arg + " int: " + e);
                builder.messageParameter(e);
                break;
            }
        }
        SQLException exception = builder.toFlatSQLException();
        if (exception instanceof SQLWarning) {
            warningMessageCallback.processWarning((SQLWarning) exception);
        } else if (exception != null) {
            throw exception;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (isAttached()) {
                safelyDetach();
            }
        } finally {
            super.finalize();
        }
    }
}
