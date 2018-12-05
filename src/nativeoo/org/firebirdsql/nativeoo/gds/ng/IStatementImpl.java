package org.firebirdsql.nativeoo.gds.ng;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.*;
import org.firebirdsql.gds.ng.jna.JnaDatabase;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;
import org.firebirdsql.jna.fbclient.XSQLVAR;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbStatement} for native client access using OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IStatementImpl extends AbstractFbStatement {

    private static final Logger log = LoggerFactory.getLogger(IStatementImpl.class);

    private final IDatabaseImpl database;
    private final IStatus status;
    private IStatement statement;
    private IResultSet cursor;
    private IMessageMetadata inMeta;
    private IMessageMetadata outMeta;
    private ByteBuffer inMessage;

    public IStatementImpl(IDatabaseImpl database) {
        super(database.getSynchronizationObject());
        this.database = database;
        this.status = this.database.getStatus();
    }

    @Override
    protected void free(int option) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (option == ISCConstants.DSQL_close)
                cursor.close(getStatus());
            else if (statement != null)
                statement.free(getStatus());
            processStatus();
            // Reset statement information
            reset(option == ISCConstants.DSQL_drop);
        }
    }

    @Override
    protected boolean isValidTransactionClass(Class<? extends FbTransaction> transactionClass) {
        return ITransactionImpl.class.isAssignableFrom(transactionClass);
    }

    @Override
    public IDatabaseImpl getDatabase() {
        return database;
    }

    @Override
    public int getHandle() {
        throw new UnsupportedOperationException( "Native OO API not support statement handle" );
    }

    @Override
    public void prepare(String statementText) throws SQLException {
        try {
            final byte[] statementArray = getDatabase().getEncoding().encodeToCharset(statementText);
            if (statementArray.length > JnaDatabase.MAX_STATEMENT_LENGTH) {
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_maxStatementLengthExceeded)
                        .messageParameter(JnaDatabase.MAX_STATEMENT_LENGTH)
                        .messageParameter(statementArray.length)
                        .toFlatSQLException();
            }
            synchronized (getSynchronizationObject()) {
                checkTransactionActive(getTransaction());
                final StatementState currentState = getState();
                if (!isPrepareAllowed(currentState)) {
                    throw new SQLNonTransientException(String.format("Current statement state (%s) does not allow call to prepare", currentState));
                }
                resetAll();

                if (currentState == StatementState.NEW) {
                    // allocated when prepare call
                    switchState(StatementState.ALLOCATED);
                    setType(StatementType.NONE);
                } else {
                    checkStatementValid();
                }

                ITransactionImpl transaction = (ITransactionImpl) getTransaction();
                statement = getDatabase().getAttachment().prepare(getStatus(), transaction.getTransaction(), statementText.length(), statementText,
                        getDatabase().getConnectionDialect(), IStatement.PREPARE_PREFETCH_METADATA);
                processStatus();
                outMeta = statement.getOutputMetadata(getStatus());
                processStatus();
                inMeta = statement.getInputMetadata(getStatus());
                processStatus();

                final byte[] statementInfoRequestItems = getStatementInfoRequestItems();
                final int responseLength = getDefaultSqlInfoSize();
                byte[] statementInfo = getSqlInfo(statementInfoRequestItems, responseLength);
                parseStatementInfo(statementInfo);
                switchState(StatementState.PREPARED);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void execute(RowValue parameters) throws SQLException {
        final StatementState initialState = getState();
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                checkTransactionActive(getTransaction());
                validateParameters(parameters);
                reset(false);

                switchState(StatementState.EXECUTING);

                setMetaData(getParameterDescriptor(), parameters);

                final StatementType statementType = getType();
                final boolean hasSingletonResult = hasSingletonResult();
                ITransactionImpl transaction = (ITransactionImpl) getTransaction();

                Pointer inPtr = null;
                if (inMessage.array().length > 0) {
                    inPtr = new Memory(inMessage.array().length);
                    inPtr.write(0, inMessage.array(), 0, inMessage.array().length);
                }
                Pointer outPtr = null;

                if ((statement.getFlags(getStatus()) & IStatement.FLAG_HAS_CURSOR) == IStatement.FLAG_HAS_CURSOR) {
                    cursor = statement.openCursor(getStatus(), transaction.getTransaction(), inMeta, inPtr, outMeta, 0);
                } else {
                    ByteBuffer outMessage = ByteBuffer.allocate(getMaxSqlInfoSize());
                    outPtr = new Memory(outMessage.array().length);
                    outPtr.write(0, outMessage.array(), 0, outMessage.array().length);
                    statement.execute(getStatus(), transaction.getTransaction(), inMeta, inPtr, outMeta, outPtr);
                }
                processStatus();

                if (hasSingletonResult) {
                    /* A type with a singleton result (ie an execute procedure with return fields), doesn't actually
                     * have a result set that will be fetched, instead we have a singleton result if we have fields
                     */
                    statementListenerDispatcher.statementExecuted(this, false, true);
                    queueRowData(toRowValue(getFieldDescriptor(), outMeta, outPtr));
                    setAllRowsFetched(true);
                } else {
                    // A normal execute is never a singleton result (even if it only produces a single result)
                    statementListenerDispatcher.statementExecuted(this, hasFields(), false);
                }

                if (getState() != StatementState.ERROR) {
                    switchState(statementType.isTypeWithCursor() ? StatementState.CURSOR_OPEN : StatementState.PREPARED);
                }
            }
        } catch (SQLException e) {
            if (getState() != StatementState.ERROR) {
                switchState(initialState);
            }
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected void setMetaData(final RowDescriptor rowDescriptor, final RowValue parameters) throws SQLException {

        IMaster master = database.getMaster();
        IMetadataBuilder metadataBuilder = master.getMetadataBuilder(getStatus(), parameters.getCount());
        inMessage = ByteBuffer.allocate(inMeta.getMessageLength(getStatus()));
        processStatus();
        int offset;
        byte[] nullShort = {0, 0};
        int align = 0;

        for (int idx = 0; idx < parameters.getCount(); idx++) {

            byte[] fieldData = parameters.getFieldData(idx);
            if (fieldData == null) {
                // Note this only works because we mark the type as nullable in allocateXSqlDa

            } else {
                final FieldDescriptor fieldDescriptor = rowDescriptor.getFieldDescriptor(idx);
                // clear status
                getStatus();
                offset = inMeta.getOffset(status, idx) - align;
                int nullOffset = inMeta.getNullOffset(status, idx);
                inMessage.position(offset);
                if (fieldDescriptor.isVarying()) {
                    metadataBuilder.setLength(status, idx, Math.min(fieldDescriptor.getLength(), fieldData.length));
                    metadataBuilder.setType(status, idx, ISCConstants.SQL_VARYING);
                    metadataBuilder.setScale(status, idx, inMeta.getScale(status, idx));
                    metadataBuilder.setSubType(status, idx, inMeta.getSubType(status, idx));
                    metadataBuilder.setCharSet(status, idx, inMeta.getCharSet(status, idx));
                    byte[] encodeShort = fieldDescriptor.getDatatypeCoder().encodeShort(fieldData.length);
                    inMessage.put(encodeShort);
                    offset += encodeShort.length;
                    inMessage.position(offset);
                    align += inMeta.getLength(status, idx) - fieldData.length;
                } else if (fieldDescriptor.isFbType(ISCConstants.SQL_TEXT)) {
                    metadataBuilder.setLength(status, idx, Math.min(fieldDescriptor.getLength(), fieldData.length));
                    metadataBuilder.setType(status, idx, ISCConstants.SQL_TEXT);
                    metadataBuilder.setScale(status, idx, inMeta.getScale(status, idx));
                    metadataBuilder.setSubType(status, idx, inMeta.getSubType(status, idx));
                    metadataBuilder.setCharSet(status, idx, inMeta.getCharSet(status, idx));
                    align += inMeta.getLength(status, idx) - fieldData.length;
                } else {
                    metadataBuilder.setLength(status, idx, inMeta.getLength(status, idx));
                    metadataBuilder.setType(status, idx, fieldDescriptor.getType());
                    metadataBuilder.setSubType(status, idx, fieldDescriptor.getSubType());
                    metadataBuilder.setSubType(status, idx, inMeta.getSubType(status, idx));
                    metadataBuilder.setCharSet(status, idx, inMeta.getCharSet(status, idx));
                }
                processStatus();
                // clear status
                getStatus();
                inMessage.put(fieldData);
                inMessage.position(nullOffset - align);
                inMessage.put(nullShort);
            }
        }
        
        inMeta = metadataBuilder.getMetadata(getStatus());
        processStatus();
        metadataBuilder.release();
    }

    @Override
    public void fetchRows(int fetchSize) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                if (!getState().isCursorOpen()) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_cursor_not_open).toSQLException();
                }
                if (isAllRowsFetched()) return;

                ByteBuffer message = ByteBuffer.allocate(outMeta.getMessageLength(getStatus()) + 1);
                processStatus();
                Pointer ptr = new Memory(message.array().length);
