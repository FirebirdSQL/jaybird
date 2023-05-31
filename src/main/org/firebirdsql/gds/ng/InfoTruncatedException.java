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

import java.sql.SQLNonTransientException;

/**
 * Thrown to indicate that an info request buffer was truncated ({@code isc_info_truncated}).
 * <p>
 * Implementations of {@link InfoProcessor} may throw this exception if they cannot recover themselves from truncation.
 * The length of the truncated buffer is reported in {@link #length()}; this is the <em>actual</em> length, not the
 * <em>requested</em> length.
 * </p>
 * <p>
 * This is a subclass of {@link SQLNonTransientException} as retrying the operation without taking corrective action
 * will likely result in the same error.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5.0.2
 */
public class InfoTruncatedException extends SQLNonTransientException {

    private final int length;

    public InfoTruncatedException(String message, int length) {
        super(message);
        this.length = length;
    }

    public final int length() {
        return length;
    }

}
