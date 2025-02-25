// SPDX-FileCopyrightText: Copyright 2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version12.V12CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V13 tests.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V13CommonConnectionInfo extends V12CommonConnectionInfo {

    V13CommonConnectionInfo() {
        this(new Version13Descriptor(), V13Database.class, V10Service.class);
    }

    public V13CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }
}
