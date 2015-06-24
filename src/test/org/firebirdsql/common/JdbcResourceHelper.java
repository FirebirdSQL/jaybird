/*
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
package org.firebirdsql.common;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper class for closing JDBC resources, ignoring all SQLExceptions.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class JdbcResourceHelper {

    private JdbcResourceHelper() {
    }

    /**
     * Helper method to quietly close statements.
     * 
     * @param stmt
     *            Statement object
     */
    public static void closeQuietly(Statement stmt) {
        if (stmt == null) {
            return;
        }
        try {
            stmt.close();
        } catch (SQLException ex) {
            // ignore
        }
    }

    /**
     * Helper method to quietly close connections.
     * 
     * @param con
     *            Connection object
     */
    public static void closeQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.close();
        } catch (SQLException ex) {
            // ignore
        }
    }

    /**
     * Helper method to quietly close resultsets.
     * 
     * @param rs
     *            ResultSet object
     */
    public static void closeQuietly(ResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.close();
        } catch (SQLException ex) {
            // ignore
        }
    }

    /**
     * Helper method to quietly close pooled connections.
     * 
     * @param con
     *            PooledConnection object
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
