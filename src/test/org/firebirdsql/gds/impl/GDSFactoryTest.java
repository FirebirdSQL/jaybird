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
package org.firebirdsql.gds.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link GDSFactory}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class GDSFactoryTest {

    @ParameterizedTest
    @CsvSource({
            "jdbc:firebirdsql://localhost/mydb,        PURE_JAVA",
            "jdbc:firebird://localhost/mydb,           PURE_JAVA",
            "jdbc:firebirdsql:localhost:mydb,          PURE_JAVA",
            "jdbc:firebirdsql:java://localhost/mydb,   PURE_JAVA",
            "jdbc:firebirdsql:oo://localhost/mydb,     OOREMOTE",
            "jdbc:firebird:oo://localhost/mydb,        OOREMOTE",
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
