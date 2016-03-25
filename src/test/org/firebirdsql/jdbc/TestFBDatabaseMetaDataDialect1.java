/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.management.FBManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for database metadata in dialect 1.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataDialect1 {

    protected FBManager fbManager = null;

    /**
     * Basic setup of the test database.
     *
     * @throws Exception
     */
    @Before
    public void basicSetUp() throws Exception {
        fbManager = createFBManager();
        fbManager.setDialect(1);
        defaultDatabaseSetUp(fbManager);
    }

    /**
     * Basic teardown of the test database
     *
     * @throws Exception
     */
    @After
    public void basicTearDown() throws Exception {
        defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }

    @Test
    public void testLargeNumeric() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sqlDialect", "1");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            DdlHelper.executeCreateTable(connection, "CREATE TABLE x (col NUMERIC(15,2))");
            final DatabaseMetaData md = connection.getMetaData();
            final ResultSet columns = md.getColumns(null, null, "X", "COL");
            assertTrue(columns.next());
            assertEquals("NUMERIC", columns.getString("TYPE_NAME"));
            assertEquals(Types.NUMERIC, columns.getInt("DATA_TYPE"));
            assertEquals(2, columns.getInt("DECIMAL_DIGITS"));
        }
    }

}
