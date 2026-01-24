// SPDX-FileCopyrightText: Copyright 2017-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.util.BasicVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mark Rotteveel
 */
class FirebirdVersionMetaDataTest {

    @Test
    void validateOrderOfEnumConstants() {
        FirebirdVersionMetaData[] sortedReservedWords = FirebirdVersionMetaData.values();
        Arrays.sort(sortedReservedWords, Comparator.comparing(FirebirdVersionMetaData::version).reversed());
        assertArrayEquals(sortedReservedWords, FirebirdVersionMetaData.values(),
                "Expected order of FirebirdVersionMetaData.values() to be descending by version");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            major, minor, expected
            6,     0,     FIREBIRD_5_0
            5,     0,     FIREBIRD_5_0
            4,     0,     FIREBIRD_4_0
            3,     0,     FIREBIRD_3_0
            2,     5,     FIREBIRD_2_5
            2,     1,     FIREBIRD_2_1
            2,     0,     FIREBIRD_2_0
            # lower versions should return the oldest known version
            1,     5,     FIREBIRD_2_0
            1,     0,     FIREBIRD_2_0
            # higher versions should return the latest known version
            9999,  0,     FIREBIRD_5_0
            """)
    void getVersionMetaDataFor(int major, int minor, FirebirdVersionMetaData expected) {
        assertEquals(expected, FirebirdVersionMetaData.getVersionMetaDataFor(BasicVersion.of(major, minor)),
                () -> "Unexpected FirebirdVersionMetaData for %d.%d".formatted(major, minor));
    }

}
