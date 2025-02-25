// SPDX-FileCopyrightText: Copyright 2015-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version12.V12WireOperationsTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V13WireOperationsTest extends V12WireOperationsTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(13);

    protected V13CommonConnectionInfo commonConnectionInfo() {
        return new V13CommonConnectionInfo();
    }

}
