// SPDX-FileCopyrightText: Copyright 2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10CommonConnectionInfo;
import org.firebirdsql.gds.ng.wire.version10.V10Service;

/**
 * Class to contain common connection information shared by the V11 tests.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V11CommonConnectionInfo extends V10CommonConnectionInfo {
    V11CommonConnectionInfo() {
        this(new Version11Descriptor(), V11Database.class, V10Service.class);
    }

    public V11CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }
}
