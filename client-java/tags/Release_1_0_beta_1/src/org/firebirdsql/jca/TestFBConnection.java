/*   This class is LGPL only, due to the inclusion of a
 *Xid implementation from the JBoss project as a static inner class for testing purposes.
 *The portions before the XidImpl are usable under MPL 1.1 or LGPL
 *If we write our own xid test implementation, we can reset the license to match
 *the rest of the project.
 *Original author of non-jboss code david jencks
 *copyright 2001 all rights reserved.
 */
package org.firebirdsql.jca;

import javax.resource.spi.*;
import javax.transaction.xa.*;
import java.sql.Connection;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.jgds.GDS_Impl;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.logging.Logger;

import java.io.*;
import java.util.Properties;
import java.util.HashSet;
import java.sql.*;

//for embedded xid implementation
    import java.net.InetAddress;
    import java.net.UnknownHostException;


import junit.framework.*;

/**
 *
 *   @see <related>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *   @version $ $
 */



/**
 *This is a class that hands out connections.  Initial implementation uses DriverManager.getConnection,
 *future enhancements will use datasources/ managed stuff.
 */
public class TestFBConnection extends TestXABase {


    public TestFBConnection(String name) {
        super(name);
    }

    public static Test suite() {

        return new TestSuite(TestFBConnection.class);
    }



    public void testCreateC() throws Exception {
        log.info("testCreateC");
        FBManagedConnectionFactory mcf = initMcf();
        assertTrue("Could not get FBManagedConnectionFactory", mcf != null);
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        assertTrue("Could not get ManagedConnection", mc != null);
        Connection c = (Connection)mc.getConnection(null, null);
        assertTrue("Could not get Connection", c != null);
        mc.destroy();
    }

    public void testAssociateC() throws Exception {
        log.info("testAssociateC");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        Connection c1 = (Connection)mc1.getConnection(null, null);
        ManagedConnection mc2 = mcf.createManagedConnection(null, null);
        Connection c2 = (Connection)mc2.getConnection(null, null);
        mc1.associateConnection(c2);
        mc2.associateConnection(c1);
        mc1.destroy();
        mc2.destroy();
    }

    public void testCreateStatement() throws Exception {
        log.info("testCreateStatement");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
        Statement s = c.createStatement();
        assertTrue("Could not create Statement", s != null);
        mc.destroy();
    }

    public void testUseStatement() throws Exception {
        log.info("testUseStatement");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
        Statement s = c.createStatement();
        XAResource xa = mc.getXAResource();
        Exception ex = null;
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        try {
            s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)");
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);

        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("DROP TABLE T1");
        s.close();
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        mc.destroy();
        if (ex != null) {
            throw ex;
        }

    }



}
