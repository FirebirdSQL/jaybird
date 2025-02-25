// SPDX-FileCopyrightText: Copyright 2023-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link CollectionUtils}.
 */
class CollectionUtilsTest {

    @ParameterizedTest
    @MethodSource("listFactories")
    void growToSize_happyPath(Function<Collection<? extends String>, List<String>> listFactory) {
        List<String> list = listFactory.apply(List.of("item1"));

        CollectionUtils.growToSize(list, 4);

        assertEquals(Arrays.asList("item1", null, null, null), list);
    }

    @ParameterizedTest
    @MethodSource("listFactories")
    void growToSize_alreadyAtSize(Function<Collection<? extends String>, List<String>> listFactory) {
        List<String> list = listFactory.apply(List.of("item1", "item2", "item3", "item4"));

        CollectionUtils.growToSize(list, 4);

        assertEquals(Arrays.asList("item1", "item2", "item3", "item4"), list);
    }

    @ParameterizedTest
    @MethodSource("listFactories")
    void growToSize_alreadyLarger(Function<Collection<? extends String>, List<String>> listFactory) {
        List<String> list = listFactory.apply(List.of("item1", "item2", "item3", "item4", "item5"));

        CollectionUtils.growToSize(list, 4);

        assertEquals(Arrays.asList("item1", "item2", "item3", "item4", "item5"), list);
    }

    @SuppressWarnings("java:S5778")
    @Test
    void growToSize_fixedSizeList() {
        assertThrows(UnsupportedOperationException.class,
                () -> CollectionUtils.growToSize(Collections.singletonList("item1"), 4));
    }

    @SuppressWarnings("java:S5778")
    @Test
    void growToSize_immutableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> CollectionUtils.growToSize(List.of("item1"), 4));
    }

    @Test
    void getLast_emptyList() {
        assertNull(CollectionUtils.getLast(List.of()));
    }

    @Test
    void getLast_singleton() {
        String item1 = "item1";
        assertEquals(item1, CollectionUtils.getLast(List.of(item1)));
    }

    @Test
    void getLast_multipleItems() {
        String item1 = "item1";
        String item2 = "item2";

        assertEquals(item2, CollectionUtils.getLast(List.of(item1, item2)));
    }

    static Stream<Arguments> listFactories() {
        return Stream.of(
                Arguments.of(factory(ArrayList::new)),
                Arguments.of(factory(Vector::new)),
                Arguments.of(factory(LinkedList::new)));
    }

    private static <T> Function<Collection<? extends T>, List<T>> factory(
            Function<Collection<? extends T>, List<T>> f) {
        return f;
    }

}