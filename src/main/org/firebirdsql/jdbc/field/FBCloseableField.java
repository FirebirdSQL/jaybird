// SPDX-FileCopyrightText: Copyright 2020 Vasiliy Yashkov
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import java.sql.SQLException;

/**
 * Instances of this field have open resources and must be cleaned up.
 *
 * @author Vasiliy Yashkov
 * @since 5
 */
public interface FBCloseableField {

    /**
     * Close this field. This method tells field implementation to release all
     * resources allocated when field methods were called.
     *
     * @throws SQLException
     *             if field cannot be closed.
     */
    void close() throws SQLException;
}
