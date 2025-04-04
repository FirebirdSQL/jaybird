// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.xca.*;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FirebirdConnection;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Bare-bones implementation of {@link javax.sql.XADataSource}.
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
public class FBXADataSource extends FBAbstractCommonDataSource implements XADataSource, Referenceable {

    // TODO Implement in terms of FBManagedConnectionFactory

    private static final System.Logger LOG = System.getLogger(FBXADataSource.class.getName());

    @SuppressWarnings("java:S3077")
    private volatile FBDataSource internalDs;

    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(getUser(), getPassword());
    }

    public XAConnection getXAConnection(String user, String password) throws SQLException {
        if (internalDs == null) {
            initialize();
        }
        FBConnection connection = (FBConnection) internalDs.getConnection(user, password);
        return new FBXAConnection(connection);
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
            FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(gdsType,
                    getConnectionProperties());
            mcf.setDefaultConnectionManager(new XAConnectionManager());
            internalDs = (FBDataSource) mcf.createConnectionFactory();
            internalDs.setLogWriter(getLogWriter());
        }
    }

    protected void checkNotStarted() throws IllegalStateException {
        if (internalDs != null) {
            throw new IllegalStateException("DataSource already in use. Change of this property is not allowed");
        }
    }

    private static class XAConnectionManager implements XcaConnectionManager, XcaConnectionEventListener,
            Serializable {

        @Serial
        private static final long serialVersionUID = 7926533334548378200L;

        @Override
        public FirebirdConnection allocateConnection(FBManagedConnectionFactory mcf,
                FBConnectionRequestInfo cxRequestInfo) throws SQLException {
            FBManagedConnection mc = mcf.createManagedConnection(cxRequestInfo);
            mc.setManagedEnvironment(true);
            mc.addConnectionEventListener(this);
            return mc.getConnection();
        }

        @Override
        public void connectionClosed(XcaConnectionEvent ce) {
            destroyConnection(ce);
        }

        @Override
        public void connectionErrorOccurred(XcaConnectionEvent ce) {
            destroyConnection(ce);
        }

        private void destroyConnection(XcaConnectionEvent ce) {
            FBManagedConnection mc = ce.getSource();
            try {
                mc.destroy(ce);
            } catch (SQLException e) {
                LOG.log(System.Logger.Level.WARNING, "Ignored exception closing unmanaged connection", e);
            } finally {
                mc.removeConnectionEventListener(this);
            }
        }
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(FBXADataSource.class.getName(), DataSourceFactory.class.getName(), null);

        FBAbstractCommonDataSource.updateReference(ref, this);

        return ref;
    }

}
