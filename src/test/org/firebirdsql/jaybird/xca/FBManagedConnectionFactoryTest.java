/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
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

