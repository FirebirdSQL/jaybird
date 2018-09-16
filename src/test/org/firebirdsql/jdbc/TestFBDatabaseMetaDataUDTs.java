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

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;

/**
 * Tests for {@link FBDatabaseMetaData} for UDT related metadata.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataUDTs {

    private static final MetaDataTestSupport<UDTMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(UDTMetaData.class);

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeClass
    public static void setUp() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
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
    public void testUDTMetaDataColumns() throws Exception {
        try (ResultSet udts = dbmd.getUDTs(null, null, null, null)) {
            metaDataTestSupport.validateResultSetColumns(udts);
        }
    }

    // As Firebird does not support UDTs no other tests are necessary

    /**
     * Columns defined for the getUDTs() metadata.
     */
    private enum UDTMetaData implements MetaDataInfo {
        TYPE_CAT(1, String.class),
        TYPE_SCHEM(2, String.class),
        TYPE_NAME(3, String.class),
        CLASS_NAME(4, String.class),
        DATA_TYPE(5, Integer.class),
        REMARKS(6, String.class),
        BASE_TYPE(7, Short.class);

        private final int position;
        private final Class<?> columnClass;

        UDTMetaData(int position, Class<?> columnClass) {
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

        @Override
        public MetaDataValidator<?> getValidator() {
            return new MetaDataValidator<>(this);
        }

    }
}
