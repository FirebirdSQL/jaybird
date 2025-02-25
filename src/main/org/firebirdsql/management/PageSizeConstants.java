// SPDX-FileCopyrightText: Copyright 2016-2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import java.util.Arrays;

/**
 * Constants for page size supported by Firebird.
 * <p>
 * Note that some page size might not be supported by all Firebird version.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@SuppressWarnings("unused")
public final class PageSizeConstants {

    public static final int SIZE_1K = 1024;
    public static final int SIZE_2K = 2 * SIZE_1K;
    public static final int SIZE_4K = 4 * SIZE_1K;
    public static final int SIZE_8K = 8 * SIZE_1K;
    public static final int SIZE_16K = 16 * SIZE_1K;
    /**
     * Firebird 4 or higher
     */
    public static final int SIZE_32K = 32 * SIZE_1K;

    private static final int[] ALLOWED_PAGE_SIZES = { SIZE_1K, SIZE_2K, SIZE_4K, SIZE_8K, SIZE_16K, SIZE_32K };

    private PageSizeConstants() {
        // No instantiation
    }

    /**
     * Checks if {@code pageSize} is a valid page size value.
     * <p>
     * Actual support of a page size depends on the Firebird version, even if a page size is valid according to this
     * method, it can still be invalid for the actual Firebird version used.
     * </p>
     *
     * @param pageSize
     *         Page size to check
     * @return {@code pageSize} (unmodified)
     * @throws IllegalArgumentException
     *         if the page size is not a valid value
     * @since 3.0.5
     */
    public static int requireValidPageSize(int pageSize) {
        if (Arrays.binarySearch(ALLOWED_PAGE_SIZES, pageSize) < 0) {
            throw new IllegalArgumentException("Page size must be one of " + Arrays.toString(ALLOWED_PAGE_SIZES));
        }
        return pageSize;
    }
}
