// SPDX-FileCopyrightText: Copyright 2014-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version10.V10TransactionTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Transaction} in the version 11 protocol
 * (note: there is no version 11 specific implementation of this class).
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V11TransactionTest extends V10TransactionTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(11);

    protected V11CommonConnectionInfo commonConnectionInfo() {
        return new V11CommonConnectionInfo();
    }

}
