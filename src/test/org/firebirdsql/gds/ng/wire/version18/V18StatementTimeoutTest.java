// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version16.V16StatementTimeoutTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link V18Statement} timeouts in the V18 protocol, reuses tests for V16.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class V18StatementTimeoutTest extends V16StatementTimeoutTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(18);

    protected V18CommonConnectionInfo commonConnectionInfo() {
        return new V18CommonConnectionInfo();
    }

}
