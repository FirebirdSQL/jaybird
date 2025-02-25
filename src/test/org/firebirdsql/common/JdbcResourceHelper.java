// SPDX-FileCopyrightText: Copyright 2012-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import java.sql.SQLException;

import javax.sql.PooledConnection;

/**
 * Helper class for closing JDBC resources, ignoring all SQLExceptions.
 *
 * @author Mark Rotteveel
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
