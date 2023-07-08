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
package org.firebirdsql.jaybird.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mark Rotteveel
 */
class ConditionalHelpersTest {

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            firstValue, secondValue, expectedResult
            0,          0,           0
            0,          2,           2
            1,          2,           1
            1,          0,           1
            2,          1,           2
            -1,         -2,          -1
            -2,         -1,          -2
            """)
    void firstNonZero_2arg(int firstValue, int secondValue, int expectedResult) {
        assertEquals(expectedResult, ConditionalHelpers.firstNonZero(firstValue, secondValue));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            firstValue, secondValue, thirdValue, expectedResult
            0,          0,           0,          0
            0,          0,           3,          3
            0,          2,           3,          2
            1,          2,           3,          1
            1,          0,           3,          1
            1,          2,           0,          1
            3,          2,           1,          3
            -3,         -2,          -1,         -3
            -1,         -2,          -3,         -1
            """)
    void firstNonZero_3arg(int firstValue, int secondValue, int thirdValue, int expectedResult) {
        assertEquals(expectedResult, ConditionalHelpers.firstNonZero(firstValue, secondValue, thirdValue));
    }

}