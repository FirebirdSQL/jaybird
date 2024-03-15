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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void trimToNull_null_yields_null() {
        assertNull(StringUtils.trimToNull(null));
    }

    @Test
    void trimToNull_empty_yields_null() {
        assertNull(StringUtils.trimToNull(""));
    }

    @Test
    void trimToNull_blank_yields_null() {
        assertNull(StringUtils.trimToNull(" "));
    }

    @Test
    void trimToNull_nonEmptyWithoutSpaceSuffixPrefix_yields_value() {
        final String value = "Without space as prefix or suffix";
        assertEquals(value, StringUtils.trimToNull(value));
    }

    @Test
    void trimToNull_startsWithSpace_yields_valueWithoutSpace() {
        final String value = " starts with space";
        final String expectedValue = "starts with space";
        assertEquals(expectedValue, StringUtils.trimToNull(value));
    }

    @Test
    void trimToNull_endsWithSpace_yields_valueWithoutSpace() {
        final String value = "ends with space ";
        final String expectedValue = "ends with space";
        assertEquals(expectedValue, StringUtils.trimToNull(value));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void testIsNullOrEmpty_nullOrEmptyYieldsTrue(String value) {
        assertTrue(StringUtils.isNullOrEmpty(value));
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "a", "\0", "abc" })
    void testIsNullOrEmpty_nonEmptyYieldsFalse(String value) {
        assertFalse(StringUtils.isNullOrEmpty(value));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            input, expectedOutput
            ,
            '',    ''
            ' ',   ''
            'a ',  a
            ' a',  a
            ' a ', a
            """)
    void testTrim(String input, String expectedOutput) {
        assertEquals(expectedOutput, StringUtils.trim(input));
    }

}