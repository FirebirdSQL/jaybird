// SPDX-FileCopyrightText: Copyright 2023-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class NameHelperTest {

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            catalog,     schema, routineName,  expectedSpecificName
            <null>,      <null>, ROUTINE,      ROUTINE
            <null>,      PUBLIC, ROUTINE,      "PUBLIC"."ROUTINE"
            PACKAGE,     <null>, ROUTINE,      "PACKAGE"."ROUTINE"
            PACKAGE,     PUBLIC, ROUTINE,      "PUBLIC"."PACKAGE"."ROUTINE"
            WITH"DOUBLE, <null>, DOUBLE"QUOTE, "WITH""DOUBLE"."DOUBLE""QUOTE"
            WITH"DOUBLE, PUBLIC, DOUBLE"QUOTE, "PUBLIC"."WITH""DOUBLE"."DOUBLE""QUOTE"
            """, nullValues = "<null>")
    void testToSpecificName(String catalog, String schema, String routineName, String expectedResult) {
        assertEquals(expectedResult, NameHelper.toSpecificName(catalog, schema, routineName));
    }

}