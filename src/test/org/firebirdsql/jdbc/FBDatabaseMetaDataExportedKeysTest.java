// SPDX-FileCopyrightText: Copyright 2024-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;

/**
 * Tests for {@link java.sql.DatabaseMetaData#getExportedKeys(String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataExportedKeysTest extends FBDatabaseMetaDataAbstractKeysTest {

    @Test
    void testExportedKeysMetaDataColumns() throws Exception {
        try (ResultSet exportedKeys = dbmd.getExportedKeys(null, null, "doesnotexit")) {
            keysDefinition.validateResultSetColumns(exportedKeys);
        }
    }

    @ParameterizedTest(name = "({0}, {1})")
    @MethodSource
    void testExportedKeys(String schema, String table, List<Map<KeysMetaData, Object>> expectedKeys) throws Exception {
        try (ResultSet exportedKeys = dbmd.getExportedKeys(null, schema, table)) {
            validateExpectedKeys(exportedKeys, expectedKeys);
        }
    }

    static Stream<Arguments> testExportedKeys() {
        var generalArguments = Stream.of(
                exportedKeysTestCase("TABLE_1", ifSchemaElse(table8Fks(), List.of()), table2Fks()),
                exportedKeysTestCase(null, "TABLE_1", ifSchemaElse(table8Fks(), List.of()), table2Fks()),
                exportedKeysTestCase("doesnotexist", List.of()),
                exportedKeysTestCase("TABLE_2", table3Fks(), table4Fks(), table5Fks(), table6Fks()),
                exportedKeysTestCase("TABLE_3", List.of()),
                exportedKeysTestCase("TABLE_6", table7to6Fks()));
        if (!getDefaultSupportInfo().supportsSchemas()) {
            return generalArguments;
        }
        return Stream.concat(generalArguments, Stream.of(
                exportedKeysTestCase("OTHER_SCHEMA", "TABLE_8", table7to8Fks()),
                exportedKeysTestCase(null, "TABLE_8", table7to8Fks())));
    }

    private static Arguments exportedKeysTestCase(String table, List<Map<KeysMetaData, Object>> expectedKeys) {
        return exportedKeysTestCase(ifSchemaElse("PUBLIC", ""), table, expectedKeys);
    }

    private static Arguments exportedKeysTestCase(String schema, String table,
            List<Map<KeysMetaData, Object>> expectedKeys) {
        return Arguments.of(schema, table, expectedKeys);
    }

    @SafeVarargs
    private static Arguments exportedKeysTestCase(String table, List<Map<KeysMetaData, Object>>... expectedKeys) {
        return exportedKeysTestCase(ifSchemaElse("PUBLIC", ""), table, expectedKeys);
    }

    @SafeVarargs
    private static Arguments exportedKeysTestCase(String schema, String table,
            List<Map<KeysMetaData, Object>>... expectedKeys) {
        var combinedExpectedKeys = Arrays.stream(expectedKeys).flatMap(Collection::stream).toList();
        return exportedKeysTestCase(schema, table, combinedExpectedKeys);
    }

}
