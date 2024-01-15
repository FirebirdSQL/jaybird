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
import org.firebirdsql.gds.ng.wire.version13.V13Statement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_batch_cancel;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_batch_rls;
import static org.firebirdsql.gds.ng.TransactionHelper.checkTransactionActive;

/**
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V16Statement extends V13Statement {

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
    protected void sendExecute(int operation, RowValue parameters) throws IOException, SQLException {
        super.sendExecute(operation, parameters);
        // timeout is an unsigned 32 bit int
        getXdrOut().writeInt((int) getAllowedTimeout()); // p_sqldata_timeout
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
            getDatabase().enqueueDeferredAction(wrapDeferredResponse(onResponse, r -> null, true));
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

        XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_batch_create);
        xdrOut.writeInt(getHandle()); // p_batch_statement
        xdrOut.writeBuffer(blrMessage); // p_batch_blr
        xdrOut.writeInt(messageLength); // p_batch_msglen
        xdrOut.writeTyped(batchPb); // p_batch_pb
        xdrOut.flush();
    }

    @Override
    public void deferredBatchSend(Collection<RowValue> rowValues, DeferredResponse<Void> onResponse) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementValid();
            DeferredAction deferredAction = wrapDeferredResponse(onResponse, r -> null, true);
            try {
                RowDescriptor parameterDescriptor = getParameterDescriptor();
                XdrOutputStream xdrOut = getXdrOut();
                registerBlobs(xdrOut, parameterDescriptor, rowValues, deferredAction);
                sendBatchMsg(xdrOut, parameterDescriptor, rowValues);
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

    protected void sendBatchMsg(XdrOutputStream xdrOut, RowDescriptor parameterDescriptor,
            Collection<RowValue> rowValues) throws SQLException, IOException {
        BlrCalculator blrCalculator = getBlrCalculator();
        xdrOut.writeInt(WireProtocolConstants.op_batch_msg);
        xdrOut.writeInt(getHandle()); // p_batch_statement
        xdrOut.writeInt(rowValues.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XdrOutputStream rowOut = new XdrOutputStream(baos, 512);
        for (RowValue rowValue : rowValues) {
            baos.reset();
            writeSqlData(rowOut, blrCalculator, parameterDescriptor, rowValue, false);
            rowOut.flush();
            byte[] rowBytes = baos.toByteArray();
            xdrOut.write(rowBytes);
            xdrOut.writeZeroPadding((4 - rowBytes.length) & 3);
        }
        xdrOut.flush();
    }

    private void registerBlobs(XdrOutputStream xdrOut, RowDescriptor parameterDescriptor,
            Collection<RowValue> rowValues, DeferredAction deferredAction) throws SQLException, IOException {
        List<Integer> blobPositions = blobPositions(parameterDescriptor);
        if (blobPositions.isEmpty()) return;
        FbWireDatabase db = getDatabase();
        for (RowValue rowValue : rowValues) {
            for (int position : blobPositions) {
                byte[] fieldData = rowValue.getFieldData(position);
                if (fieldData == null) continue;
                xdrOut.writeInt(WireProtocolConstants.op_batch_regblob);
                xdrOut.writeInt(getHandle()); // p_batch_statement
                // register as itself
                xdrOut.write(fieldData); // p_batch_exist_id
                xdrOut.write(fieldData); // p_batch_blob_id
                db.enqueueDeferredAction(deferredAction);
            }
        }
        xdrOut.flush();
    }

    private List<Integer> blobPositions(RowDescriptor parameterDescriptor) {
        return parameterDescriptor.getFieldDescriptors().stream()
                .filter(f -> f.isFbType(ISCConstants.SQL_BLOB))
                .map(FieldDescriptor::getPosition)
                .toList();
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
            XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(WireProtocolConstants.op_batch_exec);
            xdrOut.writeInt(getHandle());
            xdrOut.writeInt(transaction.getHandle());
            xdrOut.flush();
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioWriteError(e);
        }
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
            getDatabase().enqueueDeferredAction(wrapDeferredResponse(onResponse, r -> null, true));
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private void sendBatchRelease(int operation) throws SQLException {
        assert operation == op_batch_cancel || operation == op_batch_rls
                : "Unexpected operation for batch release: " + operation;
        try {
            XdrOutputStream xdrOut = getXdrOut();
            xdrOut.writeInt(operation);
            xdrOut.writeInt(getHandle());
            xdrOut.flush();
        } catch (IOException e) {
            switchState(StatementState.ERROR);
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

}
