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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Helper class for collections
 *
 * @author Mark Rotteveel
 * @since 6
 */
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
}
