/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.jdbc.FBConnection;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;

/**
 * Describe class <code>TestFBStandAloneConnectionManager</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBStandAloneConnectionManagerTest extends TestXABase {

    @Test
    public void testCreateDCM() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = mcf.createConnectionFactory();
        assertNotNull("Could not get DataSource", ds);
        Connection c = ds.getConnection();
        assertNotNull("Could not get Connection", c);
        c.close();
    }

    @Test
    public void testCreateStatement() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = mcf.createConnectionFactory();
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        assertNotNull("Could not get Statement", s);
        c.close();
    }

    @Test
    public void testUseStatement() throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = mcf.createConnectionFactory();
        FBConnection c = (FBConnection) ds.getConnection();
        Statement s = c.createStatement();
        FBLocalTransaction t = c.getLocalTransaction();
        assertNotNull("Could not get LocalTransaction", t);
        Exception ex = null;
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)");
        } catch (Exception e) {
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
