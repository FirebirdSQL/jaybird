/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jca;

import javax.resource.spi.ManagedConnection;
import java.sql.Connection;

/**
 * Describe class <code>TestFBManagedConnectionFactory</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBManagedConnectionFactory extends TestXABase {

    public TestFBManagedConnectionFactory(String name) {
        super(name);
    }

    public void testCreateMcf() throws Exception {
        // TODO Test doesn't assert anything
        initMcf();
    }

    public void testCreateMc() throws Exception {
        // TODO Test doesn't assert anything
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        mc.destroy();
    }

    /**
     * Test if default isolation level is Connection.TRANSACTION_READ_COMMITTED
     */
    public void testDefaultTransactionIsolation() {
        FBManagedConnectionFactory mcf = initMcf();

        assertEquals("Default tx isolation level must be READ_COMMITTED",
                Connection.TRANSACTION_READ_COMMITTED, mcf.getDefaultTransactionIsolation());
    }
}

