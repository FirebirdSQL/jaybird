/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.sql.SQLException;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.resource.ResourceException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Bare-bones implementation of {@link javax.sql.ConnectionPoolDataSource}.
 * <p>
 * Use this class instead of the broken implementation of
 * {@link org.firebirdsql.pool.FBConnectionPoolDataSource}.
 * </p>
 * <p>
 * Please be aware that this is not a connectionpool. This class provides
 * PooledConnection objects for connection pool implementations (eg as provided
 * by a JEE application server). If you need a standalone connectionpool,
 * consider using a connectionpool implementation like c3p0, BoneCP or BDCP.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBConnectionPoolDataSource extends FBAbstractCommonDataSource implements ConnectionPoolDataSource, Referenceable {

    private volatile transient FBDataSource internalDs;

    public PooledConnection getPooledConnection() throws SQLException {
        return getPooledConnection(getUser(), getPassword());
    }

    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        if (internalDs == null) {
            initialize();
        }
        return new FBPooledConnection(internalDs.getConnection(user, password));
    }
    
    private void initialize() throws SQLException {
        synchronized (lock) {
            if (internalDs != null) {
                return;
            }
            try {
                GDSType gdsType = GDSType.getType(getType());
                if (gdsType == null) {
                    gdsType = GDSFactory.getDefaultGDSType();
                }
                FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(
                        gdsType, getConnectionProperties());
                internalDs = (FBDataSource) mcf.createConnectionFactory();
                internalDs.setLogWriter(getLogWriter());
            } catch (ResourceException e) {
                throw new FBSQLException(e);
            }
        }
    }

    protected void checkNotStarted() {
        if (internalDs != null) {
            throw new IllegalStateException("DataSource already in use. Change of this property is not allowed");
        }
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(getClass().getName(), DataSourceFactory.class.getName(), null);
        
        FBAbstractCommonDataSource.updateReference(ref, this);

        return ref;
    }
}
