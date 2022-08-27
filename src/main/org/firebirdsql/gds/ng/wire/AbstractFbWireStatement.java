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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractFbStatement;
import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireStatement extends AbstractFbStatement implements FbWireStatement {

    private final Map<RowDescriptor, byte[]> blrCache = Collections.synchronizedMap(new WeakHashMap<>());
    private volatile int handle = WireProtocolConstants.INVALID_OBJECT;
    private FbWireDatabase database;

    public AbstractFbWireStatement(FbWireDatabase database) {
        super(database.getSynchronizationObject());
        this.database = database;
    }

    /**
     * Gets the XdrInputStream.
     *
     * @return Instance of XdrInputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the InputStream
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * Gets the XdrOutputStream.
     *
     * @return Instance of XdrOutputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the OutputStream
     */
    protected final XdrOutputStream getXdrOut() throws SQLException {
        return getXdrStreamAccess().getXdrOut();
    }

    private XdrStreamAccess getXdrStreamAccess() throws SQLException {
        if (database != null) {
            return database.getXdrStreamAccess();
        } else {
            throw new SQLException("Connection closed or no connection available");
        }
    }

    @Override
    public final FbWireDatabase getDatabase() {
        return database;
    }

    @Override
    public final int getHandle() {
        return handle;
    }

    protected final void setHandle(int handle) {
        this.handle = handle;
    }

    /**
     * Returns the (possibly cached) blr byte array for a {@link RowDescriptor}, or <code>null</code> if the parameter is null.
     *
     * @param rowDescriptor
     *         The row descriptor.
     * @return blr byte array or <code>null</code> when <code>rowDescriptor</code> is <code>null</code>
     * @throws SQLException
     *         When the {@link RowDescriptor} contains an unsupported field type.
     */
    protected final byte[] calculateBlr(RowDescriptor rowDescriptor) throws SQLException {
        if (rowDescriptor == null) return null;
        byte[] blr = blrCache.get(rowDescriptor);
        if (blr == null) {
            blr = getBlrCalculator().calculateBlr(rowDescriptor);
            blrCache.put(rowDescriptor, blr);
        }
        return blr;
    }

    /**
     * Returns the blr byte array for a {@link RowValue}, or <code>null</code> if the parameter is null.
     * <p>
     * Contrary to {@link #calculateBlr(org.firebirdsql.gds.ng.fields.RowDescriptor)}, it is not allowed
     * to cache this value as it depends on the actual row value.
     * </p>
     *
     * @param rowValue
     *         The row value.
     * @return blr byte array or <code>null</code> when <code>rowValue</code> is <code>null</code>
     * @throws SQLException
     *         When the {@link RowValue} contains an unsupported field type.
     */
    protected final byte[] calculateBlr(RowDescriptor rowDescriptor, RowValue rowValue) throws SQLException {
        if (rowDescriptor == null || rowValue == null) return null;
        return getBlrCalculator().calculateBlr(rowDescriptor, rowValue);
    }

    /**
     * @return The {@link BlrCalculator} instance for this statement (currently always the one from
     * the {@link FbWireDatabase} instance).
     */
    protected final BlrCalculator getBlrCalculator() {
        return getDatabase().getBlrCalculator();
    }

    @Override
    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            // TODO Preferably this should be done elsewhere and AbstractFbStatement.close() should be final
            synchronized (getSynchronizationObject()) {
                database = null;
                blrCache.clear();
            }
        }
    }

    @Override
    protected boolean isValidTransactionClass(Class<? extends FbTransaction> transactionClass) {
        return FbWireTransaction.class.isAssignableFrom(transactionClass);
    }

    @Override
    public final RowDescriptor emptyRowDescriptor() {
        return database.emptyRowDescriptor();
    }

    @Override
    public byte[] getSqlInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try {
            checkStatementValid();
            return getInfo(WireProtocolConstants.op_info_sql, requestItems, bufferLength);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected byte[] getInfo(int operation, byte[] requestItems, int bufferLength) throws SQLException {
        return getDatabase().getInfo(operation, getHandle(), requestItems, bufferLength, getStatementWarningCallback());
    }

    /**
     * Wraps a deferred response to produce a deferred action that can be added
     * using {@link FbWireDatabase#enqueueDeferredAction(DeferredAction)}, notifying the exception listener of this
     * statement for exceptions, and forcing the ERROR state for IO errors.
     *
     * @param deferredResponse
     *         deferred response to wrap
     * @param responseMapper
     *         Function to map a {@link Response} to the response object expected by the deferred response
     * @param <T>
     *         type of deferred response
     * @return deferred action
     */
    protected final <T> DeferredAction wrapDeferredResponse(DeferredResponse<T> deferredResponse,
            Function<Response, T> responseMapper) {
        return DeferredAction.wrapDeferredResponse(
                deferredResponse, responseMapper, getStatementWarningCallback(), this::deferredExceptionHandler);
    }

    /**
     * Handler for exceptions to a deferred response.
     * <p>
     * If the exception is a {@code SQLException}, the exception listener dispatcher is notified. If the exception
     * or its cause is an {@code IOException}, the statement state is forced to {@code ERROR}.
     * </p>
     *
     * @param exception exception received in a deferred response, or thrown while receiving the deferred response
     */
    private void deferredExceptionHandler(Exception exception) {
        if (exception instanceof SQLException) {
            exceptionListenerDispatcher.errorOccurred((SQLException) exception);
        }
        if (exception instanceof IOException || exception.getCause() instanceof IOException) {
            forceState(StatementState.ERROR);
        }
    }
}
