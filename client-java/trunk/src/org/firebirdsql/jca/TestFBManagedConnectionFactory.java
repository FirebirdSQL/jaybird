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
import java.sql.Statement;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSFactory;
import org.firebirdsql.gds.SqlInfo;
import org.firebirdsql.jgds.GDS_Impl;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.jdbc.FBConnection;
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



public class TestFBManagedConnectionFactory extends TestXABase {


    public TestFBManagedConnectionFactory(String name) {
        super(name);
    }





    public void testCreateMcf() throws Exception {
        log.info("testCreateMcf");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnectionFactory realMcf = mcf;
    }

    public void testCreateMc() throws Exception {
        
        log.info("testCreateMc");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        mc.destroy();
    }



    public void testSqlInfo() throws Exception {
        
        log.info("testSqlInfo");
        byte[] testbuffer = {
23, //isc_info_sql_records
29,  //length
0,
15,  //isc_info_req_update_count
4,//length
0,
4,
0,
0,
0,
16,//isc_info_req_delete_count
4,//length
0,
3,
0,
0,
0,
13,//isc_info_req_select_count
4,//length
0,
2,
0,
0,
0,
14,//isc_info_req_insert_count
4,//length
0,
1,
0,
0,
0,
1,  //isc_info_end
21,  //isc_info_sql_stmt_type
4,  //length
0,
2,  //isc_info_sql_stmt_insert
0,
0,
0,
1,  //isc_info_end
0,
0,
0,
0,
0,
0,
0,
0,
0,
0};
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        SqlInfo si = new SqlInfo(testbuffer, GDSFactory.newGDS());

        assertTrue("selectcount wrong " + si.getSelectCount(), si.getSelectCount() == 2);
        assertTrue("insertcount wrong " + si.getInsertCount(), si.getInsertCount() == 1);
        assertTrue("updatecount wrong " + si.getUpdateCount(), si.getUpdateCount() == 4);
        assertTrue("deletecount wrong " + si.getDeleteCount(), si.getDeleteCount() == 3);
        assertTrue("statement type wrong " + si.getStatementType(), si.getStatementType() == 2);

        mc.destroy();
    }

}

