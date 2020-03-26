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

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;

public class TestFBConnection extends TestXABase {

    @Test
    public void testCreateC() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        assertNotNull("Could not get FBManagedConnectionFactory", mcf);
        FBManagedConnection mc = mcf.createManagedConnection();
        assertNotNull("Could not get FBManagedConnection", mc);
        Connection c = mc.getConnection();
        assertNotNull("Could not get Connection", c);
        mc.destroy();
    }

    @Test
    public void testCreateStatement() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        FBManagedConnection mc = mcf.createManagedConnection();
        Connection c = mc.getConnection();
        Statement s = c.createStatement();
        assertNotNull("Could not create Statement", s);
        mc.destroy();
    }

    @Test
    public void testUseStatement() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
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
