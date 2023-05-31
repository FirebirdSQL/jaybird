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
