// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assertions for testing fetchers.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class FetcherAssertions {

    private FetcherAssertions() {
        // no instances
    }

    static void assertBeforeFirst(FBFetcher fetcher) {
        assertAll(
                () -> assertTrue(fetcher.isBeforeFirst(), "expected before-first"),
                () -> assertFalse(fetcher.isAfterLast(), "expected not after-last"),
                () -> assertFalse(fetcher.isLast(), "expected not last"),
                () -> assertFalse(fetcher.isFirst(), "expected not first"),
                () -> assertEquals(0, fetcher.getRowNum(), "expected rowNum 0")
        );
    }

    static void assertAtRow(FBFetcher fetcher, int expectedPosition) {
        assertAll(
                () -> assertFalse(fetcher.isBeforeFirst(), "expected not before-first"),
                () -> assertFalse(fetcher.isAfterLast(), "expected not after-last"),
                () -> assertEquals(expectedPosition, fetcher.getRowNum(), () -> "expected rowNum " + expectedPosition)
        );
    }

    static void assertAfterLast(FBFetcher fetcher) {
        assertAll(
                () -> assertFalse(fetcher.isBeforeFirst(), "expected not before-first"),
                () -> assertTrue(fetcher.isAfterLast(), "expected after-last"),
                () -> assertFalse(fetcher.isLast(), "expected not last"),
                () -> assertFalse(fetcher.isFirst(), "expected not first"),
                () -> assertEquals(0, fetcher.getRowNum(), "expected rowNum 0 when after-last")
        );
    }

}
