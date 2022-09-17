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
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class GenericTokenTest {

    @ParameterizedTest
    @CsvSource({
            "a,            true",
            "A,            true",
            "AB,           true",
            "abc,          true",
            "A3,           true",
            "a3a,          true",
            "A$,           true",
            "a$a,          true",
            "A_,           true",
            "a_A,          true",
            "RDB$RELATION, true",
            "$,            false",
            "$A,           false",
            "_,            false",
            "_A,           false",
            "A\u00e8,      false",
            "3a,           false",
            // Would not normally occur as GenericToken
            "3,            false"
    })
    void isValidIdentifier(String tokenText, boolean expectedValid) {
        GenericToken genericToken = new GenericToken(0, tokenText);

        assertThat(genericToken.isValidIdentifier()).isEqualTo(expectedValid);
    }

}