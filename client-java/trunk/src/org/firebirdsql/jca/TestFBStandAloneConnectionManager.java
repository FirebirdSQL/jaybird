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
import javax.resource.cci.LocalTransaction;
import javax.transaction.xa.*;
import java.sql.Connection;
import javax.sql.DataSource;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.jgds.GDS_Impl;
import org.firebirdsql.jdbc.FBConnection;

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
public class TestFBStandAloneConnectionManager extends TestXABase {
    
    
    public TestFBStandAloneConnectionManager(String name) {
        super(name);
    }
    
    public static Test suite() {

        return new TestSuite(TestFBStandAloneConnectionManager.class);
    }
    
    

    public void testCreateDCM() throws Exception {
        System.out.println();
        System.out.println("testCreateDCM");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        Connection c = ds.getConnection();
        c.close();
    }

    
    public void testCreateStatement() throws Exception {
        System.out.println();
        System.out.println("testCreateStatement");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        c.close();
    }

    public void testUseStatement() throws Exception {
        System.out.println();
        System.out.println("testUseStatement");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
        
        t.begin();
        s.execute("DROP TABLE T1"); 
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
        
    }


    
}
