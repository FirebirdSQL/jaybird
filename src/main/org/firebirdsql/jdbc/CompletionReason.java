/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc;

/**
 * Reasons for statement (or other resources) completion. This is intended for the {@link InternalTransactionCoordinator}
 * to notify the statement and related objects on why it should complete.
 * <p>
 * TODO: This is a bit of kludge to fix <a href="http://tracker.firebirdsql.org/browse/JDBC-304">JDBC-304</a> in 2.2.x, might need some more polish for 3.0
 * </p>
 * @since 2.2.3
 */
public enum CompletionReason {
    COMMIT {
        @Override
        boolean isTransactionEnd() {
            return true;
        }
    },
    ROLLBACK{
        @Override
        boolean isTransactionEnd() {
            return true;
        }
    },
    OTHER;

    /**
     * @return {@code true} if this completion indicates a transaction end
     */
    boolean isTransactionEnd() {
        return false;
    }
}
