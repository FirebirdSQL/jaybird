// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.firebirdsql.common.matchers.ComparableMatcherFactory.compares;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link BasicVersion} and parts of {@link org.firebirdsql.gds.AbstractVersion}.
 *
 * @author Mark Rotteveel
 */
class BasicVersionTest {

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            major, minor
            1,     0
            1,     5
            2,     1
            2,     5
            3,     0
            4,     0
            5,     0
            -1,    0
            0,     -1
            -2147483648, 0
            0,     -2147483648
            2147483647, 0
            0,     2147483647
            """)
    void versionReturnedByOf(int major, int minor) {
        var version = BasicVersion.of(major, minor);
        assertEquals(major, version.major(), "major");
        assertEquals(minor, version.minor(), "minor");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            op1Major, op1Minor, expectedComparison, op2Major, op2Minor
            1,        0,        ==,                 1,        0
            1,        0,        <,                  2,        0
            2,        0,        >,                  1,        0
            2,        0,        <,                  2,        1
            2,        1,        >,                  2,        0
            2,        5,        <,                  3,        1
            3,        1,        >,                  2,        5
            """)
    void compareTo(int op1Major, int op1Minor, String expectedComparison, int op2Major, int op2Minor) {
        assertThat(BasicVersion.of(op1Major, op1Minor),
                compares(expectedComparison, BasicVersion.of(op2Major, op2Minor)));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            major, minor, checkMajor, expectedResult
            1,     0,     0,          true
            0,     0,     1,          false
            1,     0,     1,          true
            1,     1,     1,          true
            2,     0,     1,          true
            2,     1,     1,          true
            1,     0,     2,          false
            """)
    void isEqualOrAbove_major(int major, int minor, int checkMajor, boolean expectedResult) {
        var version = BasicVersion.of(major, minor);

        assertEquals(expectedResult, version.isEqualOrAbove(checkMajor),
                "result of (" + version + ").isEqualOrAbove(" + checkMajor + ")");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            major, minor, checkMajor, checkMinor, expectedResult
            1,     0,     0,          0,          true
            0,     0,     1,          0,          false
            1,     0,     1,          0,          true
            1,     0,     1,          1,          false
            1,     1,     1,          0,          true
            1,     1,     1,          2,          false
            1,     1,     2,          0,          false
            2,     0,     1,          1,          true
            2,     1,     1,          1,          true
            1,     0,     2,          0,          false
            """)
    void isEqualOrAbove_major_minor(int major, int minor, int checkMajor, int checkMinor, boolean expectedResult) {
        var version = BasicVersion.of(major, minor);

        assertEquals(expectedResult, version.isEqualOrAbove(checkMajor, checkMinor),
                "result of (" + version + ").isEqualOrAbove(" + checkMajor + ',' + checkMinor + ")");
    }

}