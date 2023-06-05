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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for {@link FBDatabaseMetaData#getCatalogs()}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataCatalogsTest {

    private static final MetadataResultSetDefinition getCatalogsDefinition =
            new MetadataResultSetDefinition(CatalogMetaData.class);

    private static final String CREATE_PACKAGE_ONE = """
            create package ONE
            as
            begin
              function IN$PACKAGE(PARAM1 integer) returns INTEGER;
            end""";

    private static final String CREATE_PACKAGE_BODY_ONE = """
            create package body ONE
            as
            begin
              function IN$PACKAGE(PARAM1 integer) returns INTEGER
              as
              begin
                return PARAM1 + 1;
              end
            end""";

    private static final String CREATE_PACKAGE_TWO = """
            create package TWO
            as
            begin
              function IN$PACKAGE(PARAM1 integer) returns INTEGER;
            end""";

    private static final String CREATE_PACKAGE_BODY_TWO = """
            create package body TWO
            as
            begin
              function IN$PACKAGE(PARAM1 integer) returns INTEGER
              as
              begin
                return PARAM1 + 1;
              end
            end""";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
            dbmd = null;
        }
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getCatalogs().
     */
    @Test
    void testCatalogMetaDataColumns() throws Exception {
        try (ResultSet catalogs = dbmd.getCatalogs()) {
            getCatalogsDefinition.validateResultSetColumns(catalogs);
        }
    }

    @Test
    void testCatalogMetaData_defaultEmpty() throws Exception {
        try (ResultSet catalogs = dbmd.getCatalogs()) {
            assertFalse(catalogs.next(), "expected no rows for getCatalogs()");
        }
    }

    @Test
    void testCatalogMetaData_useCatalogAsPackage() throws Exception {
        Properties props = FBTestProperties.getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        var expectedCatalogNames = new ArrayList<String>();
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsPackages()) {
            expectedCatalogNames.add("ONE");
            expectedCatalogNames.add("TWO");
            if (supportInfo.isVersionEqualOrAbove(4)) {
                expectedCatalogNames.add("RDB$TIME_ZONE_UTIL");
            }
            if (supportInfo.isVersionEqualOrAbove(5)) {
                expectedCatalogNames.add("RDB$BLOB_UTIL");
                expectedCatalogNames.add("RDB$PROFILER");
            }
            Collections.sort(expectedCatalogNames);
        }
        try (var connection = DriverManager.getConnection(FBTestProperties.getUrl(), props)) {
            var dbmd = connection.getMetaData();
            var receivedCatalogNames = new ArrayList<String>();
            try (ResultSet catalogs = dbmd.getCatalogs()) {
                while (catalogs.next()) {
                    receivedCatalogNames.add(catalogs.getString(1));
                }
            }

            assertEquals(expectedCatalogNames, receivedCatalogNames, "Unexpected catalog names (package names)");
        }
    }

    private static List<String> getCreateStatements() {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        List<String> statements = new ArrayList<>();
        if (supportInfo.supportsPackages()) {
            statements.addAll(List.of(
                    CREATE_PACKAGE_ONE,
                    CREATE_PACKAGE_BODY_ONE,
                    CREATE_PACKAGE_TWO,
                    CREATE_PACKAGE_BODY_TWO));
        }
        return statements;
    }

    private enum CatalogMetaData implements MetaDataInfo {
        TABLE_CAT;

        @Override
        public int getPosition() {
            return 1;
        }

        @Override
        public Class<?> getColumnClass() {
            return String.class;
        }
    }

}
