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

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.BatchCompletion;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.BatchCompletionResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version15.V15WireOperations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_ping;

/**
 * @author Mark Rotteveel
 * @since 5
 */
public class V16WireOperations extends V15WireOperations {

    protected static final int BATCH_LIMIT = 64;

    public V16WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    protected void afterEnqueueDeferredAction() throws SQLException {
        if (deferredActionCount() < BATCH_LIMIT) return;
        this.completeDeferredActions();
    }

    @Override
    public void completeDeferredActions() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (completeDeferredActionsRequiresSync()) {
                // Some deferred actions, specifically batch operations, will not send responses unless the server is
                // forced by a ping or batch sync
                try {
                    XdrOutputStream xdrOut = getXdrOut();
                    xdrOut.writeInt(getBatchSyncOperation());
                    xdrOut.flush();
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioWriteError(e);
                }
                try {
                    readResponse(null);
                } catch (IOException e) {
                    throw FbExceptionBuilder.ioReadError(e);
                }
            } else {
                processDeferredActions();
            }
        }
    }

    /**
     * @return operation code that synchronizes deferred batch operations ({@code op_ping} or {@code op_batch_sync})
     */
    protected int getBatchSyncOperation() {
        // op_batch_sync was introduced in v17, ping also works, but also checks for errors server side
        // v18 implementation returns op_batch_sync
        return op_ping;
    }

    @Override
    protected void afterProcessDeferredActions(int processedDeferredActions) {
        /* If we reached BATCH_LIMIT, then likely we will receive more deferred actions; trimming now would be a waste
           (of memory and CPU) due to GC and reallocation of the list. This may result in the trim never occurring if
           we ever reach BATCH_LIMIT, but any subsequent processing never exceeds 10; we accept that limitation. */
        super.afterProcessDeferredActions(processedDeferredActions < BATCH_LIMIT ? processedDeferredActions : -1);
    }

    protected BatchCompletionResponse readBatchCompletionResponse(XdrInputStream xdrIn) throws SQLException, IOException {
        xdrIn.skipNBytes(4); // skip int: p_batch_statement
        int elementCount = xdrIn.readInt(); // p_batch_reccount
        int updateCountsCount = xdrIn.readInt(); // p_batch_updates
        int detailedErrorsCount = xdrIn.readInt(); // p_batch_vectors
        int simplifiedErrorsCount = xdrIn.readInt(); // p_batch_errors

        int[] updateCounts = new int[updateCountsCount];
        for (int record = 0; record < updateCountsCount; record++) {
            updateCounts[record] = xdrIn.readInt();
        }

        List<BatchCompletion.DetailedError> detailedErrors = new ArrayList<>(detailedErrorsCount);
        for (int i = 0; i < detailedErrorsCount; i++) {
            int element = xdrIn.readInt();
            SQLException error = readStatusVector(xdrIn);
            detailedErrors.add(new BatchCompletion.DetailedError(element, error));
        }

        int[] simplifiedErrors = new int[simplifiedErrorsCount];
        for (int i = 0; i < simplifiedErrorsCount; i++) {
            simplifiedErrors[i] = xdrIn.readInt();
        }

        return new BatchCompletionResponse(
                new BatchCompletion(elementCount, updateCounts, detailedErrors, simplifiedErrors));
    }
}
