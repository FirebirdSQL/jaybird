// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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