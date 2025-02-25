// SPDX-FileCopyrightText: Copyright 2019 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

/**
 * Close handle for {@link org.firebirdsql.gds.ng.monitor.Operation} implementations.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public interface OperationCloseHandle extends AutoCloseable {

    @Override
    void close();

    /**
     * @return {@code true} if the operation was cancelled
     */
    boolean isCancelled();

}
