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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource
    void testImportedKeys(String table, List<Map<KeysMetaData, Object>> expectedKeys) throws Exception {
        try (ResultSet importedKeys = dbmd.getImportedKeys(null, null, table)) {
            validateExpectedKeys(importedKeys, expectedKeys);
        }
    }

    static Stream<Arguments> testImportedKeys() {
        return Stream.of(
                importedKeysTestCase("TABLE_1", table1Fks()),
                importedKeysTestCase("doesnotexist", List.of()),
                importedKeysTestCase("TABLE_2", table2Fks()),
                importedKeysTestCase("TABLE_3", table3Fks()),
                importedKeysTestCase("TABLE_4", table4Fks()),
                importedKeysTestCase("TABLE_5", table5Fks()),
                importedKeysTestCase("TABLE_6", table6Fks()),
                importedKeysTestCase("TABLE_7", table7Fks()));
    }

    private static Arguments importedKeysTestCase(String table, List<Map<KeysMetaData, Object>> expectedKeys) {
        return Arguments.of(table, expectedKeys);
    }

}
