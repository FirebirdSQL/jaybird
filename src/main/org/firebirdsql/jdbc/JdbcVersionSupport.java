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

import java.sql.BatchUpdateException;

/**
 * Interface to mediate between differences in JDBC versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface JdbcVersionSupport {

    /**
     * Constructs a {@link java.sql.BatchUpdateException}.
     * <p>
     * For JDBC versions < 4.2, the {@code updateCounts} can be converted to integers without taking overflow into
     * account.
     * </p>
     *
     * @param reason
     *         A description of the error
     * @param SQLState
     *         SQL state of the error
     * @param vendorCode
     *         Vendor specific error code (use {@code 0} if there is no specific error code)
     * @param updateCounts
     *         An array of {@code long} update counts
     * @param cause
     *         Underlying cause (may be {@code null}).
     * @return The created BatchUpdateException.
     */
    BatchUpdateException createBatchUpdateException(String reason, String SQLState, int vendorCode, long[] updateCounts,
            Throwable cause);
}
