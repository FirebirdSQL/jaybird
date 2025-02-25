// SPDX-FileCopyrightText: Copyright 2016-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link GDSFactory}
 *
 * @author Mark Rotteveel
 */
class GDSFactoryTest {

    @ParameterizedTest
    @CsvSource({
            "jdbc:firebirdsql://localhost/mydb,        PURE_JAVA",
            "jdbc:firebird://localhost/mydb,           PURE_JAVA",
            "jdbc:firebirdsql:localhost:mydb,          PURE_JAVA",
            "jdbc:firebirdsql:java://localhost/mydb,   PURE_JAVA",
            "jdbc:firebirdsql:native://localhost/mydb, NATIVE",
            "jdbc:firebird:native://localhost/mydb,    NATIVE",
            "jdbc:firebirdsql:native:localhost:mydb,   NATIVE",
            "jdbc:firebirdsql:embedded:mydb,           EMBEDDED",
            "jdbc:firebird:embedded:mydb,              EMBEDDED",
            "jdbc:firebirdsql:local:mydb,              NATIVE"
    })
    void testGetTypeForProtocol(String url, String expectedType) {
        assertEquals(expectedType, String.valueOf(GDSFactory.getTypeForProtocol(url)));
    }
}
