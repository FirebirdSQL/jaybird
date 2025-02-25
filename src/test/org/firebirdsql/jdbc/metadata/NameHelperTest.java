// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class NameHelperTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            <null>,      ROUTINE,      ROUTINE
            PACKAGE,     ROUTINE,      "PACKAGE"."ROUTINE"
            WITH"DOUBLE, DOUBLE"QUOTE, "WITH""DOUBLE"."DOUBLE""QUOTE"
            """, nullValues = "<null>")
    void testToSpecificName(String catalog, String routineName, String expectedResult) {
        assertEquals(expectedResult, NameHelper.toSpecificName(catalog, routineName));
    }

}