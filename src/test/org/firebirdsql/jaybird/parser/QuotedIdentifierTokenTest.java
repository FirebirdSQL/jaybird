// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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