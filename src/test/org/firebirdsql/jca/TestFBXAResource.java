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

import javax.resource.spi.*;
import javax.transaction.xa.*;
import java.sql.Connection;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
//import org.firebirdsql.jgds.GDS_Impl;
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
        
        if (log != null) log.info("testStartXATrans");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc = (FBManagedConnection)mc;
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        if (fbmc.getIscDBHandle(new HashSet()) == null) {
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
        if (fbmc.getIscDBHandle(new HashSet()) == null) {
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
        if (fbmc.getIscDBHandle(new HashSet()) == null) {
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
        if (fbmc.getIscDBHandle(new HashSet()) == null) {
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
        if (fbmc1.getIscDBHandle(new HashSet()) == null) {
            throw new Exception("no db handle after start xid");
        }
        ManagedConnection mc2 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc2 = (FBManagedConnection)mc2;
        XAResource xa2 = mc2.getXAResource();
        Xid xid2 = new XidImpl();
        xa2.start(xid2, XAResource.TMNOFLAGS);
        if (fbmc2.getIscDBHandle(new HashSet()) == null) {
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
        
        if (log != null) log.info("testRecover");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        FBManagedConnection fbmc1 = (FBManagedConnection)mc1;
        XAResource xa1 = mc1.getXAResource();
        Xid[] xids = xa1.recover(XAResource.TMSTARTRSCAN);
        assertTrue("Xid[] was null from recover!", xids != null);
    }

}
