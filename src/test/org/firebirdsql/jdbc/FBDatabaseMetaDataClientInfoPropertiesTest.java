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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBDatabaseMetaData#getClientInfoProperties()}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataClientInfoPropertiesTest {

    private static final MetadataResultSetDefinition getClientInfoPropertiesDefinition =
            new MetadataResultSetDefinition(ClientInfoPropertiesMetadata.class);

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
     * Tests the ordinal positions and types for the metadata columns of getClientInfoProperties().
     */
    @Test
    void testClientInfoPropertiesMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getClientInfoProperties()) {
            getClientInfoPropertiesDefinition.validateResultSetColumns(columns);
        }
    }

    @Test
    void testGetClientInfoProperties() throws Exception {
        List<Map<ClientInfoPropertiesMetadata, Object>> validationRules =
                getDefaultSupportInfo().supportsGetSetContext()
                        ? List.of(createRule("ApplicationName"), createRule("ClientHostname"), createRule("ClientUser"))
                        : emptyList();

        ResultSet clientInfoProperties = dbmd.getClientInfoProperties();
        validate(clientInfoProperties, validationRules);
    }

    private static void validate(ResultSet clientInfoProperties,
            List<Map<ClientInfoPropertiesMetadata, Object>> expectedClientInfoProperties) throws Exception {
        try {
            int columnCount = 0;
            while (clientInfoProperties.next()) {
                if (columnCount < expectedClientInfoProperties.size()) {
                    Map<ClientInfoPropertiesMetadata, Object> rules = expectedClientInfoProperties.get(columnCount);
                    getClientInfoPropertiesDefinition.checkValidationRulesComplete(rules);
                    getClientInfoPropertiesDefinition.validateRowValues(clientInfoProperties, rules);
                }
                columnCount++;
            }
            assertEquals(expectedClientInfoProperties.size(), columnCount, "Unexpected number of columns");
        } finally {
            closeQuietly(clientInfoProperties);
        }
    }

    private static Map<ClientInfoPropertiesMetadata, Object> createRule(String name) {
        Map<ClientInfoPropertiesMetadata, Object> rules = getDefaultValueValidationRules();
        rules.put(ClientInfoPropertiesMetadata.NAME, name);
        return rules;
    }

    private static final Map<ClientInfoPropertiesMetadata, Object> DEFAULT_COLUMN_VALUES;
    static {
        var defaults = new EnumMap<>(ClientInfoPropertiesMetadata.class);
        defaults.put(ClientInfoPropertiesMetadata.MAX_LEN, 32765);
        defaults.put(ClientInfoPropertiesMetadata.DEFAULT_VALUE, null);
        defaults.put(ClientInfoPropertiesMetadata.DESCRIPTION, MetaDataInfo.ANY_NON_NULL_VALUE);
        DEFAULT_COLUMN_VALUES = unmodifiableMap(defaults);
    }

    private static Map<ClientInfoPropertiesMetadata, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private enum ClientInfoPropertiesMetadata implements MetaDataInfo {
        NAME(1, String.class),
        MAX_LEN(2, Integer.class),
        DEFAULT_VALUE(3, String.class),
        DESCRIPTION(4, String.class);

        private final int position;
        private final Class<?> columnClass;

        ClientInfoPropertiesMetadata(int position, Class<?> columnClass) {
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
