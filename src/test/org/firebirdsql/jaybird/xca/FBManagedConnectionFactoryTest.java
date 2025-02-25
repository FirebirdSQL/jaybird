// SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
// SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;

import static org.firebirdsql.common.FBTestProperties.createDefaultMcf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FBManagedConnectionFactoryTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @Test
    void testCreateMcf() {
        assertDoesNotThrow(() -> createDefaultMcf());
    }

    @Test
    void testCreateMc() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = assertDoesNotThrow(() -> mcf.createManagedConnection());
        mc.destroy();
    }

    /**
     * Test if default isolation level is Connection.TRANSACTION_READ_COMMITTED
     */
    @Test
    void testDefaultTransactionIsolation() {
        FBManagedConnectionFactory mcf = createDefaultMcf();

        assertEquals(Connection.TRANSACTION_READ_COMMITTED, mcf.getDefaultTransactionIsolation(),
                "Default tx isolation level must be READ_COMMITTED");
    }

    @Test
    void cannotChangeConfigurationAfterStartForSharedMcf() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf(true);

        // possible before connecting
        mcf.setBlobBufferSize(1024);

        FBManagedConnection mc = mcf.createManagedConnection();
        mc.destroy();

        assertThrows(IllegalStateException.class, () -> mcf.setBlobBufferSize(2048),
                "modification of config of shared mcf should not be allowed after creating a connection");
    }

    @Test
    void canChangeConfigurationAfterStartForUnsharedMcf() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf(false);

        // possible before connecting
        mcf.setBlobBufferSize(1024);

        FBManagedConnection mc = mcf.createManagedConnection();
        mc.destroy();

        // still possible after creating a connection
        assertDoesNotThrow(() -> mcf.setBlobBufferSize(2048),
                "modification of config of not shared mcf should be allowed after creating a connection");
    }
}

