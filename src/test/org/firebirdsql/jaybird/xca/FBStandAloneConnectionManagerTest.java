/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2012-2022 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
 * @author David Jencks
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
