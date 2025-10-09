// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.stream.Stream;

import static org.firebirdsql.common.matchers.ComparableMatcherFactory.compares;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            # minimum allowed
            0,     0
            # maximum allowed
            65535, 65535
            """)
    void versionReturnedByOf(int major, int minor) {
        var version = BasicVersion.of(major, minor);
        assertEquals(major, version.major(), "major");
        assertEquals(minor, version.minor(), "minor");
    }

    @Test
    void withMajor() {
        assertEquals(BasicVersion.of(2, 5), BasicVersion.of(1, 5).withMajor(2));
    }

    @Test
    void withMinor() {
        assertEquals(BasicVersion.of(2, 1), BasicVersion.of(2, 5).withMinor(1));
    }

    @ParameterizedTest
    @MethodSource
    void keyStriping(int major, int minor, int expectedKey) throws Throwable {
        var lookup = MethodHandles.privateLookupIn(BasicVersion.class, MethodHandles.lookup());
        MethodHandle keyMethod =
                lookup.findStatic(BasicVersion.class, "key", MethodType.methodType(int.class, int.class, int.class));
        int key = (int) keyMethod.invoke(major, minor);
        assertEquals(expectedKey, key,
                "expected %s, received: %s".formatted(keyAsString(expectedKey), keyAsString(key)));
    }

    static Stream<Arguments> keyStriping() {
        //@formatting:off
        return Stream.of(
                Arguments.of(0b1000_0000_0000_0001, 0b1100_0000_0000_0011, 0b1000_0000_0001_1000_0000_0000_0110_0001),
                Arguments.of(0xFFFF,                0,                     0b1111_1111_1110_0000_0000_0000_0001_1111),
                Arguments.of(0,                     0xFFFF,                0b0000_0000_0001_1111_1111_1111_1110_0000),
                Arguments.of(1,                     0,                     1),
                Arguments.of(1,                     5,                     0b0000_0000_0000_0000_0000_0000_1010_0001),
                Arguments.of(2,                     0,                     2),
                Arguments.of(2,                     1,                     0b0000_0000_0000_0000_0000_0000_0010_0010),
                Arguments.of(2,                     5,                     0b0000_0000_0000_0000_0000_0000_1010_0010),
                Arguments.of(3,                     0,                     3),
                Arguments.of(4,                     0,                     4),
                Arguments.of(5,                     0,                     5),
                Arguments.of(31,                    3,                     0b0000_0000_0000_0000_0000_0000_0111_1111));
        //@formatting:one
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
            major, minor
            -1,    0
            0,     -1
            -1,    -1
            65536, 0
            0,     65536
            65536, 65536
            """)
    void of_outOfRangeMajorMinor(int major, int minor) {
        assertThrows(IllegalArgumentException.class, () -> BasicVersion.of(major, minor));
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

    private static String keyAsString(int key) {
        return Long.toString(key & 0x0FFFFFFFFL, 2);
    }

}