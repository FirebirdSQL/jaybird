/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2003-2006 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jaybird.util.StringUtils;
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
 * @author David Jencks
 * @version 1.0
 */
class FBManagerTest {

    @Test
    void testStart() throws Exception {
        try (FBManager m = createFBManager(false)) {
            assertDoesNotThrow(m::start);
        }
    }

    @Test
    void testCreateDrop() throws Exception {
        try (FBManager m = createFBManager()) {
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
        FBManager m = FBTestProperties.createFBManager();

        assertThrows(IllegalArgumentException.class, () -> m.setPageSize(4000));
    }

    @SuppressWarnings("resource")
    @ParameterizedTest
    @ValueSource(ints = { SIZE_1K, SIZE_2K, SIZE_4K, SIZE_8K, SIZE_16K, SIZE_32K })
    void testSetPageSize_ValidValues(int pageSize) {
        FBManager m = FBTestProperties.createFBManager();

        assertDoesNotThrow(() -> m.setPageSize(pageSize));
    }

    @Test
    void testDialect3_dbCreatedWithRightDialect() throws Exception {
        try (FBManager m = createFBManager()) {
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
                    assertEquals("UTF8", StringUtils.trim(rs.getString(1)), "Unexpected default character set");
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

    private static FBManager createFBManager() throws Exception {
        return createFBManager(true);
    }

    private static FBManager createFBManager(boolean start) throws Exception {
        return configureFBManager(FBTestProperties.createFBManager(), start);
    }

}
