/*
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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.monitor.Operation;

import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * {@link Operation} implementation wrapping {@link FbDatabase}.
 * <p>
 * This implementation will automatically signal completion on {@link #close()}.
 * </p>
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
final class FbDatabaseOperation implements Operation, OperationCloseHandle {

    private final Type type;
    private volatile FbDatabase fbDatabase;
    private volatile boolean cancelled;

    private FbDatabaseOperation(Operation.Type type, FbDatabase fbDatabase) {
        this.type = requireNonNull(type, "type");
        this.fbDatabase = requireNonNull(fbDatabase, "fbDatabase");
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void cancel() throws SQLException {
        final FbDatabase current = this.fbDatabase;
        if (current == null) {
            throw FbExceptionBuilder
                    .forException(JaybirdErrorCodes.jb_operationClosed)
                    .messageParameter("cancel")
                    .toFlatSQLException();
        }
        cancelled = true;
        current.cancelOperation(ISCConstants.fb_cancel_raise);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Close this operation and signal 'end-of-operation'.
     * <p>
     * Closing will prevent further actions on this operation like cancellation.
     * </p>
     */
    @Override
    public void close() {
        final FbDatabase previous = fbDatabase;
        fbDatabase = null;
        if (previous != null) {
            OperationMonitor.endOperation(this);
        }
    }

    static OperationCloseHandle signalExecute(FbDatabase fbDatabase) {
        return signalOperation(fbDatabase, Type.STATEMENT_EXECUTE);
    }

    static OperationCloseHandle signalFetch(FbDatabase fbDatabase) {
        return signalOperation(fbDatabase, Type.STATEMENT_FETCH);
    }

    private static OperationCloseHandle signalOperation(FbDatabase fbDatabase, Type statementExecute) {
        FbDatabaseOperation fbDatabaseOperation = new FbDatabaseOperation(statementExecute, fbDatabase);
        OperationMonitor.startOperation(fbDatabaseOperation);
        return fbDatabaseOperation;
    }

}
