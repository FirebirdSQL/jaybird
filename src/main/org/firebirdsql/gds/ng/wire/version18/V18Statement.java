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
                    throw FbExceptionBuilder.forException(ISCConstants.isc_cancelled).toSQLException();
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
        xdrOut.writeInt(WireProtocolConstants.op_fetch_scroll);
        xdrOut.writeInt(getHandle());
        xdrOut.writeBuffer(hasFetched() ? null : calculateBlr(getRowDescriptor()));
        xdrOut.writeInt(0); // out_message_number = out_message_type
        xdrOut.writeInt(fetchSize); // fetch size
        xdrOut.writeInt(fetchType.getFbFetchType()); // p_sqldata_fetch_op
        xdrOut.writeInt(position); // p_sqldata_fetch_pos
    }

    @Override
    protected byte[] getCursorInfoImpl(byte[] requestItems, int bufferLength) throws SQLException {
        if (!hasFetched()) {
            // Attempting to retrieve cursor information when cursor is not open causes SQLDA errors on fetch.
            // This is an attempt to protect against that problem by disallowing such requests.
            throw FbExceptionBuilder.forTransientException(ISCConstants.isc_cursor_not_open).toSQLException();
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
