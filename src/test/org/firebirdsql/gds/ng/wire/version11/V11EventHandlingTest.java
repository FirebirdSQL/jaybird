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

import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.gds.ng.wire.version10.V10EventHandlingTest;
import org.junit.ClassRule;

import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;

/**
 * Tests for events in {@link org.firebirdsql.gds.ng.wire.version11.V11Database}, reuses test for V10.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V11EventHandlingTest extends V10EventHandlingTest {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(11);

    public V11EventHandlingTest() {
        this(new V11CommonConnectionInfo());
    }

    protected V11EventHandlingTest(V11CommonConnectionInfo commonConnectionInfo) {
        super(commonConnectionInfo);
    }
}
