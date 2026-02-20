// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Functional interface to process an information buffer (responses to p_info_* requests) returning an object of type T.
 *
 * @param <T>
 *         type of the result of the {@link #process(byte[])} method.
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface InfoProcessor<T extends @Nullable Object> {

    /**
     * Process an infoResponse block into an object of type T.
     *
     * @param infoResponse
     *         byte array containing the server response to an info-request.
     * @return Processed response of type T (usually - but not required - a newly created object).
     * @throws InfoTruncatedException
     *         (optional) if {@code infoResponse} is truncated and this processor could not recover by itself
     * @throws SQLException
     *         for errors during processing the infoResponse.
     */
    T process(byte[] infoResponse) throws SQLException;

    /**
     * Interface for information on a statement.
     *
     * @author Mark Rotteveel
     * @since 3.0
     */
    interface StatementInfo {
        /**
         * @return The Statement Type
         */
        StatementType getStatementType();

        /**
         * @return RowDescriptor of the fields of the statement, never null
         */
        RowDescriptor getFields();

        /**
         * @return RowDescriptor of the parameters of the statement, never null
         */
        RowDescriptor getParameters();
    }
}
