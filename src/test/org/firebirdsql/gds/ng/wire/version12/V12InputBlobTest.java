// SPDX-FileCopyrightText: Copyright 2014-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version11.V11InputBlobTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob} in the version 12 protocol
 * (note: there is no version 12 specific implementation of this class).
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V12InputBlobTest extends V11InputBlobTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(12);

    @Override
    protected V12CommonConnectionInfo commonConnectionInfo() {
        return new V12CommonConnectionInfo();
    }
    
}
