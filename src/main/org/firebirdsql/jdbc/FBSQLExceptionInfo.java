// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import java.io.Serial;
import java.sql.SQLException;

/**
 * Subclass of SQLException whose only purpose is to preserve the information provided by the status vector when an
 * exception is created using {@link org.firebirdsql.gds.ng.FbExceptionBuilder#toFlatSQLException()}.
 * <p>
 * Instances of this exception have an empty stack trace.
 * </p>
 * <p>
 * Objects of this type are chained using the {@link #setNextException(java.sql.SQLException)}
 * facility. Further information can be obtained by iterating over this object, or by using {@link #getNextException()}.
 * </p>
 */
public class FBSQLExceptionInfo extends SQLException {

    @Serial
    private static final long serialVersionUID = -686564056327297128L;

    public FBSQLExceptionInfo(String reason, String sqlState, int vendorCode) {
        super(reason, sqlState, vendorCode);
    }

    @Override
    @SuppressWarnings("java:S3551")
    public Throwable fillInStackTrace() {
        // Not filling in stacktrace as this is purely informational
        return this;
    }
}
