// SPDX-FileCopyrightText: Copyright 2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version11.V11CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V12 tests.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V12CommonConnectionInfo extends V11CommonConnectionInfo {

    V12CommonConnectionInfo() {
        this(new Version12Descriptor(), V12Database.class, V10Service.class);
    }

    public V12CommonConnectionInfo(ProtocolDescriptor protocolDescriptor,
            Class<? extends FbWireDatabase> expectedDatabaseType, Class<? extends FbWireService> expectedServiceType) {
        super(protocolDescriptor, expectedDatabaseType, expectedServiceType);
    }
}
