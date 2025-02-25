// SPDX-FileCopyrightText: Copyright 2019-2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version15.V15CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V16 tests.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V16CommonConnectionInfo extends V15CommonConnectionInfo {

    V16CommonConnectionInfo() {
        this(new Version16Descriptor(), V16Database.class, V10Service.class);
    }

    public V16CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }
}
