// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link StringDeduplicator}.
 */
class StringDeduplicatorTest {

    @Test
    void deduplicate() {
        var deduplicator = StringDeduplicator.of();

        final String value1 = "value1";
        final String value1Copy = copyOf(value1);

        // Check our assumptions (indirectly test copyOf(...))
        assumeEqualNotSameInstance(value1, value1Copy, "value1Copy should be a distinct instance");

        assertSame(value1, deduplicator.get(value1));
        assertSame(value1, deduplicator.get(value1Copy));
    }

    @Test
    void deduplicateToPreset() {
        var deduplicator = StringDeduplicator.of("PRESET_1", "PRESET_2");

        assertSame("PRESET_1", deduplicator.get(copyOf("PRESET_1")));
        assertSame("PRESET_2", deduplicator.get(copyOf("PRESET_2")));
    }

    @Test
    void eviction() {
        final int maxCapacity = 5;
        var deduplicator = StringDeduplicator.of(maxCapacity, List.of());

        final String value1 = "value1";
        final String value1Copy = copyOf(value1);

        assertSame(value1, deduplicator.get(value1), value1);
        // Deduplicate other values to evict value1
        IntStream.rangeClosed(2, maxCapacity + 1).forEach(i -> {
            String value = "value" + i;
            assertSame(value, deduplicator.get(value), value);
        });

        assertSame(value1Copy, deduplicator.get(value1Copy), "expected value1Copy as value1 has been evicted");
    }

    @Test
    void deduplicateEmptyString_toLiteralEmpty() {
        var deduplicator = StringDeduplicator.of();

        final String emptyCopy = copyOf("");
        assumeEqualNotSameInstance("", emptyCopy, "emptyCopy should be a distinct instance");

        assertSame("", deduplicator.get(emptyCopy), "should deduplicate to empty literal instance, not copy");
    }

    @ParameterizedTest
    @ValueSource(ints = { -2, -1, 0 })
    void cannotCreateWithMaxCapacityZeroOrSmaller(int maxCapacity) {
        assertThrows(IllegalArgumentException.class, () -> StringDeduplicator.of(maxCapacity, List.of()));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 10 })
    void canCreateWithMaxCapacityOneOrGreater(int maxCapacity) {
        assertDoesNotThrow(() -> StringDeduplicator.of(maxCapacity, List.of()));
    }

    /**
     * Creates a distinct copy of {@code value}.
     *
     * @param value
     *         value to copy
     * @return distinct copy of {@code value} (i.e. {@code new String(value)})
     */
    @SuppressWarnings("StringOperationCanBeSimplified")
    private static String copyOf(String value) {
        return new String(value);
    }

    private void assumeEqualNotSameInstance(String v1, String v2, String message) {
        assumeThat(message, v2,
                allOf(equalTo(v1), not(sameInstance(v1))));
    }

}