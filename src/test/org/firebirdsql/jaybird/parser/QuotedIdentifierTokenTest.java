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

class QuotedIdentifierTokenTest {

    @ParameterizedTest
    @CsvSource({
            "\"name\",                       name",
            "\"with\"\"double\",             with\"double",
            "\"with\"\"multiple\"\"double\", with\"multiple\"double"
    })
    void quotedIdentifier(String input, String expectedName) {
        QuotedIdentifierToken token = new QuotedIdentifierToken(0, input);

        assertThat(token.text()).describedAs("text").isEqualTo(input);
        assertThat(token.name()).describedAs("name").isEqualTo(expectedName);
        assertThat(token.isValidIdentifier()).describedAs("validIdentifier").isTrue();
    }

}