//                ptr.write(0, message.array(), 0, message.array().length);
                int fetchStatus = cursor.fetchNext(getStatus(), ptr);
                processStatus();
                if (fetchStatus == IStatus.RESULT_OK) {
                    queueRowData(toRowValue(getFieldDescriptor(), outMeta, ptr));
                } else if (fetchStatus == IStatus.RESULT_NO_DATA) {
                    setAllRowsFetched(true);
                    // Note: we are not explicitly 'closing' the cursor here
                } else {
                    final String errorMessage = "Unexpected fetch status (expected 0 or 100): " + fetchStatus;
                    log.error(errorMessage);
                    throw new SQLException(errorMessage);
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected RowValue toRowValue(RowDescriptor rowDescriptor, IMessageMetadata meta, Pointer ptr) throws SQLException {
        final RowValueBuilder row = new RowValueBuilder(rowDescriptor);
        int columns = meta.getCount(getStatus());
        processStatus();
        for (int idx = 0; idx < columns; idx++) {
            row.setFieldIndex(idx);
            int nullOffset = meta.getNullOffset(getStatus(), idx);
            processStatus();
            if (ptr.getShort(nullOffset) == XSQLVAR.SQLIND_NULL) {
                row.set(null);
            } else {
                int bufferLength = meta.getLength(getStatus(), idx);
                processStatus();
                int offset = meta.getOffset(getStatus(), idx);
                processStatus();
                if (rowDescriptor.getFieldDescriptor(idx).isVarying()) {
                    bufferLength = ptr.getShort(offset) & 0xffff;
                    offset += 2;
                }
                byte[] data = new byte[bufferLength];
                ptr.read(offset, data, 0, bufferLength);
                row.set(data);
            }
        }
        return row.toRowValue(false);
    }

    @Override
    public byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            final byte[] responseArr = new byte[bufferLength];
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                statement.getInfo(getStatus(), requestItems.length, requestItems,
                        bufferLength, responseArr);
                processStatus();
            }
            return responseArr;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int getDefaultSqlInfoSize() {
        // TODO Test for an optimal buffer size
        return getMaxSqlInfoSize();
    }

    @Override
    public int getMaxSqlInfoSize() {
        // TODO check this
        return 65535;
    }

    @Override
    public void setCursorName(String cursorName) throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                statement.setCursorName(getStatus(), cursorName + '\0');
                processStatus();
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public RowDescriptor emptyRowDescriptor() {
        return database.emptyRowDescriptor();
    }

    @Override
    public FbBatch createBatch(BatchParameterBuffer parameters) throws SQLException {
        IBatch batch = statement.createBatch(getStatus(),
                inMeta, parameters.toBytesWithType().length, parameters.toBytesWithType());
        return new IBatchImpl(batch, this, parameters);
    }

    public FbMessageMetadata getInputMetadata() throws SQLException {
        return new IMessageMetadataImpl(database, inMeta);
    }

    private IStatus getStatus() {
        status.init();
        return status;
    }

    private void processStatus() throws SQLException {
        getDatabase().processStatus(status, getStatementWarningCallback());
    }
}
