// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FetchType;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.OperationCloseHandle;
import org.firebirdsql.gds.ng.StatementState;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.version16.V16Statement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Mark Rotteveel
 * @since 5
 */
public class V18Statement extends V16Statement {

    private final Set<CursorFlag> cursorFlags = EnumSet.noneOf(CursorFlag.class);

    /**
     * Creates a new instance of V18Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V18Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    protected void sendExecute(int operation, RowValue parameters) throws IOException, SQLException {
        super.sendExecute(operation, parameters);
        getXdrOut().writeInt(getCursorFlagsAsInt()); // p_sqldata_cursor_flags
    }

    @Override
    protected void fetchScrollImpl(FetchType fetchType, int fetchSize, int position) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkStatementHasOpenCursor();

            try (OperationCloseHandle operationCloseHandle = signalFetch()) {
                if (operationCloseHandle.isCancelled()) {
                    // operation was synchronously cancelled from an OperationAware implementation
                    throw FbExceptionBuilder.toException(ISCConstants.isc_cancelled);
                }
                try {
                    // We are allowing 0 and negative fetch sizes here, in case this triggers some server behaviour
                    int actualFetchSize = fetchType.supportsBatch() ? fetchSize : Math.min(1, fetchSize);
                    sendFetchScroll(fetchType, actualFetchSize, position);
                    getXdrOut().flush();
                } catch (IOException e) {
                    switchState(StatementState.ERROR);
                    throw FbExceptionBuilder.ioWriteError(e);
                }
                try {
                    processFetchResponse(fetchType.direction(position));
                } catch (IOException e) {
                    switchState(StatementState.ERROR);
                    throw FbExceptionBuilder.ioReadError(e);
                }
            }
        }
    }

    protected void sendFetchScroll(FetchType fetchType, int fetchSize, int position) throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_fetch_scroll); // p_operation
        xdrOut.writeInt(getHandle()); // p_sqldata_statement
        xdrOut.writeBuffer(hasFetched() ? null : calculateBlr(getRowDescriptor())); // p_sqldata_blr
        xdrOut.writeInt(0); // p_sqldata_message_number
        xdrOut.writeInt(fetchSize); // p_sqldata_messages - fetch size
        xdrOut.writeInt(fetchType.getFbFetchType()); // p_sqldata_fetch_op
        xdrOut.writeInt(position); // p_sqldata_fetch_pos
    }

    @Override
    protected byte[] getCursorInfoImpl(byte[] requestItems, int bufferLength) throws SQLException {
        if (!hasFetched()) {
            // Attempting to retrieve cursor information when cursor is not open causes SQLDA errors on fetch.
            // This is an attempt to protect against that problem by disallowing such requests.
            throw FbExceptionBuilder.toTransientException(ISCConstants.isc_cursor_not_open);
        }
        return getInfo(WireProtocolConstants.op_info_cursor, requestItems, bufferLength);
    }

    @Override
    public boolean supportsFetchScroll() {
        return true;
    }

    @Override
    public boolean supportsCursorInfo() {
        return true;
    }

    protected final int getCursorFlagsAsInt() {
        int flags = 0;
        for (CursorFlag flag : cursorFlags) {
            flags |= flag.flagValue();
        }
        return flags;
    }

    @Override
    public final void setCursorFlag(CursorFlag flag) {
        try (LockCloseable ignored = withLock()) {
            cursorFlags.add(flag);
        }
    }

    @Override
    public final void clearCursorFlag(CursorFlag flag) {
        try (LockCloseable ignored = withLock()) {
            cursorFlags.remove(flag);
        }
    }

    @Override
    public final boolean isCursorFlagSet(CursorFlag flag) {
        try (LockCloseable ignored = withLock()) {
            return cursorFlags.contains(flag);
        }
    }
}
