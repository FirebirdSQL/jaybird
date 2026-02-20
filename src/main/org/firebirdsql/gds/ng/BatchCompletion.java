// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.fb.constants.BatchItems;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Completion data from a batch execute.
 *
 * @param elementCount
 *         number of <em>elements</em> (batch row values) processed by the server
 * @param updateCounts
 *         Update counts per <em>element</em>.
 *         <p>
 *         Reports either the update count, {@code -1} ({@link BatchItems#BATCH_EXECUTE_FAILED} if the element resulted
 *         in an error (<b>NOTE:</b> JDBC uses {@code -3} ({@link Statement#EXECUTE_FAILED}!), or {@code -2}
 *         ({@link BatchItems#BATCH_SUCCESS_NO_INFO}) for success without update count (equivalent to
 *         {@link Statement#SUCCESS_NO_INFO}).
 *         </p>
 *         <p>
 *         The array is empty if {@code TAG_RECORD_COUNTS} was not requested.
 *         </p>
 *         <p>
 *         Note that contrary to JDBC, if {@code TAG_MULTIERROR} is not requested, the last update count will be
 *         {@code -1} for the failed record (JDBC expects the update counts to end <em>before</em> the first failure).
 *         </p>
 * @param detailedErrors
 *         Detailed errors per failed <em>element</em>, reporting the failed element number and {@code SQLException}.
 *         <p>
 *         Will have at most 1 error if {@code TAG_MULTIERROR} is not requested.
 *         </p>
 *         <p>
 *         By default, at most 64 detailed errors will be reported (at most 256 can be requested with
 *         {@code TAG_DETAILED_ERRORS}). The remaining errors will be reported in {@code simplifiedErrors}. The limits
 *         and defaults mentioned are as of Firebird 4 and are not enforced by Jaybird.
 *         </p>
 * @param simplifiedErrors
 *         Simplified errors, reports failed <em>element</em> number, when error count exceeds the maximum detailed
 *         errors
 * @author Mark Rotteveel
 * @since 5
 */
public record BatchCompletion(int elementCount, int[] updateCounts, List<DetailedError> detailedErrors,
        int[] simplifiedErrors) {

    // NOTE: We intentionally do not clone or copy the arrays and lists for efficiency

    /**
     * @return {@code true} if there are any errors, {@code false} otherwise
     */
    public boolean hasErrors() {
        return !detailedErrors.isEmpty() || simplifiedErrors.length > 0;
    }

    /**
     * Detailed error information of batch execution.
     *
     * @param element
     *         0-based index of the <em>element</em> with this error
     * @param error
     *         error as a {@code SQLException}
     */
    public record DetailedError(int element, SQLException error) {
    }

}
