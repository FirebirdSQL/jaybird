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
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.V10Service;
import org.firebirdsql.gds.ng.wire.version16.V16CommonConnectionInfo;

/**
 * Class to contain common connection information shared by the V18 tests.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
