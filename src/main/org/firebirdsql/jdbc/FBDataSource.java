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
    @Override
    public void setReference(Reference ref) {
        this.jndiReference = ref;
    }

    /**
     * Get the JNDI <code>Reference</code> for this DataSource.
     *
     * @return The JNDI reference
     */
    @Override
    public Reference getReference() {
        return jndiReference;
    }

    @SuppressWarnings("deprecation")
    @Override
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

    @SuppressWarnings("deprecation")
    @Override
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

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

}
