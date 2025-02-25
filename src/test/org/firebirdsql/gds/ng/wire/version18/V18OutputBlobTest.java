// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.wire.version16.V16OutputBlobTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob} in the version 18 protocol
 * (note: there is no version 18 specific implementation of this class).
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class V18OutputBlobTest extends V16OutputBlobTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(18);

    @Override
    protected V18CommonConnectionInfo commonConnectionInfo() {
        return new V18CommonConnectionInfo();
    }

}
