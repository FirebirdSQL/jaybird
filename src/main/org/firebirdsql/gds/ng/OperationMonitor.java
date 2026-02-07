// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-FileCopyrightText: Copyright 2019-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.monitor.Operation;
import org.firebirdsql.gds.ng.monitor.OperationAware;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLPermission;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The statement operation monitor allows monitoring of statement execution and fetching of all statements.
 *
 * @author Vasiliy Yashkov
 * @author Mark Rotteveel
 * @since 4.0
 */
@NullMarked
public final class OperationMonitor {

    private static final SQLPermission PERMISSION_INIT_OPERATION_AWARE =
            new SQLPermission("org.firebirdsql.jaybird.initOperationAware");

    private static final AtomicReference<OperationAware> instance = new AtomicReference<>(NoOpOperationAware.INSTANCE);

    private OperationMonitor() {
        // no instances
    }

    /**
     * Signal current registered instance of {@link OperationAware} of the start of an operation.
     *
     * @param operation
     *         operation
     */
    static void startOperation(Operation operation) {
        OperationAware operationAware = instance.get();
        operationAware.startOperation(operation);
    }

    /**
     * Signal current registered instance of {@link OperationAware} of the end of an operation.
     *
     * @param operation
     *         operation
     */
    static void endOperation(Operation operation) {
        OperationAware operationAware = instance.get();
        operationAware.endOperation(operation);
    }

    /**
     * Initializes this operation monitor.
     * <p>
     * The provided {@link OperationAware} instance will be notified of operations occurring in the driver.
     * </p>
     * <p>
     * When a security manager is installed, the existence of the {@link SQLPermission} with name
     * {@code "org.firebirdsql.jaybird.initOperationAware"} will be checked.
     * </p>
     *
     * @param operationAware
     *         an object that implements the {@link OperationAware} interface, or {@code null} to clear the current
     *         instance
     * @throws SecurityException
     *         If a security manager is installed and the calling code does not have permission {@code
     *         "org.firebirdsql.jaybird.initOperationAware"}
     */
    @SuppressWarnings("removal")
    public static void initOperationAware(@Nullable OperationAware operationAware) {
        var sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(PERMISSION_INIT_OPERATION_AWARE);
        }
        instance.set(operationAware != null ? operationAware : NoOpOperationAware.INSTANCE);
    }

    /**
     * Implementation of {@link OperationAware} that does nothing (a 'null-object').
     *
     * @author Mark Rotteveel
     * @since 4.0
     */
    private static final class NoOpOperationAware implements OperationAware {

        private static final NoOpOperationAware INSTANCE = new NoOpOperationAware();

        private NoOpOperationAware() {
            // no extra instances
        }

        @Override
        public void startOperation(Operation operation) {
            // do nothing
        }

        @Override
        public void endOperation(Operation operation) {
            // do nothing
        }

    }
}
