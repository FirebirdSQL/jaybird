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
public class TestFBManagedConnectionFactory extends TestCase {
    
    static final String dbName = "localhost/3050:/usr/local/firebird/dev/client-java/db/jbosstest.gdb";
    static final String dbName2 = "localhost:/usr/local/firebird/dev/client-java/db/testdb2.gdb";

//    private FBManagedConnectionFactory mcf;
    
//    private Clumplet dpb;
    
//    private HashSet tpb;
    
    public TestFBManagedConnectionFactory(String name) {
        super(name);
    }
    
    public static Test suite() {

        return new TestSuite(TestFBManagedConnectionFactory.class);
    }
    
    public FBManagedConnectionFactory initMcf() {
        
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory();
        mcf.setDatabase(dbName);
        GDS gds = new GDS_Impl();
        Clumplet dpb = gds.newClumplet(GDS.isc_dpb_num_buffers, new byte[] {90});
        dpb.append(gds.newClumplet(GDS.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0}));
        mcf.setDpb(dpb);
        HashSet tpb = new HashSet();
        tpb.add(new Integer(GDS.isc_tpb_write));
        tpb.add(new Integer(GDS.isc_tpb_read_committed));
        tpb.add(new Integer(GDS.isc_tpb_no_rec_version));
        tpb.add(new Integer(GDS.isc_tpb_wait));
        mcf.setTpb(tpb);
        return mcf;
    }

    
    



    public void testCreateMcf() throws Exception {
        System.out.println();
        System.out.println("testCreateMcf");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnectionFactory realMcf = mcf;
    }

    public void testCreateMc() throws Exception {
        System.out.println();
        System.out.println("testCreateMc");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
    }

    public void testCreateC() throws Exception {
        System.out.println();
        System.out.println("testCreateC");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
    }

    public void testAssociateC() throws Exception {
        System.out.println();
        System.out.println("testAssociateC");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc1 = mcf.createManagedConnection(null, null);
        Connection c1 = (Connection)mc1.getConnection(null, null);
        ManagedConnection mc2 = mcf.createManagedConnection(null, null);
        Connection c2 = (Connection)mc2.getConnection(null, null);
        mc1.associateConnection(c2);
        mc2.associateConnection(c1);
    }
    
    public void testCreateStatement() throws Exception {
        System.out.println();
        System.out.println("testCreateStatement");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
        Statement s = c.createStatement();
    }

    public void testUseStatement() throws Exception {
        System.out.println();
        System.out.println("testCreateStatement");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        Connection c = (Connection)mc.getConnection(null, null);
        Statement s = c.createStatement();
        XAResource xa = mc.getXAResource();
        Xid xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)"); 
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        
        xid = new XidImpl();
        xa.start(xid, XAResource.TMNOFLAGS);
        s.execute("DROP TABLE T1"); 
        xa.end(xid, XAResource.TMNOFLAGS);
        xa.commit(xid, true);
        
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
        
    }
    
    /*Borrowed from 
     * JBoss, the OpenSource EJB server
     *
     * Distributable under LGPL license.
     * See terms of license at gnu.org.
     */
     /*
    package org.firebirdsql.jca.test;
    
    import java.net.InetAddress;
    import java.net.UnknownHostException;
    
    import javax.transaction.xa.Xid;
    */
    
    /**
     *  This object encapsulates the ID of a transaction.
     *  This implementation is immutable and always serializable at runtime.
     *
     *  @see TransactionImpl
     *  @author Rickard Öberg (rickard.oberg@telkel.com)
     *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
     *  @version $Revision$
     */
        public static class  XidImpl
       implements Xid, java.io.Serializable
    {
       // Constants -----------------------------------------------------
    
       public static final int JBOSS_FORMAT_ID = 0x0101;
    
       // Attributes ----------------------------------------------------
    
       /**
        *  Hash code of this instance. This is really a sequence number.
        */
       private int hash;
    
       /**
        *  Global transaction id of this instance.
        *  The coding of this class depends on the fact that this variable is
        *  initialized in the constructor and never modified. References to
        *  this array are never given away, instead a clone is delivered.
        */
       private byte[] globalId;
    
       /**
        *  Branch qualifier of this instance.
        *  This identifies the branch of a transaction.
        */
       private byte[] branchId;
    
       // Static --------------------------------------------------------
    
       /**
        *  The host name of this host, followed by a slash.
        *
        *  This is used for building globally unique transaction identifiers.
        *  It would be safer to use the IP address, but a host name is better
        *  for humans to read and will do for now.
        */
       private static String hostName;
    
       /**
        *  The next transaction id to use on this host.
        */
       static private int nextId = 0;
    
       /**
        *  Return a new unique transaction id to use on this host.
        */
       static private synchronized int getNextId()
       {
          return nextId++;
       }
    
       /**
        *  Singleton for no branch qualifier.
        */
       static private byte[] noBranchQualifier = new byte[0];
    
       /**
        *  Initialize the <code>hostName</code> class variable.
        */
       static {
          try {
             hostName = InetAddress.getLocalHost().getHostName() + "/";
             // Ensure room for 14 digits of serial no.
             if (hostName.length() > MAXGTRIDSIZE - 15)
                hostName = hostName.substring(0, MAXGTRIDSIZE - 15);
             hostName = hostName + "/";
          } catch (UnknownHostException e) {
             hostName = "localhost/";
          }
       }
    
       /**
        *  Return a string that describes any Xid instance.
        */
       static String toString(Xid id) {
          if (id == null)
             return "[NULL Xid]";
    
          String s = id.getClass().getName();
          s = s.substring(s.lastIndexOf('.') + 1);
          s = s + " [FormatId=" + id.getFormatId() +
                  ", GlobalId=" + new String(id.getGlobalTransactionId()).trim() +
                  ", BranchQual=" + new String(id.getBranchQualifier()).trim()+"]";
    
          return s;
       }
    
       // Constructors --------------------------------------------------
    
       /**
        *  Create a new instance.
        */
       public XidImpl()
       {
          hash = getNextId();
          globalId = (hostName + Integer.toString(hash)).getBytes();
          branchId = noBranchQualifier;
       }
    
       /**
        *  Create a new branch of an existing global transaction ID.
        *
        *  @param xid The transaction ID to create a new branch of.
        *  @param branchId The ID of the new branch.
        *
        */
       public XidImpl(XidImpl xid, int branchId)
       {
          this.hash = xid.hash;
          this.globalId = xid.globalId; // reuse array instance, we never modify.
          this.branchId = Integer.toString(branchId).getBytes();
       }
    
       // Public --------------------------------------------------------
    
       // Xid implementation --------------------------------------------
    
       /**
        *  Return the global transaction id of this transaction.
        */
       public byte[] getGlobalTransactionId()
       {
          return (byte[])globalId.clone();
       }
    
       /**
        *  Return the branch qualifier of this transaction.
        */
       public byte[] getBranchQualifier()
       {
          if (branchId.length == 0)
             return branchId; // Zero length arrays are immutable.
          else
             return (byte[])branchId.clone();
       }
    
       /**
        *  Return the format identifier of this transaction.
        *
        *  The format identifier augments the global id and specifies
        *  how the global id and branch qualifier should be interpreted.
        */
       public int getFormatId() {
          // The id we return here should be different from all other transaction
          // implementations.
          // Known IDs are:
          // -1:     Sometimes used to denote a null transaction id.
          // 0:      OSI TP (javadoc states OSI CCR, but that is a bit misleading
          //         as OSI CCR doesn't even have ACID properties. But OSI CCR and
          //         OSI TP do have the same id format.)
          // 1:      Was used by early betas of jBoss.
          // 0x0101: The JBOSS_FORMAT_ID we use here.
          // 0xBB14: Used by JONAS.
          // 0xBB20: Used by JONAS.
    
          return JBOSS_FORMAT_ID;
       }
    
       /**
        *  Compare for equality.
        *
        *  Instances are considered equal if they are both instances of XidImpl,
        *  and if they have the same global transaction id and transaction
        *  branch qualifier.
        */
       public boolean equals(Object obj)
       {
          if (obj instanceof XidImpl) {
             XidImpl other = (XidImpl)obj;
    
             if (globalId.length != other.globalId.length ||
                 branchId.length != other.branchId.length)
                return false;
    
             for (int i = 0; i < globalId.length; ++i)
                if (globalId[i] != other.globalId[i])
                   return false;
    
             for (int i = 0; i < branchId.length; ++i)
                if (branchId[i] != other.branchId[i])
                   return false;
    
             return true;
          }
          return false;
       }
    
       public int hashCode()
       {
          return hash;
       }
    
       public String toString()
       {
          return toString(this);
       }
    
       // Package protected ---------------------------------------------
    
       /**
        *  Return the global transaction id of this transaction.
        *  Unlike the {@link #getGlobalTransactionId()} method, this one
        *  returns a reference to the global id byte array that may <em>not</em>
        *  be changed.
        */
       public byte[] getInternalGlobalTransactionId()
       {
          return (byte[])globalId.clone();
       }
    
       
       // Protected -----------------------------------------------------
    
       // Private -------------------------------------------------------
    
       // Inner classes -------------------------------------------------
    }
    
}
