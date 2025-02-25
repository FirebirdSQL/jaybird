// SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version16.V16CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V18 tests.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class V18CommonConnectionInfo extends V16CommonConnectionInfo {

    V18CommonConnectionInfo() {
        this(new Version18Descriptor(), V18Database.class, V10Service.class);
    }

    public V18CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }
}
