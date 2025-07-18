// SPDX-FileCopyrightText: Copyright 2023-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * Helper class for collections
 *
 * @author Mark Rotteveel
 * @since 6
 */
@NullMarked
public final class CollectionUtils {

    private CollectionUtils() {
        // no instances
    }

    /**
     * Grows the size of {@code list} to {@code size} by padding it with {@code null} to the requested size.
     * <p>
     * If the size of the list is already {@code size} or larger, it will not be modified
     * </p>
     *
     * @param list
     *         list to grow
     * @param size
     *         desired size
     * @throws NullPointerException
     *         if {@code list} is {@code null}, or does not accept {@code null} as a value
     * @throws UnsupportedOperationException
     *         if {@code list} is unmodifiable or fixed size and its current size is less than {@code size}
     */
    @SuppressWarnings("java:S1149")
    public static void growToSize(final List<?> list, final int size) {
        if (list.size() >= size) return;
        if (list instanceof ArrayList<?> a) {
            // avoid inefficient resizes by ensuring capacity
            a.ensureCapacity(size);
        } else if (list instanceof Vector<?> v) {
            // use vectors built-in feature
            v.setSize(size);
            return;
        }
        while (list.size() < size) {
            list.add(null);
        }
    }

    /**
     * Returns the last item of a list, or {@code null} if the list is empty.
     *
     * @param list
     *         list
     * @param <T>
     *         type of the list
     * @return last item (which may be {@code null}), or {@code null} if the list is empty
     * @throws NullPointerException
     *         if {@code list} is {@code null}
     */
    public static <T extends @Nullable Object> @Nullable T getLast(final List<T> list) {
        int size = list.size();
        return size > 0 ? list.get(size - 1) : null;
    }

    /**
     * Concatenates two lists to a new modifiable list.
     *
     * @param list1
     *         list 1
     * @param list2
     *         list 2
     * @param <T>
     *         type parameter of {@code list1}, and parent type parameter of {@code list2}
     * @return concatenation of {@code list1} and {@code list2}
     */
    public static <T> List<T> concat(List<T> list1, List<? extends T> list2) {
        var newList = new ArrayList<T>(list1.size() + list2.size());
        newList.addAll(list1);
        newList.addAll(list2);
        return newList;
    }

    /**
     * Concatenates two or more lists to a new modifiable list.
     * <p>
     * If there are no lists in {@code otherLists}, it will return a new list, with the contents of {@code list1}.
     * </p>
     *
     * @param list1
     *         list 1
     * @param otherLists
     *         other lists
     * @param <T>
     *         type parameter of {@code list1}, and parent type parameter of lists in {@code otherLists}
     * @return concatenation of {@code list1} and {@code otherLists}
     * @see #concat(List, List)
     */
    @SafeVarargs
    public static <T> List<T> concat(List<T> list1, List<? extends T>... otherLists) {
        int listsSize = list1.size() + Stream.of(otherLists).mapToInt(List::size).sum();
        var newList = new ArrayList<T>(listsSize);
        newList.addAll(list1);
        for (var list : otherLists) {
            newList.addAll(list);
        }
        return newList;
    }

}
