// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.fb.constants.BatchItems;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Completion data from a batch execute.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class BatchCompletion {

    private final int elementCount;
    private final int[] updateCounts;
    private final List<DetailedError> detailedErrors;
    // Values represent the message numbers with an error, without details (status vector),
    // happens when too many errors occur
    private final int[] simplifiedErrors;

    public BatchCompletion(int elementCount, int[] updateCounts, List<DetailedError> detailedErrors,
            int[] simplifiedErrors) {
        this.elementCount = elementCount;
        this.updateCounts = updateCounts;
        this.detailedErrors = detailedErrors;
        this.simplifiedErrors = simplifiedErrors;
    }

    /**
     * @return number of <em>elements</em> (batch row values) processed by the server.
     */
    public int elementCount() {
        return elementCount;
    }

    /**
     * Update counts per <em>element</em>.
     * <p>
     * Reports either the update count, {@code -1} ({@link BatchItems#BATCH_EXECUTE_FAILED} if the element resulted in
     * an error (<b>NOTE:</b> JDBC uses {@code -3} ({@link Statement#EXECUTE_FAILED}!), or {@code -2}
     * ({@link BatchItems#BATCH_SUCCESS_NO_INFO}) for success without update count (equivalent to
     * {@link Statement#SUCCESS_NO_INFO}).
     * </p>
     * <p>
     * The array is empty if {@code TAG_RECORD_COUNTS} was not requested.
     * </p>
     * <p>
     * Note that contrary to JDBC, if {@code TAG_MULTIERROR} is not requested, the last update count will be {@code -1}
     * for the failed record (JDBC expects the update counts to end <em>before</em> the first failure).
     * </p>
     *
     * @return update counts
     */
    public int[] updateCounts() {
        return updateCounts;
    }

    /**
     * Detailed errors per failed <em>element</em>, reporting the failed element number and {@code SQLException}.
     * <p>
     * Will have at most 1 error if {@code TAG_MULTIERROR} is not requested.
     * </p>
     * <p>
     * By default, at most 64 detailed errors will be reported (at most 256 can be requested
     * with {@code TAG_DETAILED_ERRORS}). The remaining errors will be reported in {@code simplifiedErrors}. The
     * limits and defaults mentioned are as of Firebird 4 and not enforced by Jaybird.
     * </p>
     *
     * @return detailed errors
     */
    public List<DetailedError> detailedErrors() {
        return detailedErrors;
    }

    /**
     * Simplified errors, reports failed <em>element</em> number, when error count exceeds the maximum detailed errors.
     *
     * @return rows with errors without detailed error information
     */
    public int[] simplifiedErrors() {
        return simplifiedErrors;
    }

    /**
     * @return {@code true} if there are any errors, {@code false} otherwise
     */
    public boolean hasErrors() {
        return !detailedErrors.isEmpty() || simplifiedErrors.length > 0;
    }

    public static final class DetailedError {

        private final int element;
        private final SQLException error;

        public DetailedError(int element, SQLException error) {
            this.element = element;
            this.error = error;
        }

        /**
         * @return 0-based index of the <em>element</em> with this error
         */
        public int element() {
            return element;
        }

        /**
         * @return error as a {@code SQLException}
         */
        public SQLException error() {
            return error;
        }
    }
}
