// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.monitor;

import java.sql.SQLException;

/**
 * An operation of the driver.
 * <p>
 * In current implementations, this represents either a statement execution or a fetch.
 * </p>
 * <p>
 * <b>Note</b>: This is an experimental feature. The implementation or API may be removed or changed at any time.
 * </p>
 *
 * @author Vasiliy Yashkov
 * @author Mark Rotteveel
 * @since 4
 */
public interface Operation {

    /**
     * @return The type of operation.
     */
    Operation.Type getType();

    /**
     * Cancel this operation.
     *
     * @throws SQLException
     *         If the cancellation failed or if this operation is no longer cancellable.
     */
    void cancel() throws SQLException;

    /**
     * Type of operation.
     */
    enum Type {
        STATEMENT_EXECUTE,
        /**
         * Synchronous fetch from statement cursor.
         */
        STATEMENT_FETCH,
        /**
         * Asynchronous fetch from statement cursor: sending request for rows.
         *
         * @since 6
         */
        STATEMENT_ASYNC_FETCH_START,
        /**
         * Asynchronous fetch from statement cursor: receiving response with rows.
         * <p>
         * This operation is not cancellable.
         * </p>
         *
         * @since 6
         */
        STATEMENT_ASYNC_FETCH_COMPLETE(false),
        ;

        private final boolean cancellable;

        Type() {
            this(true);
        }

        Type(boolean cancellable) {
            this.cancellable = cancellable;
        }

        public boolean isCancellable() {
            return cancellable;
        }
    }
}
