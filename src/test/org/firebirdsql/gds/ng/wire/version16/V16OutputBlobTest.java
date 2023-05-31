/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version15.V15OutputBlobTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob} in the version 16 protocol
 * (note: there is no version 16 specific implementation of this class).
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V16OutputBlobTest extends V15OutputBlobTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(16);

    @Override
    protected V16CommonConnectionInfo commonConnectionInfo() {
        return new V16CommonConnectionInfo();
    }
    
}
