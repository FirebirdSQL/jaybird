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
package org.firebirdsql.gds.ng.wire.version17;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FetchType;
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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class V17Statement extends V16Statement {

    private final Set<CursorFlag> cursorFlags = EnumSet.noneOf(CursorFlag.class);

    /**
     * Creates a new instance of V17Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V17Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    protected void sendExecute(int operation, RowValue parameters) throws IOException, SQLException {
        super.sendExecute(operation, parameters);
        getXdrOut().writeInt(getCursorFlagsAsInt()); // p_sqldata_cursor_flags
    }

    @Override
    public void fetchScroll(FetchType fetchType, int fetchSize, int position) throws SQLException {
//        if (fetchType == FetchType.NEXT) {
//            fetchRows(fetchSize);
//            return;
//        }
        try {
            synchronized (getSynchronizationObject()) {
                checkStatementValid();
                if (!getState().isCursorOpen()) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_cursor_not_open).toSQLException();
                }
                // TODO Check logic for handling fetch after we reached EOF
//                if (isAllRowsFetched()) return;

                try (OperationCloseHandle operationCloseHandle = signalFetch()) {
                    if (operationCloseHandle.isCancelled()) {
                        // operation was synchronously cancelled from an OperationAware implementation
                        throw FbExceptionBuilder.forException(ISCConstants.isc_cancelled).toFlatSQLException();
                    }
                    try {
                        int actualFetchSize = fetchType.supportsBatch() ? fetchSize : 1;
                        sendFetchScroll(fetchType, actualFetchSize, position);
                        getXdrOut().flush();
                    } catch (IOException ex) {
                        switchState(StatementState.ERROR);
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ex).toSQLException();
                    }
                    try {
                        processFetchResponse();
                    } catch (IOException ex) {
                        switchState(StatementState.ERROR);
                        throw new FbExceptionBuilder().exception(ISCConstants.isc_net_read_err).cause(ex).toSQLException();
                    }
                }
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    protected void sendFetchScroll(FetchType fetchType, int fetchSize, int position) throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(WireProtocolConstants.op_fetch_scroll);
        xdrOut.writeInt(getHandle());
        xdrOut.writeBuffer(calculateBlr(getRowDescriptor()));
        xdrOut.writeInt(0); // out_message_number = out_message_type
        xdrOut.writeInt(fetchSize); // fetch size
        xdrOut.writeInt(fetchType.getFbFetchType()); // p_sqldata_fetch_op
        // TODO Will change in next build of Firebird
        if (fetchType == FetchType.ABSOLUTE || fetchType == FetchType.RELATIVE) {
            xdrOut.writeInt(position); // p_sqldata_fetch_pos
        }
    }

    protected final int getCursorFlagsAsInt() {
        int flags = 0;
        for (CursorFlag flag : cursorFlags) {
            flags |= flag.flagValue();
        }
        return flags;
    }

    @Override
    public final void setCursorFlag(CursorFlag flag) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            cursorFlags.add(flag);
        }
    }

    @Override
    public final void clearCursorFlag(CursorFlag flag) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            cursorFlags.remove(flag);
        }
    }

    @Override
    public final boolean isCursorFlagSet(CursorFlag flag) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkStatementValid();
            return cursorFlags.contains(flag);
        }
    }
}
