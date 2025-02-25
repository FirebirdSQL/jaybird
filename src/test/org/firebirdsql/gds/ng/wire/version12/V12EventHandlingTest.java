// SPDX-FileCopyrightText: Copyright 2015-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version11.V11EventHandlingTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for events in {@link org.firebirdsql.gds.ng.wire.version12.V12Database}, reuses test for V11.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V12EventHandlingTest extends V11EventHandlingTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(12);

    protected V12CommonConnectionInfo commonConnectionInfo() {
        return new V12CommonConnectionInfo();
    }

}
