 /*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jca;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.resource.spi.ManagedConnection;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Describe class <code>TestFBXAResource</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBXAResource extends TestXABase {


    public TestFBXAResource(String name) {
        super(name);
    }

    public void testGetXAResource() throws Exception {
        
        if (log != null) log.info("testGetXAResource");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        XAResource xa1 = mc.getXAResource();
        XAResource xa2 = mc.getXAResource();
        if (xa1 != xa2) {
            throw new Exception("XAResources do not match from same mc");
        }
        mc.destroy();
    }

    public void testIsSameRM() throws Exception {
        
        if (log != null) log.info("testIsSameRM");
        FBManagedConnectionFactory mcf1 = initMcf();
        ManagedConnection mc1 = mcf1.createManagedConnection(null, null);
        XAResource xa1 = mc1.getXAResource();
        ManagedConnection mc2 = mcf1.createManagedConnection(null, null);
        XAResource xa2 = mc2.getXAResource();
        FBManagedConnectionFactory mcf3 = initMcf();
        ManagedConnection mc3 = mcf3.createManagedConnection(null, null);
        XAResource xa3 = mc3.getXAResource();
        if (xa1.isSameRM(xa2)) {
            throw new Exception("isSameRM reports no difference from same mcf");
        }
        if (xa1.isSameRM(xa3)) {
            throw new Exception("isSameRM reports no difference from different mcf");
        }
        mc1.destroy();
        mc2.destroy();
    }

    public void testStartXATrans() throws Exception {
        
        if (log != null) log.info("testStartXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getGDSHelper().getCurrentDbHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMSUCCESS);
        xa.commit(xid, true);
        mc.destroy();
    }

    public void testRollbackXATrans() throws Exception {
        
        if (log != null) log.info("testRollbackXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getGDSHelper().getCurrentDbHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMSUCCESS);
        xa.rollback(xid);
        mc.destroy();
    }

    public void test2PCXATrans() throws Exception {
        
        if (log != null) log.info("test2PCXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getGDSHelper().getCurrentDbHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMSUCCESS);
        xa.prepare(xid);
        xa.commit(xid, false);
        mc.destroy();
    }

    public void testRollback2PCXATrans() throws Exception {
        
        if (log != null) log.info("testRollback2PCXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getGDSHelper().getCurrentDbHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMSUCCESS);
        xa.prepare(xid);
        xa.rollback(xid);
        mc.destroy();
    }

    public void testDo2XATrans() throws Exception {
        
        if (log != null) log.info("testDo2XATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc1 = (FBManagedConnection)mc1;
        XAResource xa1 = mc1.getXAResource();
        Xid xid1 = new XidImpl();
        xa1.start(xid1, XAResource.TMNOFLAGS);
        if (fbmc1.getGDSHelper().getCurrentDbHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        ManagedConnection mc2 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc2 = (FBManagedConnection)mc2;
        XAResource xa2 = mc2.getXAResource();
        Xid xid2 = new XidImpl();
        xa2.start(xid2, XAResource.TMNOFLAGS);
        if (fbmc2.getGDSHelper().getCurrentDbHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        //commit each tr on other xares
        xa1.end(xid1, XAResource.TMSUCCESS);
        xa2.commit(xid1, true);
        xa2.end(xid2, XAResource.TMSUCCESS);
        xa1.commit(xid2, true);
        mc1.destroy();
        mc2.destroy();

    }

    public void testRecover() throws Exception
    {
        
        Connection connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            try {
                try {
                    stmt.execute("DROP TABLE test_reconnect");
                } catch(SQLException ex) {
                    // empty
                }
                
                stmt.execute("CREATE TABLE test_reconnect(id INTEGER)");
            } finally {
                stmt.close();
            }
            
        } finally {
            connection.close();
        }
        
        if (log != null) log.info("testRecover");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        
        FBManagedConnection fbmc1 = (FBManagedConnection)mc1;
        XAResource xa1 = mc1.getXAResource();
        
        Xid xid1 = new XidImpl();
        xa1.start(xid1, XAResource.TMNOFLAGS);
        
        Connection fbc1 = (Connection)fbmc1.getConnection(null, null);
        Statement fbstmt1 = fbc1.createStatement();
        try {
            fbstmt1.execute("INSERT INTO test_reconnect(id) VALUES(1)");
        } finally {
            fbstmt1.close();
        }
        
        xa1.end(xid1, XAResource.TMSUCCESS);
        xa1.prepare(xid1);
        
        // kill connection after prepare.
        mc1.destroy();
        

        FBManagedConnectionFactory mcf2 = initMcf();
        ManagedConnection mc2 = mcf2.createManagedConnection(null, null);
        XAResource xa2 = mc2.getXAResource();

        Xid xid2 = new XidImpl();
        xa2.start(xid2, XAResource.TMNOFLAGS);
        
        Xid[] xids = xa2.recover(XAResource.TMSTARTRSCAN);
        
        xa2.end(xid2, XAResource.TMSUCCESS);
        xa2.commit(xid2, true);
        
        assertTrue("Should recover non-null array", xids != null);
        assertTrue("Should recover at least one transaction", xids.length > 0);
        
        boolean found = false;
        for (int i = 0; i < xids.length; i++) {
            if (xids[i].equals(xid1)) {
                found = true;
                break;
            }
        }
        
        assertTrue("Should find our transaction", found);
        
        xa2.commit(xid1, false);
        
        connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM test_reconnect");
                assertTrue("Should find at least one row.", rs.next());
                assertTrue("Should read correct value", rs.getInt(1) == 1);
                assertTrue("Should select only one row", !rs.next());
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
    }

}