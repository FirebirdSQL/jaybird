// SPDX-FileCopyrightText: Copyright 2019-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version15;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version13.V13StatementTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version13.V13Statement} in the V15 protocol, reuses test for V13.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V15StatementTest extends V13StatementTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(15);

    protected V15CommonConnectionInfo commonConnectionInfo() {
        return new V15CommonConnectionInfo();
    }

}
