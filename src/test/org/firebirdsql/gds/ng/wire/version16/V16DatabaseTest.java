// SPDX-FileCopyrightText: Copyright 2019-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version15.V15DatabaseTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version16.V16Database}, reuses test for V15.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V16DatabaseTest extends V15DatabaseTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(16);

    protected V16CommonConnectionInfo commonConnectionInfo() {
        return new V16CommonConnectionInfo();
    }

}
