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

import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.assertEquals;

public class TestFBManagedConnectionFactory extends TestXABase {

    @Test
    public void testCreateMcf() {
        // TODO Test doesn't assert anything
        initMcf();
    }

    @Test
    public void testCreateMc() throws Exception {
        // TODO Test doesn't assert anything
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        mc.destroy();
    }

    /**
     * Test if default isolation level is Connection.TRANSACTION_READ_COMMITTED
     */
    @Test
    public void testDefaultTransactionIsolation() {
        FBManagedConnectionFactory mcf = initMcf();

        assertEquals("Default tx isolation level must be READ_COMMITTED",
                Connection.TRANSACTION_READ_COMMITTED, mcf.getDefaultTransactionIsolation());
    }
}

