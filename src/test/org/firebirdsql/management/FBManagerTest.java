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
package org.firebirdsql.management;

import org.firebirdsql.common.StringHelper;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.management.PageSizeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBManager}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
class FBManagerTest {

    @Test
    void testStart() throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();
        }
    }

    @Test
    void testCreateDrop() throws Exception {
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

            assertTrue(m.isDatabaseExists(databasePath, DB_USER, DB_PASSWORD), "Must report that database exists");

            // check drop
            m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);

            assertFalse(m.isDatabaseExists(databasePath, DB_USER, DB_PASSWORD),
                    "Must report that database does not exist");
        }
    }

    @Test
    void testSetPageSize_createdDatabaseHasSize() throws Exception {
        checkPageSizeCreated(PageSizeConstants.SIZE_16K);
    }

    @Test
    void testSetPageSize32K() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPageSize(PageSizeConstants.SIZE_32K),
                "requires 32k page size support");
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
                assertEquals(ISCConstants.isc_info_page_size, databaseInfo[0], "Unexpected info item");
                int length = iscVaxInteger2(databaseInfo, 1);
                int actualPageSize = iscVaxInteger(databaseInfo, 3, length);
                assertEquals(requestedPageSize, actualPageSize, "Unexpected page size");
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @SuppressWarnings("resource")
    @Test
    void testSetPageSize_Invalid_throwsIllegalArgumentException() {
        FBManager m = createFBManager();

        assertThrows(IllegalArgumentException.class, () -> m.setPageSize(4000));
    }

    @SuppressWarnings("resource")
    @ParameterizedTest
    @ValueSource(ints = { SIZE_1K, SIZE_2K, SIZE_4K, SIZE_8K, SIZE_16K, SIZE_32K })
    void testSetPageSize_ValidValues(int pageSize) {
        FBManager m = createFBManager();

        m.setPageSize(pageSize);
    }

    @Test
    void testDialect3_dbCreatedWithRightDialect() throws Exception {
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
                assertEquals(3, currentDatabase.getDatabaseDialect(), "Unexpected database dialect");
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @Test
    void testDialect1_dbCreatedWithRightDialect() throws Exception {
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
                assertEquals(1, currentDatabase.getDatabaseDialect(), "Unexpected database dialect");
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @Test
    void testCreate_withDefaultCharacterSet() throws Exception {
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

                    assertTrue(rs.next(), "expected a row");
                    assertEquals("UTF8", StringHelper.trim(rs.getString(1)), "Unexpected default character set");
                }
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }

    @Test
    void testCreate_forceWrite_null() throws Exception {
        checkForceWrite(null, 1);
    }

    @Test
    void testCreate_forceWrite_true() throws Exception {
        checkForceWrite(true, 1);
    }

    @Test
    void testCreate_forceWrite_false() throws Exception {
        checkForceWrite(false, 0);
    }

    private void checkForceWrite(Boolean forceWrite, int expectedValue) throws Exception {
        try (FBManager m = createFBManager()) {
            m.setServer(DB_SERVER_URL);
            m.setPort(DB_SERVER_PORT);
            m.start();

            // Adding .fdb suffix to prevent conflicts with other tests if drop fails
            final String databasePath = getDatabasePath() + ".fdb";
            try {
                m.setForceWrite(forceWrite);
                m.createDatabase(databasePath, DB_USER, DB_PASSWORD);

                try (Connection connection = DriverManager.getConnection(getUrl() + ".fdb",
                        getDefaultPropertiesForConnection());
                     Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("select MON$FORCED_WRITES from mon$database")) {

                    assertTrue(rs.next(), "expected a row");
                    assertEquals(expectedValue, rs.getInt(1), "Unexpected default character set");
                }
            } finally {
                m.dropDatabase(databasePath, DB_USER, DB_PASSWORD);
            }
        }
    }
}
