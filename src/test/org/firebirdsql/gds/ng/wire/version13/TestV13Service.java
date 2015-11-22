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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.gds.ng.wire.version12.TestV12Service;
import org.junit.ClassRule;

import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Service} in the V13 protocol.
 * <p>
 * TODO We will probably need a V13Service implementation
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV13Service extends TestV12Service {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(13);

    public TestV13Service() {
        this(new V13CommonConnectionInfo());
    }

    protected TestV13Service(V13CommonConnectionInfo commonConnectionInfo) {
        super(commonConnectionInfo);
    }
}
