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
import java.sql.PreparedStatement;
import javax.sql.DataSource;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.jgds.GDS_Impl;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.jdbc.FBConnection;

import org.firebirdsql.jgds.TestGds;

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
 *This runs all the FBTests as one suite.
 */
public class TestAll extends TestCase {
    
    
    public TestAll(String name) {
        super(name);
    }
    
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestGds.class);
        suite.addTestSuite(TestFBBlob.class);
        //        suite.addTestSuite(TestFBConnection.class);
        //suite.addTestSuite(TestFBDatabaseMetaData.class);
        suite.addTestSuite(TestFBManagedConnectionFactory.class);
        suite.addTestSuite(TestFBResultSet.class);
        suite.addTestSuite(TestFBStandAloneConnectionManager.class);
        suite.addTestSuite(TestFBXAResource.class);
        return suite;
    }
    


    
}
