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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.createDefaultMcf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Describe class <code>TestFBStandAloneConnectionManager</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
class FBStandAloneConnectionManagerTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @Test
    void testCreateDCM() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        DataSource ds = mcf.createConnectionFactory();
        assertNotNull(ds, "Could not get DataSource");
        try (Connection c = ds.getConnection()) {
            assertNotNull(c, "Could not get Connection");
        }
    }

    @Test
    void testCreateStatement() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        DataSource ds = mcf.createConnectionFactory();
        try (Connection c = ds.getConnection()) {
            Statement s = c.createStatement();
            assertNotNull(s, "Could not get Statement");
        }
    }

    @Test
    void testUseStatement() throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        DataSource ds = mcf.createConnectionFactory();
        Exception ex = null;
        try (FBConnection c = (FBConnection) ds.getConnection()) {
            Statement s = c.createStatement();
            FBLocalTransaction t = c.getLocalTransaction();
            assertNotNull(t, "Could not get LocalTransaction");
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
        }
        if (ex != null) {
            throw ex;
        }
    }

}
