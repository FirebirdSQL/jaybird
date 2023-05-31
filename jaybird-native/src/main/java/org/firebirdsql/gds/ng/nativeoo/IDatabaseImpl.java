package org.firebirdsql.gds.ng.nativeoo;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.AbstractFbDatabase;
import org.firebirdsql.gds.ng.FbBatch;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbMessageMetadata;
import org.firebirdsql.gds.ng.FbMetadataBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.ParameterConverter;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.FbInterface;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.jna.fbclient.FbInterface.IAttachment;
import org.firebirdsql.jna.fbclient.FbInterface.IEvents;
import org.firebirdsql.jna.fbclient.FbInterface.IMaster;
import org.firebirdsql.jna.fbclient.FbInterface.IProvider;
import org.firebirdsql.jna.fbclient.FbInterface.IStatus;
import org.firebirdsql.jna.fbclient.FbInterface.ITransaction;
import org.firebirdsql.jna.fbclient.FbInterface.IUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.util.HashMap;
import java.util.Map;

import static org.firebirdsql.gds.ISCConstants.fb_cancel_abort;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabase} for native OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 6.0
 */
public class IDatabaseImpl extends AbstractFbDatabase<NativeDatabaseConnection>
        implements FbAttachment, TransactionListener {

    private static final ParameterConverter<NativeDatabaseConnection, ?> PARAMETER_CONVERTER = new IParameterConverterImpl();

    private FbClientLibrary clientLibrary;
    private IMaster master;
    private final IProvider provider;
    private final IUtil util;
    protected IAttachment attachment;
    private final Map<EventHandle, IEvents> events = new HashMap<>();
    protected IStatus status;


    public IDatabaseImpl(NativeDatabaseConnection connection) {
        super(connection, connection.createDatatypeCoder());
        clientLibrary = connection.getClientLibrary();
        master = ((FbInterface)clientLibrary).fb_get_master_interface();
        provider = master.getDispatcher();
        util = master.getUtilInterface();
        attachment = null;
        status = master.getStatus();
    }

    /**
     * @return The client library instance associated with the database.
     */
    protected final FbClientLibrary getClientLibrary() {
        return clientLibrary;
    }

    @Override
    public void attach() throws SQLException {
        try {
            final DatabaseParameterBuffer dpb = PARAMETER_CONVERTER.toDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, false);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    protected void internalDetach() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            attachment.detach(getStatus());
            processStatus();
        } catch (SQLException e) {
            throw e;
        } finally {
            setDetached();
        }
    }

    @Override
    public void createDatabase() throws SQLException {
        try {
            final DatabaseParameterBuffer dpb = PARAMETER_CONVERTER.toDatabaseParameterBuffer(connection);
            attachOrCreate(dpb, true);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void dropDatabase() throws SQLException {
        try {
            checkConnected();
            try (LockCloseable ignored = withLock()) {
                attachment.dropDatabase(getStatus());
            } finally {
                setDetached();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        try {
            checkConnected();
            // No synchronization, otherwise cancel will never work
            try {
                attachment.cancelOperation(getStatus(), kind);
                processStatus();
            } finally {
                if (kind == fb_cancel_abort) {
                    attachment.detach(getStatus());
                    processStatus();
                    setDetached();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        try {
            checkConnected();
            final byte[] tpbArray = tpb.toBytesWithType();
            try (LockCloseable ignored = withLock()) {
                ITransaction transaction = attachment.startTransaction(getStatus(), tpbArray.length, tpbArray);
                processStatus();
                final ITransactionImpl transactionImpl = new ITransactionImpl(this, transaction,
                        TransactionState.ACTIVE);
                transactionAdded(transactionImpl);
                return transactionImpl;
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbTransaction reconnectTransaction(long transactionId) throws SQLException {
        try {
            checkConnected();
            final byte[] transactionIdBuffer = getTransactionIdBuffer(transactionId);

            try (LockCloseable ignored = withLock()) {
                ITransaction iTransaction = attachment.reconnectTransaction(getStatus(), transactionIdBuffer.length,
                        transactionIdBuffer);
                processStatus();
                final ITransactionImpl transaction =
                        new ITransactionImpl(this, iTransaction, TransactionState.PREPARED);
                transactionAdded(transaction);
                return transaction;
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected byte[] getTransactionIdBuffer(long transactionId) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
        try {
            VaxEncoding.encodeVaxLongWithoutLength(bos, transactionId);
        } catch (IOException e) {
            // ignored: won't happen with a ByteArrayOutputStream
        }
        return bos.toByteArray();
    }

    @Override
    public FbStatement createStatement(FbTransaction transaction) throws SQLException {
        try {
            checkConnected();
            final IStatementImpl statement = new IStatementImpl(this);
            statement.addExceptionListener(exceptionListenerDispatcher);
            statement.setTransaction(transaction);
            return statement;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        final IBlobImpl blob = new IBlobImpl(this, (ITransactionImpl) transaction, blobParameterBuffer);
        blob.addExceptionListener(exceptionListenerDispatcher);
        return blob;
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, BlobParameterBuffer blobParameterBuffer, long blobId) {
        final IBlobImpl blob = new IBlobImpl(this, (ITransactionImpl) transaction, blobParameterBuffer, blobId);
        blob.addExceptionListener(exceptionListenerDispatcher);
        return blob;
    }

    @Override
    public byte[] getDatabaseInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        try {
            final byte[] responseArray = new byte[maxBufferLength];
            try (LockCloseable ignored = withLock()) {
                attachment.getInfo(getStatus(), requestItems.length, requestItems, (short) maxBufferLength, responseArray);
            }
            processStatus();
            return responseArray;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(new SQLException(e));
            throw new SQLException(e);
        }
    }

    @Override
    public void executeImmediate(String statementText, FbTransaction transaction) throws SQLException {
        try {
            if (isAttached()) {
                if (transaction == null) {
                    throw FbExceptionBuilder
                            .forException(JaybirdErrorCodes.jb_executeImmediateRequiresTransactionAttached)
                            .toFlatSQLException();
                } else if (!(transaction instanceof ITransactionImpl)) {
                    throw new SQLNonTransientException(
                            String.format("Invalid transaction handle type: %s, expected: %s",
                                    transaction.getClass(), ITransactionImpl.class),
                            SQLStateConstants.SQL_STATE_GENERAL_ERROR);
                }
                checkTransactionActive(transaction);
            } else if (transaction != null) {
                throw FbExceptionBuilder
                        .forException(JaybirdErrorCodes.jb_executeImmediateRequiresNoTransactionDetached)
                        .toFlatSQLException();
            }
            final byte[] statementArray = getEncoding().encodeToCharset(statementText);
            try (LockCloseable ignored = withLock()) {
                if (attachment == null) {
                    attachment = util.executeCreateDatabase(getStatus(), statementArray.length,
                            statementArray, getConnectionDialect(), new boolean[]{false});
                } else {
                    attachment.execute(getStatus(),
                            transaction != null ? ((ITransactionImpl) transaction).getTransaction() :
                                    attachment.startTransaction(getStatus(), 0, null),
                            statementArray.length,
                            statementArray, getConnectionDialect(), null, null,
                            null, null);
                }
                if (!isAttached()) {
                    setAttached();
                    afterAttachActions();
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int getHandle() {
        throw new UnsupportedOperationException( "Native OO API not support database handle" );
    }

    @Override
    public void setNetworkTimeout(int milliseconds) throws SQLException {
        throw new FBDriverNotCapableException(
                "Setting network timeout not supported in native implementation");
    }

    protected IEventImpl validateEventHandle(EventHandle eventHandle) throws SQLException {
        if (!(eventHandle instanceof IEventImpl)) {
            // TODO SQLState and/or Firebird specific error
            throw new SQLNonTransientException(String.format("Invalid event handle type: %s, expected: %s",
                    eventHandle.getClass(), IEventImpl.class));
        }
        IEventImpl event = (IEventImpl) eventHandle;
        if (event.getSize() == -1) {
            // TODO SQLState and/or Firebird specific error
            throw new SQLTransientException("Event handle hasn't been initialized");
        }
        return event;
    }

    @Override
    public IEventImpl createEventHandle(String eventName, EventHandler eventHandler) throws SQLException {
        final IEventImpl eventHandle = new IEventImpl(eventName, eventHandler, getEncoding());
        try (LockCloseable ignored = withLock()) {
            synchronized (eventHandle) {
                int size = clientLibrary.isc_event_block(eventHandle.getEventBuffer(), eventHandle.getResultBuffer(),
                        (short) 1, eventHandle.getEventNameMemory());
                eventHandle.setSize(size);
            }
        }
        return eventHandle;
    }

    @Override
    public void countEvents(EventHandle eventHandle) throws SQLException {
        try {
            final IEventImpl event = validateEventHandle(eventHandle);
            int count;
            try (LockCloseable ignored = withLock()) {
                synchronized (event) {
                    ISC_STATUS[] status = new ISC_STATUS[20];
                    clientLibrary.isc_event_counts(status, (short) event.getSize(),
                            event.getEventBuffer().getValue(), event.getResultBuffer().getValue());
                    count = status[0].intValue();
                }
            }
            event.setEventCount(count);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void queueEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkConnected();
            final IEventImpl event = validateEventHandle(eventHandle);

            try (LockCloseable ignored = withLock()) {
                synchronized (event) {
                    int length = event.getSize();
                    byte[] array = event.getEventBuffer().getValue().getByteArray(0, length);
                    IEvents iEvents = attachment.queEvents(getStatus(), event.getCallback(),
                            length,
                            array);
                    events.put(eventHandle, iEvents);
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void cancelEvent(EventHandle eventHandle) throws SQLException {
        try {
            checkConnected();
            final IEventImpl event = validateEventHandle(eventHandle);

            try (LockCloseable ignored = withLock()) {
                synchronized (event) {
                    try {
                        IEvents iEvents = events.remove(eventHandle);
                        iEvents.cancel(getStatus());
                    } finally {
                        event.releaseMemory();
                    }
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public FbBatch createBatch(FbTransaction transaction, String statement, FbMessageMetadata metadata,
                               BatchParameterBuffer parameters) throws SQLException {
        return new IBatchImpl(this, transaction, statement, metadata, parameters);
    }

    @Override
    public FbBatch createBatch(FbTransaction transaction, String statement, BatchParameterBuffer parameters)
            throws SQLException {
        return new IBatchImpl(this, transaction, statement, parameters);
    }

    @Override
    public FbMetadataBuilder getMetadataBuilder(int fieldCount) throws SQLException {
        return new IMetadataBuilderImpl(this, fieldCount);
    }


    @Override
    protected void checkConnected() throws SQLException {
        if (!isAttached()) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_notAttachedToDatabase)
                    .toFlatSQLException();
        }
    }

    protected void attachOrCreate(final DatabaseParameterBuffer dpb, final boolean create) throws SQLException {
        if (isAttached()) {
            throw new SQLException("Already attached to a database");
        }
        final String dbName = connection.getAttachUrl();
        final byte[] dpbArray = dpb.toBytesWithType();

        try (LockCloseable ignored = withLock()) {
            try {
                if (create) {
                    attachment = provider.createDatabase(getStatus(), dbName, (short) dpbArray.length, dpbArray);
                } else {
                    attachment = provider.attachDatabase(getStatus(), dbName, (short) dpbArray.length, dpbArray);
                }
                processStatus();
            } catch (SQLException e) {
                safelyDetach();
                throw e;
            }
            setAttached();
            afterAttachActions();
        }
    }

    protected void afterAttachActions() throws SQLException {
        getDatabaseInfo(getDescribeDatabaseInfoBlock(), 1024, getDatabaseInformationProcessor());
    }

    public IMaster getMaster() {
        return master;
    }

    public IStatus getStatus() {
        status.clear();
        return status;
    }

    public IAttachment getAttachment() {
        return attachment;
    }

    private void processStatus() throws SQLException {
        processStatus(status, getDatabaseWarningCallback());
    }

    public void processStatus(IStatus status, WarningMessageCallback warningMessageCallback)
            throws SQLException {
        if (warningMessageCallback == null) {
            warningMessageCallback = getDatabaseWarningCallback();
        }
        connection.processStatus(status, warningMessageCallback);
    }
}
