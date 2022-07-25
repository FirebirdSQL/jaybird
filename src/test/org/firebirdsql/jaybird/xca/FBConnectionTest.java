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

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.createDefaultMcf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FBConnectionTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @Test
    void testCreateC() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        assertNotNull(mcf, "Could not get FBManagedConnectionFactory");
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            assertNotNull(mc, "Could not get FBManagedConnection");
            Connection c = mc.getConnection();
            assertNotNull(c, "Could not get Connection");
        } finally {
            mc.destroy();
        }
    }

    @Test
    void testCreateStatement() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            Connection c = mc.getConnection();
            Statement s = c.createStatement();
            assertNotNull(s, "Could not create Statement");
        } finally {
            mc.destroy();
        }
    }

    @Test
    void testUseStatement() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        try {
            Connection c = mc.getConnection();
            Statement s = c.createStatement();
            XAResource xa = mc.getXAResource();
            Exception ex = null;
            Xid xid = new XidImpl();
            xa.start(xid, XAResource.TMNOFLAGS);
            try {
                s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)");
                //s.close();
            } catch (Exception e) {
                ex = e;
            }
            xa.end(xid, XAResource.TMSUCCESS);
            xa.commit(xid, true);

            xid = new XidImpl();
            xa.start(xid, XAResource.TMNOFLAGS);
            s.execute("DROP TABLE T1");
            s.close();
            xa.end(xid, XAResource.TMSUCCESS);
            xa.commit(xid, true);
            if (ex != null) {
                throw ex;
            }
        } finally {
            mc.destroy();
        }
    }
}
