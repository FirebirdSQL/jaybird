// SPDX-FileCopyrightText: Copyright 2023-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.Nullable;
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

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            firstValue, secondValue, expectedResult
            a,          b,           a
            a,          <NIL>,       a
            <NIL>,      b,           b
            <NIL>,      <NIL>,       <NIL>
            """)
    void firstNonNull_2arg(@Nullable String firstValue, @Nullable String secondValue, @Nullable String expectedResult) {
        assertEquals(expectedResult, ConditionalHelpers.firstNonNull(firstValue, secondValue));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            firstValue, secondValue, thirdValue, expectedResult
            a,          b,           c,          a
            a,          <NIL>,       c,          a
            <NIL>,      b,           c,          b
            <NIL>,      b,           <NIL>,      b
            <NIL>,      <NIL>,       c,          c
            <NIL>,      <NIL>,       <NIL>,      <NIL>
            """)
    void firstNonNull_3arg(@Nullable String firstValue, @Nullable String secondValue, @Nullable String thirdValue,
            @Nullable String expectedResult) {
        assertEquals(expectedResult, ConditionalHelpers.firstNonNull(firstValue, secondValue, thirdValue));
    }

}