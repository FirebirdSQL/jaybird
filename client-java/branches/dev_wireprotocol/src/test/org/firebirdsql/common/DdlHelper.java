/*
 * $Id$
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Helper class for executing DDL while ignoring certain errors.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class DdlHelper {

    private DdlHelper() {
    }

    public static void executeCreateTable(Connection connection, String sql) throws SQLException {
        DdlHelper.executeDDL(connection, sql, new int[] { ISCConstants.isc_no_meta_update });
    }

    public static void executeDDL(Connection connection, String sql, int[] ignoreErrors)
            throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute(sql);
            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            if (ignoreErrors == null || ignoreErrors.length == 0)
                throw ex;

            boolean ignoreException = false;

            int errorCode = ex.getErrorCode();
            Throwable current = ex;
            errorcodeloop: do {
                for (int i = 0; i < ignoreErrors.length; i++) {
                    if (ignoreErrors[i] == errorCode) {
                        ignoreException = true;
                        break errorcodeloop;
                    }
                }
                if (current instanceof GDSException) {
                    current = ((GDSException) current).getNext();
                } else {
                    current = current.getCause();
                }
                if (current == null || !(current instanceof GDSException)) {
                    break;
                } else {
                    errorCode = ((GDSException) current).getFbErrorCode();
                }
            } while (errorCode != -1);

            if (!ignoreException)
                throw ex;
        }
    }

    public static void executeDropTable(Connection connection, String sql) throws SQLException {
        executeDDL(connection, sql, DdlHelper.getDropIgnoreErrors(connection));
    }

    private static int[] getDropIgnoreErrors(Connection connection) throws SQLException {
        GDSHelper gdsHelper;
        try {
            gdsHelper = ((FBConnection) connection).getGDSHelper();
            if (gdsHelper.compareToVersion(2, 0) < 0) {
                // Firebird 1.5 and earlier do not always return specific error
                // codes
                return new int[] { ISCConstants.isc_dsql_error, ISCConstants.isc_no_meta_update,
                        ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found };
            } else {
                return new int[] { ISCConstants.isc_no_meta_update,
                        ISCConstants.isc_dsql_table_not_found, ISCConstants.isc_dsql_view_not_found };
            }
        } catch (GDSException e) {
            throw new FBSQLException(e);
        }
    }

}
