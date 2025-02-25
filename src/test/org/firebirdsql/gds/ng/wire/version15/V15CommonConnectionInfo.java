// SPDX-FileCopyrightText: Copyright 2019 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version15;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version13.V13CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V15 tests.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V15CommonConnectionInfo extends V13CommonConnectionInfo {

    V15CommonConnectionInfo() {
        this(new Version15Descriptor(), V15Database.class, V10Service.class);
    }

    public V15CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }
}
