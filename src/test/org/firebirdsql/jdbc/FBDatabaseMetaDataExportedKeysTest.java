// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource
    void testExportedKeys(String table, List<Map<KeysMetaData, Object>> expectedKeys) throws Exception {
        try (ResultSet exportedKeys = dbmd.getExportedKeys(null, null, table)) {
            validateExpectedKeys(exportedKeys, expectedKeys);
        }
    }

    static Stream<Arguments> testExportedKeys() {
        return Stream.of(
                exportedKeysTestCase("TABLE_1", table2Fks()),
                exportedKeysTestCase("doesnotexist", List.of()),
                exportedKeysTestCase("TABLE_2", table3Fks(), table4Fks(), table5Fks(), table6Fks()),
                exportedKeysTestCase("TABLE_3", List.of()),
                exportedKeysTestCase("TABLE_6", table7Fks()));
    }

    private static Arguments exportedKeysTestCase(String table, List<Map<KeysMetaData, Object>> expectedKeys) {
        return Arguments.of(table, expectedKeys);
    }

    @SuppressWarnings("SameParameterValue")
    @SafeVarargs
    private static Arguments exportedKeysTestCase(String table, List<Map<KeysMetaData, Object>>... expectedKeys) {
        var combinedExpectedKeys = new ArrayList<Map<KeysMetaData, Object>>();
        Arrays.stream(expectedKeys).forEach(combinedExpectedKeys::addAll);
        return exportedKeysTestCase(table, combinedExpectedKeys);
    }

}
