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

import javax.resource.cci.LocalTransaction;
import java.sql.Connection;
import javax.sql.DataSource;

import org.firebirdsql.jdbc.FBConnection;

import java.sql.*;

import junit.framework.*;

/**
 * Describe class <code>TestFBStandAloneConnectionManager</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBStandAloneConnectionManager extends TestXABase {


    public TestFBStandAloneConnectionManager(String name) {
        super(name);
    }

    public static Test suite() {

        return new TestSuite(TestFBStandAloneConnectionManager.class);
    }



    public void testCreateDCM() throws Exception {
        
        if (log != null) log.info("testCreateDCM");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        assertTrue("Could not get DataSource", ds != null);
        Connection c = ds.getConnection();
        assertTrue("Could not get Connection", c != null);
        c.close();
    }


    public void testCreateStatement() throws Exception {
        
        if (log != null) log.info("testCreateStatement");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        assertTrue("Could not get Statement", s != null);
        c.close();
    }

    public void testUseStatement() throws Exception {
        
        if (log != null) log.info("testUseStatement");
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource)mcf.createConnectionFactory();
        FBConnection c = (FBConnection)ds.getConnection();
        Statement s = c.createStatement();
        LocalTransaction t = c.getLocalTransaction();
        assertTrue("Could not get LocalTransaction", t != null);
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
