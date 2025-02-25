// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

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

/**
 * Tests for {@link OdsVersion}.
 *
 * @author Mark Rotteveel
 */
class OdsVersionTest {

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            major, minor
            10,    0
            11,    0
            12,    0
            13,    0
            13,    1
            13,    2
            """)
    void odsVersionReturnedByOf(int major, int minor) {
        var odsVersion = OdsVersion.of(major, minor);
        assertEquals(major, odsVersion.major(), "major");
        assertEquals(minor, odsVersion.minor(), "minor");
    }

    @Test
    void withMajor() {
        assertEquals(OdsVersion.of(11, 2), OdsVersion.of(15, 2).withMajor(11));
    }

    @Test
    void withMinor() {
        assertEquals(OdsVersion.of(13, 1), OdsVersion.of(13, 2).withMinor(1));
    }

    @ParameterizedTest
    @MethodSource
    void keyStriping(int major, int minor, int expectedKey) throws Throwable {
        var lookup = MethodHandles.privateLookupIn(OdsVersion.class, MethodHandles.lookup());
        MethodHandle keyMethod =
                lookup.findStatic(OdsVersion.class, "key", MethodType.methodType(int.class, int.class, int.class));
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
                Arguments.of(10,                    0,                     10),
                Arguments.of(13,                    0,                     0b0000_0000_0000_0000_0000_0000_0000_1101),
                Arguments.of(13,                    1,                     0b0000_0000_0000_0000_0000_0000_0010_1101),
                Arguments.of(13,                    2,                     0b0000_0000_0000_0000_0000_0000_0100_1101),
                Arguments.of(13,                    3,                     0b0000_0000_0000_0000_0000_0000_0110_1101),
                Arguments.of(13,                    4,                     0b0000_0000_0000_0000_0000_0000_1000_1101),
                Arguments.of(31,                    3,                     0b0000_0000_0000_0000_0000_0000_0111_1111));
        //@formatting:one
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            op1Major, op1Minor, expectedComparison, op2Major, op2Minor
            10,       0,        ==,                 10,       0
            10,       0,        <,                  11,       0
            11,       0,        >,                  10,       0
            11,       0,        <,                  11,       1
            11,       1,        >,                  11,       0
            11,       2,        <,                  13,       1
            13,       1,        >,                  11,       2
            """)
    void compareTo(int op1Major, int op1Minor, String expectedComparison, int op2Major, int op2Minor) {
        assertThat(OdsVersion.of(op1Major, op1Minor), compares(expectedComparison, OdsVersion.of(op2Major, op2Minor)));
    }

    private static String keyAsString(int key) {
        return Long.toString(key & 0x0FFFFFFFFL, 2);
    }

}