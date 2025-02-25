// SPDX-FileCopyrightText: Copyright 2015-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version12.V12TransactionTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Transaction} in the version 13 protocol
 * (note: there is no version 13 specific implementation of this class).
 *
 * @author Mark Rotteveel
 */
public class V13TransactionTest extends V12TransactionTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(13);

    protected V13CommonConnectionInfo commonConnectionInfo() {
        return new V13CommonConnectionInfo();
    }

}
