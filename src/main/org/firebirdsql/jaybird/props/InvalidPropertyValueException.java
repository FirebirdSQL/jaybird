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
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;

/**
 * Exception thrown when a connection property has an invalid value.
 * <p>
 * This exception always has a {@link java.sql.SQLException} as cause.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class InvalidPropertyValueException extends IllegalArgumentException {

    private InvalidPropertyValueException(SQLException cause) {
        super(cause.getMessage(), cause);
    }

    public SQLException asSQLException() {
        return (SQLException) getCause();
    }

    public static InvalidPropertyValueException invalidProperty(String name, String value) {
        return invalidProperty(name, value, null);
    }

    public static InvalidPropertyValueException invalidProperty(String name, String value, String additionalDetails) {
        FbExceptionBuilder builder =
                FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidConnectionPropertyValue)
                        .messageParameter(value)
                        .messageParameter(name);
        if (additionalDetails != null) {
            builder.messageParameter(additionalDetails);
        }
        return new InvalidPropertyValueException(builder.toSQLException());
    }
}
