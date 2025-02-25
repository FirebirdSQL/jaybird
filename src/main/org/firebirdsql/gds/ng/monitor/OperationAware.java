// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-FileCopyrightText: Copyright 2019 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.monitor;

import org.firebirdsql.gds.ng.OperationMonitor;

/**
 * Allows monitoring of driver operations like the execution of statements.
 * <p>
 * The notification of {@link #startOperation(Operation)} and {@link #endOperation(Operation)} occurs on the thread
 * performing the operation. Implementations of {@code OperationAware} should complete these methods as quick as
 * possible and prevent any blocking operations to avoid excessive performance degradation of the driver.
 * </p>
 * <p>
 * <b>Note</b>: This is an experimental feature. The implementation or API may be removed or changed at any time.
 * </p>
 *
 * @author Vasiliy Yashkov
 * @see OperationMonitor
 * @since 4.0
 */
public interface OperationAware {

    /**
     * Start of operation.
     *
     * @param operation
     *         operation.
     */
    void startOperation(Operation operation);

    /**
     * End of operation.
     *
     * @param operation
     *         operation.
     */
    void endOperation(Operation operation);

}
