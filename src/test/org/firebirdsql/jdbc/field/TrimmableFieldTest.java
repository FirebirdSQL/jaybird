// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TrimmableFieldTest {

    @ParameterizedTest(name = "[{index}] [{0}] => [{1}]")
    @CsvSource({
            ",",
            "'', ''",
            "' ', ''",
            "'  ', ''",
            "a, a",
            "'a ', a",
            "' a', ' a'",
            "' a ', ' a'",
            "'a  ', a",
            "'abc   ', abc"
    })
    void testTrimTrailing(String input, String expectedOutput) {
        assertEquals(expectedOutput, TrimmableField.trimTrailing(input));
    }

}