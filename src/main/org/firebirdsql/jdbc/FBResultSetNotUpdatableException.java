// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2016-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import java.io.Serial;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_CURSOR_STATE;

/**
 * Exception is thrown when trying to modify the non-updatable result set.
 */
public class FBResultSetNotUpdatableException extends SQLNonTransientException {

    @Serial
    private static final long serialVersionUID = 675357382993364256L;

    /**
     * Create default instance of this class.
     */
    public FBResultSetNotUpdatableException() {
        this("Underlying ResultSet is not updatable.");
    }

    /**
     * Create instance of this class for the specified message.
     *
     * @param message
     *         message to display.
     */
    public FBResultSetNotUpdatableException(String message) {
        super(message, SQL_STATE_INVALID_CURSOR_STATE);
    }

}
