// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.io.Serial;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_invalidConnectionPropertyValue;

/**
 * Exception thrown when a connection property has an invalid value.
 * <p>
 * This exception always has a {@link java.sql.SQLException} as cause.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class InvalidPropertyValueException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 8836337024456816650L;

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
        FbExceptionBuilder builder = FbExceptionBuilder.forException(jb_invalidConnectionPropertyValue)
                .messageParameter(value, name);
        if (additionalDetails != null) {
            builder.messageParameter(additionalDetails);
        }
        return new InvalidPropertyValueException(builder.toSQLException());
    }
}
