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
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 *This is a class that hands out connections.  Initial implementation uses DriverManager.getConnection,
 *future enhancements will use datasources/ managed stuff.
 */
public class TestFBXAResource extends TestXABase {


    public TestFBXAResource(String name) {
        super(name);
    }

    public void testGetXAResource() throws Exception {
        System.out.println();
        System.out.println("testGetXAResource");
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
        System.out.println();
        System.out.println("testIsSameRM");
        FBManagedConnectionFactory mcf1 = initMcf();
        ManagedConnection mc1 = mcf1.createManagedConnection(null, null);
        XAResource xa1 = mc1.getXAResource();
        ManagedConnection mc2 = mcf1.createManagedConnection(null, null);
        XAResource xa2 = mc2.getXAResource();
        FBManagedConnectionFactory mcf3 = initMcf();
        ManagedConnection mc3 = mcf3.createManagedConnection(null, null);
        XAResource xa3 = mc3.getXAResource();
        if (!xa1.isSameRM(xa2)) {
            throw new Exception("isSameRM reports difference from same mcf");
        }
        if (xa1.isSameRM(xa3)) {
            throw new Exception("isSameRM reports no difference from different mcf");
        }
        mc1.destroy();
        mc2.destroy();
    }

    public void testStartXATrans() throws Exception {
        System.out.println();
        System.out.println("testStartXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getIscDBHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        mc.destroy();
    }

    public void testRollbackXATrans() throws Exception {
        System.out.println();
        System.out.println("testStartXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getIscDBHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.rollback(xid);
        mc.destroy();
    }

    public void test2PCXATrans() throws Exception {
        System.out.println();
        System.out.println("testStartXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getIscDBHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.prepare(xid);
        xa.commit(xid, false);
        mc.destroy();
    }

    public void testRollback2PCXATrans() throws Exception {
        System.out.println();
        System.out.println("testStartXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getIscDBHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.prepare(xid);
        xa.rollback(xid);
        mc.destroy();
    }

    public void testDo2XATrans() throws Exception {
        System.out.println();
        System.out.println("testDo2XATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc1 = (FBManagedConnection)mc1;
        XAResource xa1 = mc1.getXAResource();
        Xid xid1 = new XidImpl();
        xa1.start(xid1, XAResource.TMNOFLAGS);
        if (fbmc1.getIscDBHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        ManagedConnection mc2 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc2 = (FBManagedConnection)mc2;
        XAResource xa2 = mc2.getXAResource();
        Xid xid2 = new XidImpl();
        xa2.start(xid2, XAResource.TMNOFLAGS);
        if (fbmc2.getIscDBHandle() == null) {
            throw new Exception("no db handle after start xid");
        }
        //commit each tr on other xares
        xa1.end(xid1, XAResource.TMNOFLAGS);
        xa2.commit(xid1, true);
        xa2.end(xid2, XAResource.TMNOFLAGS);
        xa1.commit(xid2, true);
        mc1.destroy();
        mc2.destroy();

    }

    public void testRecover() throws Exception
    {
        System.out.println();
        System.out.println("testRecover");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc1 = (FBManagedConnection)mc1;
        XAResource xa1 = mc1.getXAResource();
        Xid[] xids = xa1.recover(XAResource.TMSTARTRSCAN);
        assertTrue("Xid[] was null from recover!", xids != null);
    }

}
