// SPDX-FileCopyrightText: Copyright 2024-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;

/**
 * Tests for {@link FBDatabaseMetaData#getCrossReference(String, String, String, String, String, String)}.
 * 
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataCrossReferenceTest extends FBDatabaseMetaDataAbstractKeysTest {

    // TODO Add schema support: tests involving other schema

    @Test
    void testCrossReferenceMetaDataColumns() throws Exception {
        try (ResultSet crossReference = dbmd.getCrossReference(
                null, null, "doesnotexist", null, null, "doesnotexist")) {
            keysDefinition.validateResultSetColumns(crossReference);
        }
    }

    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource
    void testCrossReference(String parentTable, String foreignTable, List<Map<KeysMetaData, Object>> expectedKeys)
            throws Exception {
        try (ResultSet crossReference = dbmd.getCrossReference(null, ifSchemaElse("PUBLIC", ""), parentTable, null,
                ifSchemaElse("PUBLIC", ""), foreignTable)) {
            validateExpectedKeys(crossReference, expectedKeys);
        }
    }

    static Stream<Arguments> testCrossReference() {
        return Stream.of(
                crossRefTestCase("TABLE_1", "TABLE_2", table2Fks()),
                crossRefTestCase("TABLE_2", "TABLE_1", List.of()),
                crossRefTestCase("TABLE_1", "TABLE_3", List.of()),
                crossRefTestCase("TABLE_2", "TABLE_3", table3Fks()),
                crossRefTestCase("TABLE_2", "TABLE_4", table4Fks()),
                crossRefTestCase("TABLE_2", "TABLE_5", table5Fks()),
                crossRefTestCase("TABLE_2", "TABLE_6", table6Fks()),
                crossRefTestCase("TABLE_6", "TABLE_7", table7Fks()),
                crossRefTestCase("TABLE_1", "doesnotexist", List.of()),
                crossRefTestCase("doesnotexist", "TABLE_2", List.of()));
    }

    private static Arguments crossRefTestCase(String parentTable, String foreignTable,
            List<Map<KeysMetaData, Object>> expectedKeys) {
        return Arguments.of(parentTable, foreignTable, expectedKeys);
    }

}
