// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.version16.V16WireOperations;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.op_batch_sync;

/**
 * @author Mark Rotteveel
 * @since 6
 */
public class V18WireOperations extends V16WireOperations {

    public V18WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    protected int getBatchSyncOperation() {
        return op_batch_sync;
    }
}
