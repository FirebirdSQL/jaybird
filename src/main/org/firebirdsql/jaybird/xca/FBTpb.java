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
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.gds.TransactionParameterBuffer;

import java.io.Serial;
import java.io.Serializable;

import static org.firebirdsql.jaybird.fb.constants.TpbItems.*;

/**
 * Represents the Firebird Transaction Parameter Block (TPB), which contains Firebird-specific information about
 * transaction isolation.
 *
 * @author David Jencks
 */
public final class FBTpb implements Serializable {

    @Serial
    private static final long serialVersionUID = 966376319390599431L;

    @SuppressWarnings("java:S1948")
    private TransactionParameterBuffer transactionParams;

    /**
     * Create a new Transaction Parameters Block instance based around a {@link TransactionParameterBuffer}.
     *
     * @param transactionParams
     *         instance of {@link TransactionParameterBuffer} representing transaction parameters.
     */
    public FBTpb(TransactionParameterBuffer transactionParams) {
        this.transactionParams = transactionParams;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FBTpb other)) return false;
        return transactionParams.equals(other.transactionParams);
    }

    public int hashCode() {
        return transactionParams.hashCode();
    }

    /**
     * Set the read-only flag on this TPB.
     *
     * @param readOnly
     *         If {@code true}, this TPB will be set to read-only, otherwise it will be read-write
     */
    public void setReadOnly(boolean readOnly) {
        transactionParams.removeArgument(isc_tpb_read);
        transactionParams.removeArgument(isc_tpb_write);

        transactionParams.addArgument(readOnly ? isc_tpb_read : isc_tpb_write);
    }

    /**
     * Determine whether this TPB is set to read-only.
     *
     * @return {@code true} if this TPB is read-only, otherwise {@code false}
     */
    public boolean isReadOnly() {
        return transactionParams.hasArgument(isc_tpb_read);
    }

    public TransactionParameterBuffer getTransactionParameterBuffer() {
        return transactionParams;
    }

    public void setTransactionParameterBuffer(TransactionParameterBuffer tpb) {
        this.transactionParams = tpb;
    }
}
