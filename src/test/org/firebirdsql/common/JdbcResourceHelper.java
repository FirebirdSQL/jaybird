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
package org.firebirdsql.common;

import java.sql.SQLException;

import javax.sql.PooledConnection;

/**
 * Helper class for closing JDBC resources, ignoring all SQLExceptions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class JdbcResourceHelper {

    private JdbcResourceHelper() {
    }

    /**
     * Helper method to quietly close a resource.
     *
     * @param resource
     *         resource to close
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Helper method to quietly close multiple resources.
     *
     * @param resources
     *         resources to close
     */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            closeQuietly(resource);
        }
    }

    /**
     * Helper method to quietly close pooled connections.
     *
     * @param con
     *         PooledConnection object
     */
    public static void closeQuietly(PooledConnection con) {
        if (con == null) {
            return;
        }
        try {
            con.close();
        } catch (SQLException ex) {
            // ignore
        }
    }

}
