/*
 * $Id$
 *
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
