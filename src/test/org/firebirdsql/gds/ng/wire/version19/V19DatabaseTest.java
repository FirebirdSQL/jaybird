// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version19;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version18.V18DatabaseTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link V19Database}, reuses test for V18.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public class V19DatabaseTest extends V18DatabaseTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(19);

    protected V19CommonConnectionInfo commonConnectionInfo() {
        return new V19CommonConnectionInfo();
    }

}
