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
package org.firebirdsql.jaybird.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a {@link SQLException} with an unchecked exception.
 * <p>
 * Use to bridge Java's functional APIs using functional interfaces which don't throw a checked exception.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class UncheckedSQLException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7363534668503435705L;

    public UncheckedSQLException(SQLException sqlException) {
        super(requireNonNull(sqlException));
    }

    @Override
    @SuppressWarnings("java:S3551")
    public SQLException getCause() {
        return (SQLException) super.getCause();
    }

    @java.io.Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Throwable cause = super.getCause();
        if (!(cause instanceof SQLException)) {
            throw new InvalidObjectException("Cause must be a SQLException");
        }
    }

}
