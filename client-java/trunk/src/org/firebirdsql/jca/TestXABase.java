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
//import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSFactory;
//import org.firebirdsql.jgds.GDS_Impl;
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
public class TestXABase extends TestCase {
    
    static final String DBNAME = "/usr/java/jboss/dev/jboss/dist/db/firebird/jbosstest.gdb";
//    static final String DBNAME = "/usr/local/firebird/dev/client-java/db/fbmctest.gdb";
    static final String dbName = "localhost/3050:" + DBNAME;
//    static final String dbName = "localhost/3050:/usr/local/firebird/dev/client-java/db/jbosstest.gdb";
//    static final String dbName2 = "localhost:/usr/local/firebird/dev/client-java/db/testdb2.gdb";

//    private FBManagedConnectionFactory mcf;
    
//    private Clumplet dpb;
    
//    private HashSet tpb;
    
    public TestXABase(String name) {
        super(name);
    }
    
/*    public static Test suite() {

        return new TestSuite(TestFBManagedConnectionFactory.class);
    }*/
    
    public void _setUp() throws Exception {
        FBManager m = new FBManager();
        m.setURL("localhost");
        m.setPort(3050);
        m.start();
        m.createDatabase(DBNAME);
        m.stop();
    }
    
    public void _tearDown() throws Exception {
        FBManager m = new FBManager();
        m.setURL("localhost");
        m.setPort(3050);
        m.start();
        m.dropDatabase(DBNAME);
        m.stop();
    }
    
    public FBManagedConnectionFactory initMcf() {
        
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory();
        mcf.setDatabase(dbName);
        FBConnectionRequestInfo cri = new FBConnectionRequestInfo();
        cri.setProperty(GDS.isc_dpb_num_buffers, new byte[] {90});
        cri.setProperty(GDS.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0});
        mcf.setConnectionRequestInfo(cri);
//        GDS gds = new GDS_Impl();
//        Clumplet dpb = GDSFactory.newClumplet(GDS.isc_dpb_num_buffers, new byte[] {90});
//        dpb.append(GDSFactory.newClumplet(GDS.isc_dpb_dummy_packet_interval, new byte[] {120, 10, 0, 0}));
//        mcf.setDpb(dpb);
        HashSet tpb = new HashSet();
        tpb.add(new Integer(GDS.isc_tpb_write));
        tpb.add(new Integer(GDS.isc_tpb_read_committed));
        tpb.add(new Integer(GDS.isc_tpb_no_rec_version));
        tpb.add(new Integer(GDS.isc_tpb_wait));
        mcf.setTpb(tpb);
        return mcf;
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
     *  @author Rickard �berg (rickard.oberg@telkel.com)
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
