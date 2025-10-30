// SPDX-FileCopyrightText: Copyright 2024-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;

/**
 * Tests for {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataImportedKeysTest extends FBDatabaseMetaDataAbstractKeysTest {

    @Test
    void testExportedKeysMetaDataColumns() throws Exception {
        try (ResultSet importedKeys = dbmd.getImportedKeys(null, null, "doesnotexit")) {
            keysDefinition.validateResultSetColumns(importedKeys);
        }
    }

    @ParameterizedTest(name = "({0}, {1})")
    @MethodSource
    void testImportedKeys(String schema, String table, List<Map<KeysMetaData, Object>> expectedKeys) throws Exception {
        try (ResultSet importedKeys = dbmd.getImportedKeys(null, schema, table)) {
            validateExpectedKeys(importedKeys, expectedKeys);
        }
    }

    static Stream<Arguments> testImportedKeys() {
        var generalArguments = Stream.of(
                importedKeysTestCase("TABLE_1", table1Fks()),
                importedKeysTestCase(null, "TABLE_1", table1Fks()),
                importedKeysTestCase("doesnotexist", List.of()),
                importedKeysTestCase("TABLE_2", table2Fks()),
                importedKeysTestCase(null, "TABLE_2", table2Fks()),
                importedKeysTestCase("TABLE_3", table3Fks()),
                importedKeysTestCase("TABLE_4", table4Fks()),
                importedKeysTestCase("TABLE_5", table5Fks()),
                importedKeysTestCase("TABLE_6", table6Fks()),
                importedKeysTestCase("TABLE_7", ifSchemaElse(table7to8Fks(), List.of()), table7to6Fks()));
        if (!getDefaultSupportInfo().supportsSchemas()) {
            return generalArguments;
        }
        return Stream.concat(generalArguments, Stream.of(
                importedKeysTestCase("OTHER_SCHEMA", "TABLE_8", table8Fks()),
                importedKeysTestCase(null, "TABLE_8", table8Fks())));
    }

    private static Arguments importedKeysTestCase(String table, List<Map<KeysMetaData, Object>> expectedKeys) {
        return importedKeysTestCase(ifSchemaElse("PUBLIC", ""), table, expectedKeys);
    }

    private static Arguments importedKeysTestCase(String schema, String table,
            List<Map<KeysMetaData, Object>> expectedKeys) {
        return Arguments.of(schema, table, expectedKeys);
    }

    @SuppressWarnings("SameParameterValue")
    @SafeVarargs
    private static Arguments importedKeysTestCase(String table, List<Map<KeysMetaData, Object>>... expectedKeys) {
        return importedKeysTestCase(ifSchemaElse("PUBLIC", ""), table, expectedKeys);
    }

    @SafeVarargs
    private static Arguments importedKeysTestCase(String schema, String table,
            List<Map<KeysMetaData, Object>>... expectedKeys) {
        var combinedExpectedKeys = Stream.of(expectedKeys).flatMap(Collection::stream).toList();
        return importedKeysTestCase(schema, table, combinedExpectedKeys);
    }

}
