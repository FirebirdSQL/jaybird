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
public class TestFBDatabaseMetaData extends TestXABase {
    
    
    public TestFBDatabaseMetaData(String name) {
        super(name);
    }
    
    public static Test suite() {

        return new TestSuite(TestFBDatabaseMetaData.class);
    }
    


    public void testGetTables() throws Exception {
        System.out.println();
        System.out.println("testGetTables");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        Exception ex = null;
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 SMALLINT, C3 DECIMAL(18,0), C4 FLOAT, C5 DOUBLE PRECISION, C6 CHAR(10), C7 VARCHAR(20))"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
        
        //t.begin();
        DatabaseMetaData dmd = c.getMetaData();
        ResultSet rs = dmd.getTables(null, null, "T1", null);
        int count = 0;
        while (rs.next()) {
            String name =  rs.getString(3).trim();
            System.out.println("table name: " + name);
            count++;
            assertTrue("Didn't get back the name expected", "T1".equals(name));
        }
        assertTrue("Got more than one table name back!", count == 1);
        rs.close();


        
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
