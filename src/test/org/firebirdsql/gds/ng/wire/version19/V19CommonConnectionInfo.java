// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version19;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version18.V18CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V19 tests.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public class V19CommonConnectionInfo extends V18CommonConnectionInfo {

    V19CommonConnectionInfo() {
        this(new Version19Descriptor(), V19Database.class, V10Service.class);
    }

    public V19CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }

}
