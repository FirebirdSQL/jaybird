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
import org.firebirdsql.gds.ng.BatchCompletion;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.BatchCompletionResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version15.V15WireOperations;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class V16WireOperations extends V15WireOperations {

    public V16WireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
        super(connection, defaultWarningMessageCallback, syncObject);
    }

    protected BatchCompletionResponse readBatchCompletionResponse(XdrInputStream xdrIn) throws SQLException, IOException {
        xdrIn.readInt(); // p_batch_statement (ignored)
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
