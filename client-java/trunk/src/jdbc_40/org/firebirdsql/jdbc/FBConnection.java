/*
 * $Id$
 * 
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
package org.firebirdsql.jdbc;

import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.util.SQLExceptionChainBuilder;

/**
 * Firebird connection class implementing JDBC 3.0 methods.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnection extends AbstractConnection {

    private HashSet clientInfoPropNames = new HashSet();

    /**
     * Create instance of this class for the specified managed connection.
     * 
     * @param mc
     *            managed connection.
     */
    public FBConnection(FBManagedConnection mc) {
        super(mc);
    }

    // -------------------------------------------------------------------------
    // JDBC 4.0
    // -------------------------------------------------------------------------
    
    public NClob createNClob() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    public SQLXML createSQLXML() throws SQLException {
        throw new FBDriverNotCapableException();
    }

    private static final String GET_CLIENT_INFO_SQL = 
        "SELECT "
            + "    rdb$get_context('USER_SESSION', ?) session_context "
            + "  , rdb$get_context('USER_TRANSACTION', ?) tx_context " 
            + "FROM rdb$database";

    private static final String SET_CLIENT_INFO_SQL = 
        "SELECT "
            + "  rdb$set_context('USER_SESSION', ?, ?) session_context " 
            + "FROM rdb$database";

    public Properties getClientInfo() throws SQLException {
        Properties result = new Properties();

        PreparedStatement stmt = prepareStatement(GET_CLIENT_INFO_SQL);
        try {
            for (Iterator iterator = clientInfoPropNames.iterator(); iterator.hasNext();) {
                String propName = (String) iterator.next();
                result.put(propName, getClientInfo(stmt, propName));
            }
        } finally {
            stmt.close();
        }

        return result;
    }

    public String getClientInfo(String name) throws SQLException {
        PreparedStatement stmt = prepareStatement(GET_CLIENT_INFO_SQL);
        try {
            return getClientInfo(stmt, name);
        } finally {
            stmt.close();
        }
    }

    protected String getClientInfo(PreparedStatement stmt, String name) throws SQLException {
        stmt.clearParameters();

        stmt.setString(1, name);
        stmt.setString(2, name);

        ResultSet rs = stmt.executeQuery();
        try {
            if (!rs.next())
                return null;

            String sessionContext = rs.getString(1);
            String transactionContext = rs.getString(2);

            if (transactionContext != null)
                return transactionContext;
            else if (sessionContext != null)
                return sessionContext;
            else
                return null;

        } finally {
            rs.close();
        }

    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        SQLExceptionChainBuilder<SQLClientInfoException> chain = new SQLExceptionChainBuilder<SQLClientInfoException>();

        try {
            PreparedStatement stmt = prepareStatement(SET_CLIENT_INFO_SQL);
            try {

                for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();

                    try {
                        setClientInfo(stmt, (String) entry.getKey(), (String) entry.getValue());
                    } catch (SQLClientInfoException ex) {
                        chain.append(ex);
                    }

                }
            } finally {
                stmt.close();
            }

        } catch (SQLException ex) {
            throw new SQLClientInfoException(ex.getMessage(), ex.getSQLState(), null, ex);
        }

        if (chain.hasException())
            throw chain.getException();
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try {
            PreparedStatement stmt = prepareStatement(SET_CLIENT_INFO_SQL);
            try {
                setClientInfo(stmt, name, value);
            } finally {
                stmt.close();
            }

        } catch (SQLException ex) {
            throw new SQLClientInfoException(ex.getMessage(), ex.getSQLState(), null, ex);
        }
    }

    protected void setClientInfo(PreparedStatement stmt, String name, String value)
            throws SQLException {
        try {
            stmt.clearParameters();
            stmt.setString(1, name);
            stmt.setString(2, value);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                throw new FBDriverConsistencyCheckException(
                        "Expected result from RDB$SET_CONTEXT call");

            // needed, since the value is set on fetch!!!
            rs.getInt(1);

        } catch (SQLException ex) {
            throw new SQLClientInfoException(null, ex);
        }
    }

    public void abort(Executor executor) throws SQLException {
        // TODO Write implementation
        checkValidity();
        throw new FBDriverNotCapableException();
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        // TODO Write implementation
        checkValidity();
        throw new FBDriverNotCapableException();
    }

    public int getNetworkTimeout() throws SQLException {
        // TODO Write implementation
        checkValidity();
        return 0;
    }
}
