// SPDX-FileCopyrightText: Copyright 2019-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.BatchParameterBufferImp;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.BatchCompletion;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbBatchConfig;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.BatchCompletionResponse;
import org.firebirdsql.gds.ng.wire.DeferredAction;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.Response;
import org.firebirdsql.gds.ng.wire.version13.V13Statement;
import org.firebirdsql.jaybird.util.CollectionUtils;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_batch_cancel;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_batch_rls;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V16Statement extends V13Statement {

    private static final Function<Response, @Nullable Void> NULL_RESPONSE = r -> null;

    /**
     * Creates a new instance of V16Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V16Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    protected void sendExecuteMsg(XdrOutputStream xdrOut, int operation, RowValue parameters)
            throws IOException, SQLException {
        // timeout is an unsigned 32-bit int
        int timeout = (int) getAllowedTimeout();
        super.sendExecuteMsg(xdrOut, operation, parameters);
        xdrOut.writeInt(timeout); // p_sqldata_timeout
    }

    @Override
    public boolean supportBatchUpdates() {
        return true;
    }

    @Override
    public BatchParameterBuffer createBatchParameterBuffer() throws SQLException {
        checkStatementValid();
        return new BatchParameterBufferImp();
    }

    @Override
    public void deferredBatchCreate(FbBatchConfig batchConfig, DeferredResponse<Void> onResponse) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            sendBatchCreate0(batchConfig);
            getDatabase().enqueueDeferredAction(wrapDeferredResponse(onResponse, NULL_RESPONSE, true));
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendBatchCreate0(FbBatchConfig batchConfig) throws SQLException {
        try {
            sendBatchCreate(batchConfig);
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    protected void sendBatchCreate(FbBatchConfig batchConfig) throws SQLException, IOException {
        BlrCalculator blrCalculator = getBlrCalculator();
        RowDescriptor parameterDescriptor = getParameterDescriptor();
        byte[] blrMessage = blrCalculator.calculateBlr(parameterDescriptor);
        int messageLength = blrCalculator.calculateBatchMessageLength(parameterDescriptor);
        BatchParameterBuffer batchPb = createBatchParameterBuffer();
        batchConfig.populateBatchParameterBuffer(batchPb);

        withTransmitLock(xdrOut -> {
            sendBatchCreateMsg(xdrOut, blrMessage, messageLength, batchPb);
            xdrOut.flush();
        });
    }

    /**
     * Sends the batch create message (struct {@code p_batch_create}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param blrMessage
     *         batch message BLR
     * @param messageLength
     *         message length
     * @param batchPb
     *         batch parameter buffer
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendBatchCreateMsg(XdrOutputStream xdrOut, byte[] blrMessage, int messageLength,
            BatchParameterBuffer batchPb) throws IOException {
        xdrOut.writeInt(WireProtocolConstants.op_batch_create); // p_operation
        xdrOut.writeInt(getHandle()); // p_batch_statement
        xdrOut.writeBuffer(blrMessage); // p_batch_blr
        xdrOut.writeInt(messageLength); // p_batch_msglen
        xdrOut.writeTyped(batchPb); // p_batch_pb
    }

    @Override
    public void deferredBatchSend(Collection<RowValue> rowValues, DeferredResponse<Void> onResponse) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            DeferredAction deferredAction = wrapDeferredResponse(onResponse, NULL_RESPONSE, true);
            try {
                RowDescriptor parameterDescriptor = getParameterDescriptor();
                registerBlobs(parameterDescriptor, rowValues, deferredAction);
                withTransmitLock(xdrOut -> sendBatchMsg(xdrOut, parameterDescriptor, rowValues));
            } catch (IOException e) {
                switchState(StatementState.ERROR);
                throw FbExceptionBuilder.ioWriteError(e);
            }
            getDatabase().enqueueDeferredAction(deferredAction);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Sends a batch message and row data to the server.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     */
    private void sendBatchMsg(XdrOutputStream xdrOut, RowDescriptor parameterDescriptor,
            Collection<RowValue> rowValues) throws SQLException, IOException {
        BlrCalculator blrCalculator = getBlrCalculator();
        sendBatchMsg(xdrOut, rowValues.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XdrOutputStream rowOut = new XdrOutputStream(baos, 512);
        for (RowValue rowValue : rowValues) {
            baos.reset();
            writeSqlData(rowOut, blrCalculator, parameterDescriptor, rowValue, false);
            rowOut.flush();
            byte[] rowBytes = baos.toByteArray();
            xdrOut.write(rowBytes);
            // TODO Verify if this padding is necessary, as writeSqlData should already result in multiples of 4.
            //  Possibly this is an interpretation error or reverse-engineering mistake on our side. Commenting it out
            //  does not result in test failures.
            //  If unnecessary, remove this, and write directly to xdrOut instead of using the intermediate baos.
            xdrOut.writeZeroPadding((4 - rowBytes.length) & 3);
        }
        xdrOut.flush();
    }

    /**
     * Sends <em>only</em> the batch message (struct {@code p_batch_msg}) to the server, without flushing.
     * <p>
     * The row data is handled by the caller using
     * {@link #writeSqlData(XdrOutputStream, BlrCalculator, RowDescriptor, RowValue, boolean)} for each row.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param rowCount
     *         number of rows that are sent following this message
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendBatchMsg(XdrOutputStream xdrOut, int rowCount) throws IOException {
        xdrOut.writeInt(WireProtocolConstants.op_batch_msg);
        xdrOut.writeInt(getHandle()); // p_batch_statement
        xdrOut.writeInt(rowCount); // p_batch_messages
    }

    private void registerBlobs(RowDescriptor parameterDescriptor, Collection<RowValue> rowValues,
            DeferredAction deferredAction) throws SQLException, IOException {
        int[] blobPositions = blobPositions(parameterDescriptor);
        if (blobPositions.length == 0) return;
        FbWireDatabase db = getDatabase();
        // Given we register blobs under their own id, we can only register them once, use a Set to track this
        var blobsRegistered = new HashSet<>(CollectionUtils.mapCapacity(rowValues.size() * blobPositions.length));
        for (RowValue rowValue : rowValues) {
            for (int position : blobPositions) {
                byte[] fieldData = rowValue.getFieldData(position);
                if (fieldData == null) continue;
                // Do not register a blob multiple times
                if (!blobsRegistered.add(toLong(fieldData))) continue;

                // Enqueueing the deferred action can perform a blocking read from the server, so we need to obtain and
                // release the transmit lock for each registration individually
                withTransmitLock(xdrOut ->
                        // register blob as itself
                        sendBatchRegBlobMsg(xdrOut, fieldData, fieldData));
                db.enqueueDeferredAction(deferredAction);
            }
        }
        withTransmitLock(OutputStream::flush);
    }

    /**
     * Sends the batch register blob message (struct {@code p_batch_regblob}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param existId
     *         existing blob id
     * @param blobId
     *         blob id used in the batch to refer to {@code existId}
     * @throws IOException
     *         for errors writing to the output stream
     */
    protected void sendBatchRegBlobMsg(XdrOutputStream xdrOut, byte[] existId, byte[] blobId) throws IOException {
        xdrOut.writeInt(WireProtocolConstants.op_batch_regblob);
        xdrOut.writeInt(getHandle()); // p_batch_statement
        xdrOut.write(existId); // p_batch_exist_id
        xdrOut.write(blobId); // p_batch_blob_id
    }

    // This is not necessarily the correct endianness, we just want the id bytes expressed as long to use as a key.
    private static long toLong(byte[] bytes) {
        assert bytes.length == 8 : "expected 8 bytes";
        return ByteBuffer.wrap(bytes).getLong();
    }

    private int[] blobPositions(RowDescriptor parameterDescriptor) {
        return parameterDescriptor.getFieldDescriptors().stream()
                .filter(f -> f.isFbType(ISCConstants.SQL_BLOB))
                .mapToInt(FieldDescriptor::getPosition)
                .toArray();
    }

    @Override
    public BatchCompletion batchExecute() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            FbTransaction transaction = getTransaction();
            checkTransactionActive(transaction);
            sendBatchExec(transaction);
            return receiveBatchExecResponse();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendBatchExec(FbTransaction transaction) throws SQLException {
        try {
            withTransmitLock(xdrOut -> {
                sendBatchExecMsg(xdrOut, transaction);
                xdrOut.flush();
            });
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    /**
     * Sends the execute batch message (struct {@code p_batch_exec}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param transaction
     *         transaction for execution (validity checked by the caller)
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendBatchExecMsg(XdrOutputStream xdrOut, FbTransaction transaction) throws IOException {
        xdrOut.writeInt(WireProtocolConstants.op_batch_exec);
        xdrOut.writeInt(getHandle());
        xdrOut.writeInt(transaction.getHandle());
    }

    private BatchCompletion receiveBatchExecResponse() throws SQLException {
        try {
            BatchCompletionResponse response = (BatchCompletionResponse) getDatabase()
                    .readResponse(getStatementWarningCallback());
            return response.batchCompletion();
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    @Override
    public void batchCancel() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            sendBatchRelease(op_batch_cancel);
            receiveBatchCancelResponse();
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void receiveBatchCancelResponse() throws SQLException {
        try {
            getDatabase().readResponse(getStatementWarningCallback());
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioReadError(e);
        }
    }

    @Override
    public void deferredBatchRelease(DeferredResponse<Void> onResponse) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            sendBatchRelease(op_batch_rls);
            getDatabase().enqueueDeferredAction(wrapDeferredResponse(onResponse, NULL_RESPONSE, true));
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendBatchRelease(int operation) throws SQLException {
        assert operation == op_batch_cancel || operation == op_batch_rls
                : "Unexpected operation for batch release: " + operation;
        try {
            withTransmitLock(xdrOut -> {
                // TODO Duplicates AbstractFbWireDatabase.sendReleaseObjectMsg; rethink this if it needs versioning
                xdrOut.writeInt(operation); // p_operation
                xdrOut.writeInt(getHandle()); // p_rlse_object
                xdrOut.flush();
            });
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

}
