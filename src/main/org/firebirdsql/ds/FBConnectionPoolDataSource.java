// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDataSource;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.sql.SQLException;

/**
 * Bare-bones implementation of {@link javax.sql.ConnectionPoolDataSource}.
 * <p>
 * Please be aware that this is not a connectionpool. This class provides
 * PooledConnection objects for connection pool implementations (eg as provided
 * by a JEE application server). If you need a standalone connectionpool,
 * consider using a connectionpool implementation like HikariCP, c3p0 or DBCP.
 * </p>
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
public class FBConnectionPoolDataSource extends FBAbstractCommonDataSource implements ConnectionPoolDataSource,
        Referenceable {

    // TODO Implement in terms of FBManagedConnectionFactory
    
    @SuppressWarnings("java:S3077")
    private volatile FBDataSource internalDs;

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
        try (LockCloseable ignored = withLock()) {
            if (internalDs != null) {
                return;
            }
            GDSType gdsType = GDSType.getType(getType());
            if (gdsType == null) {
                gdsType = GDSFactory.getDefaultGDSType();
            }
            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(gdsType, getConnectionProperties());
            internalDs = (FBDataSource) mcf.createConnectionFactory();
            internalDs.setLogWriter(getLogWriter());
        }
    }

    protected void checkNotStarted() {
        if (internalDs != null) {
            throw new IllegalStateException("DataSource already in use. Change of this property is not allowed");
        }
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(FBConnectionPoolDataSource.class.getName(),
                DataSourceFactory.class.getName(), null);
        
        FBAbstractCommonDataSource.updateReference(ref, this);

        return ref;
    }
}
