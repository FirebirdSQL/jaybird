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

/**
 * Describe class <code>TestFBManagedConnectionFactory</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBManagedConnectionFactory extends TestXABase {


    public TestFBManagedConnectionFactory(String name) {
        super(name);
    }





    public void testCreateMcf() throws Exception {
        if (log != null) log.info("testCreateMcf");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnectionFactory realMcf = mcf;
    }

    public void testCreateMc() throws Exception {
        
        if (log != null) log.info("testCreateMc");
        FBManagedConnectionFactory mcf = initMcf();
        ManagedConnection mc = mcf.createManagedConnection(null, null);
        mc.destroy();
    }


/*
    public void testSqlInfo() throws Exception {
        
        if (log != null) log.info("testSqlInfo");
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
*/
}

