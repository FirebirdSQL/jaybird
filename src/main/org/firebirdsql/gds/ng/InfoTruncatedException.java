// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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
