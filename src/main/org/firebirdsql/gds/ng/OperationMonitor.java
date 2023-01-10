/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.monitor.Operation;
import org.firebirdsql.gds.ng.monitor.OperationAware;

import java.sql.SQLPermission;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The statement operation monitor allows monitoring of statement execution and fetching of all statements.
 *
 * @author Vasiliy Yashkov
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class OperationMonitor {

    private static final String PERMISSION_INIT_OPERATION_AWARE = "org.firebirdsql.jaybird.initOperationAware";

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
    public static void initOperationAware(OperationAware operationAware) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            SQLPermission permission = new SQLPermission(PERMISSION_INIT_OPERATION_AWARE);
            securityManager.checkPermission(permission);
        }
        instance.set(operationAware != null
                ? operationAware
                : NoOpOperationAware.INSTANCE);
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
