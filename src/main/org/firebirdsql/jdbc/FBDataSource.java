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
package org.firebirdsql.jdbc;

import org.firebirdsql.ds.RootCommonDataSource;
import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBManagedConnectionFactory;

import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The class <code>FBDataSource</code> is a ConnectionFactory for jdbc
 * Connection objects.  All work is delegated to a ConnectionManager.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBDataSource extends RootCommonDataSource implements DataSource, Serializable, Referenceable {

    private static final long serialVersionUID = 1178461472062969634L;

    private final ConnectionManager cm;

    private final FBManagedConnectionFactory mcf;

    private Reference jndiReference;

    private int loginTimeout = 0;

    // this constructor is needed to make BES happy.
    public FBDataSource(ManagedConnectionFactory mcf, ConnectionManager cm) {
        this((FBManagedConnectionFactory) mcf, cm);
    }

    public FBDataSource(FBManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    /**
     * Set the JNDI <code>Reference</code> for this DataSource.
     *
     * @param ref
     *         The JNDI reference for this DataSource
     */
    public void setReference(Reference ref) {
        this.jndiReference = ref;
    }

    /**
     * Get the JNDI <code>Reference</code> for this DataSource.
     *
     * @return The JNDI reference
     */
    public Reference getReference() {
        return jndiReference;
    }

    /**
     * <p>Attempt to establish a database connection.
     *
     * @return a Connection to the database
     * @throws SQLException
     *         if a database-access error occurs.
     */
    @SuppressWarnings("deprecation")
    public Connection getConnection() throws SQLException {
        try {
            return (Connection) cm.allocateConnection(mcf, mcf.getDefaultConnectionRequestInfo());
        } catch (ResourceException re) {
            if (re.getCause() instanceof SQLException) {
                throw (SQLException) re.getCause();
            }
            if (re.getLinkedException() instanceof SQLException) {
                throw (SQLException) re.getLinkedException();
            }
            throw new FBSQLException(re);
        }
    }

    /**
     * <p>Attempt to establish a database connection.
     *
     * @param username
     *         the database user on whose behalf the Connection is
     *         being made
     * @param password
     *         the user's password
     * @return a Connection to the database
     * @throws SQLException
     *         if a database-access error occurs.
     */
    @SuppressWarnings("deprecation")
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            //mcf makes a copy for us.
            FBConnectionRequestInfo subjectCri = mcf.getDefaultConnectionRequestInfo();
            subjectCri.setUserName(username);
            subjectCri.setPassword(password);
            return (Connection) cm.allocateConnection(mcf, subjectCri);
        } catch (ResourceException re) {
            if (re.getCause() instanceof SQLException) {
                throw (SQLException) re.getCause();
            }
            if (re.getLinkedException() instanceof SQLException) {
                throw (SQLException) re.getLinkedException();
            }
            throw new FBSQLException(re);
        }
    }

    /**
     * <p>Sets the maximum time in seconds that this data source will wait
     * while attempting to connect to a database.  A value of zero
     * specifies that the timeout is the default system timeout
     * if there is one; otherwise it specifies that there is no timeout.
     * When a DataSource object is created the login timeout is
     * initially zero.
     *
     * @param seconds
     *         the data source login time limit
     * @throws SQLException
     *         if a database access error occurs.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        loginTimeout = seconds;
    }


    /**
     * Gets the maximum time in seconds that this data source can wait
     * while attempting to connect to a database.  A value of zero
     * means that the timeout is the default system timeout
     * if there is one; otherwise it means that there is no timeout.
     * When a DataSource object is created the login timeout is
     * initially zero.
     *
     * @return the data source login time limit
     * @throws SQLException
     *         if a database access error occurs.
     */
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    // JDBC 4.0

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

}





