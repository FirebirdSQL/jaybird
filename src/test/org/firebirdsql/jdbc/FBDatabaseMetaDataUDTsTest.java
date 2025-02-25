// SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;

/**
 * Tests for {@link FBDatabaseMetaData} for UDT related metadata.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataUDTsTest {

    private static final MetadataResultSetDefinition getUDTsDefinition =
            new MetadataResultSetDefinition(UDTMetadata.class);

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

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
     * Tests the ordinal positions and types for the metadata columns of getUDTs().
     */
    @Test
    void testUDTMetaDataColumns() throws Exception {
        try (ResultSet udts = dbmd.getUDTs(null, null, null, null)) {
            getUDTsDefinition.validateResultSetColumns(udts);
        }
    }

    // As Firebird does not support UDTs no other tests are necessary

    /**
     * Columns defined for the getUDTs() metadata.
     */
    private enum UDTMetadata implements MetaDataInfo {
        TYPE_CAT(1, String.class),
        TYPE_SCHEM(2, String.class),
        TYPE_NAME(3, String.class),
        CLASS_NAME(4, String.class),
        DATA_TYPE(5, Integer.class),
        REMARKS(6, String.class),
        BASE_TYPE(7, Short.class);

        private final int position;
        private final Class<?> columnClass;

        UDTMetadata(int position, Class<?> columnClass) {
            this.position = position;
            this.columnClass = columnClass;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public Class<?> getColumnClass() {
            return columnClass;
        }
    }
}
