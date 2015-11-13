/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10CommonConnectionInfo;
import org.firebirdsql.gds.ng.wire.version10.V10Service;

/**
 * Class to contain common connection information shared by the V11 tests.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
