/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.management;

import java.util.Arrays;

/**
 * Constants for page size supported by Firebird.
 * <p>
 * Note that some page size might not be supported by all Firebird version.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
