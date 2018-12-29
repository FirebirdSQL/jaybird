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
package org.firebirdsql.management;

import org.firebirdsql.common.StringHelper;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.management.PageSizeConstants.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Describe class <code>TestFBManager</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBManagerTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testStart() throws Exception {
        FBManager m = createFBManager();
        m.setServer(DB_SERVER_URL);
        m.setPort(DB_SERVER_PORT);
        m.start();
        m.stop();
    }

    @Test
    public void testCreateDrop() throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();

            // Adding .fdb suffix to prevent conflicts with other tests if drop fails
            final String databasePath = getDatabasePath() + ".fdb";
            // check create
            m.createDatabase(databasePath, DB_USER, DB_PASSWORD);

            // check create with set forceCreate
            m.setForceCreate(true);
            m.createDatabase(databasePath, DB_USER, DB_PASSWORD);

            assertTrue("Must report that database exists", m.isDatabaseExists(databasePath, DB_USER, DB_PASSWORD));

            // check drop
            m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);

            assertFalse("Must report that database does not exist",
                    m.isDatabaseExists(databasePath, DB_USER, DB_PASSWORD));
        }
    }

    @Test
    public void testSetPageSize_createdDatabaseHasSize() throws Exception {
        checkPageSizeCreated(PageSizeConstants.SIZE_16K);
    }

    @Test
    public void testSetPageSize32K() throws Exception {
        assumeTrue("requires 32k page size support",
                getDefaultSupportInfo().supportsPageSize(PageSizeConstants.SIZE_32K));
        checkPageSizeCreated(PageSizeConstants.SIZE_32K);
    }

    private void checkPageSizeCreated(int requestedPageSize) throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();

            // Adding .fdb suffix to prevent conflicts with other tests if drop fails
            final String databasePath = getDatabasePath() + ".fdb";

            m.setPageSize(requestedPageSize);

            // check create
            m.createDatabase(databasePath, DB_USER, DB_PASSWORD);
            try (FBConnection connection = (FBConnection) DriverManager.getConnection(getUrl() + ".fdb",
                    getDefaultPropertiesForConnection())) {
                final FbDatabase currentDatabase = connection.getGDSHelper().getCurrentDatabase();
                final byte[] databaseInfo = currentDatabase.getDatabaseInfo(
                        new byte[] { ISCConstants.isc_info_page_size }, 10);
                assertEquals("Unexpected info item", ISCConstants.isc_info_page_size, databaseInfo[0]);
                int length = iscVaxInteger2(databaseInfo, 1);
                int actualPageSize = iscVaxInteger(databaseInfo, 3, length);
                assertEquals("Unexpected page size", requestedPageSize, actualPageSize);
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @Test
    public void testSetPageSize_Invalid_throwsIllegalArgumentException() {
        FBManager m = createFBManager();
        expectedException.expect(IllegalArgumentException.class);

        m.setPageSize(4000);
    }

    @Test
    public void testSetPageSize_ValidValues() {
        FBManager m = createFBManager();

        final int[] pageSizes = { SIZE_1K, SIZE_2K, SIZE_4K, SIZE_8K, SIZE_16K, SIZE_32K };
        for (int pageSize : pageSizes) {
            m.setPageSize(pageSize);
        }
    }

    @Test
    public void testDialect3_dbCreatedWithRightDialect() throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();

            // Adding .fdb suffix to prevent conflicts with other tests if drop fails
            final String databasePath = getDatabasePath() + ".fdb";

            m.setDialect(3);

            // check create
            m.createDatabase(databasePath, DB_USER, DB_PASSWORD);
            try (FBConnection connection = (FBConnection) DriverManager.getConnection(getUrl() + ".fdb",
                    getDefaultPropertiesForConnection())) {
                final FbDatabase currentDatabase = connection.getGDSHelper().getCurrentDatabase();
                assertEquals("Unexpected database dialect", 3, currentDatabase.getDatabaseDialect());
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @Test
    public void testDialect1_dbCreatedWithRightDialect() throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();

            // Adding .fdb suffix to prevent conflicts with other tests if drop fails
            final String databasePath = getDatabasePath() + ".fdb";

            m.setDialect(1);

            // check create
            m.createDatabase(databasePath, DB_USER, DB_PASSWORD);
            try (FBConnection connection = (FBConnection) DriverManager.getConnection(getUrl() + ".fdb",
                    getDefaultPropertiesForConnection())) {
                final FbDatabase currentDatabase = connection.getGDSHelper().getCurrentDatabase();
                assertEquals("Unexpected database dialect", 1, currentDatabase.getDatabaseDialect());
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @Test
    public void testCreate_withDefaultCharacterSet() throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();

            // Adding .fdb suffix to prevent conflicts with other tests if drop fails
            final String databasePath = getDatabasePath() + ".fdb";
            try {
                m.setDefaultCharacterSet("UTF8");
                m.createDatabase(databasePath, DB_USER, DB_PASSWORD);

                try (Connection connection = DriverManager.getConnection(getUrl() + ".fdb",
                        getDefaultPropertiesForConnection());
                     Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("select RDB$CHARACTER_SET_NAME from rdb$database")) {

                    assertTrue("expected a row", rs.next());
                    assertEquals("Unexpected default character set", "UTF8", StringHelper.trim(rs.getString(1)));
                }
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }
}
