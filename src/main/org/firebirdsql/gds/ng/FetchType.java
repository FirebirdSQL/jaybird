// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

/**
 * Type of fetch. Parallels P_FETCH from Firebird.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public enum FetchType {

    // P_FETCH.fetch_next
    NEXT(0, true) {
        @Override
        public FetchDirection direction(int position) {
            return FetchDirection.FORWARD;
        }
    },
    // P_FETCH.fetch_prior
    PRIOR(1, true) {
        @Override
        public FetchDirection direction(int position) {
            return FetchDirection.REVERSE;
        }
    },
    // P_FETCH.fetch_first
    FIRST(2, false) {
        @Override
        public FetchDirection direction(int position) {
            // Direction can only be derived by knowing the current position
            return FetchDirection.UNKNOWN;
        }
    },
    // P_FETCH.fetch_last
    LAST(3, false) {
        @Override
        public FetchDirection direction(int position) {
            // Direction can only be derived by knowing the current position
            return FetchDirection.UNKNOWN;
        }
    },
    // P_FETCH.fetch_absolute
    ABSOLUTE(4, false) {
        @Override
        public FetchDirection direction(int position) {
            // Negative absolute work from the end backwards, so a "too big" value ends up before-start
            // 0 positions before-start, so we also consider it reverse
            return position <= 0 ? FetchDirection.REVERSE : FetchDirection.FORWARD;
        }
    },
    // P_FETCH.fetch_relative
    RELATIVE(5, false) {
        @Override
        public FetchDirection direction(int position) {
            if (position < 0) {
                return FetchDirection.REVERSE;
            } else if (position == 0) {
                return FetchDirection.IN_PLACE;
            } else {
                return FetchDirection.FORWARD;
            }
        }
    },
    ;

    private final int fbFetchType;
    private final boolean batch;

    FetchType(int fbFetchType, boolean batch) {
        this.fbFetchType = fbFetchType;
        this.batch = batch;
    }

    /**
     * @return Firebird fetch type (P_FETCH)
     */
    public int getFbFetchType() {
        return fbFetchType;
    }

    /**
     * @return {@code true} allows batched fetch (multiple rows), {@code false} if fetching only a single row is allowed
     */
    public boolean supportsBatch() {
        return batch;
    }

    /**
     * Determine the direction of a fetch with the specified position.
     *
     * @param position
     *         Position (only for {@link #ABSOLUTE} or {@link #RELATIVE})
     * @return Fetch direction
     */
    public abstract FetchDirection direction(int position);
}
