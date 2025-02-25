// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
