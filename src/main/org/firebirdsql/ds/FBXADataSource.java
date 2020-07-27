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
package org.firebirdsql.ds;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jaybird.xca.*;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Bare-bones implementation of {@link javax.sql.XADataSource}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBXADataSource extends FBAbstractCommonDataSource implements XADataSource, Referenceable {

    // TODO Implement in terms of FBManagedConnectionFactory

    private static final Logger LOG = LoggerFactory.getLogger(FBXADataSource.class);

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
        synchronized (lock) {
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
            try {
                ce.getSource().destroy(ce);
            } catch (SQLException e) {
                LOG.warn("Exception closing unmanaged connection", e);
            }
        }

        @Override
        public void connectionErrorOccurred(XcaConnectionEvent ce) {
            try {
                ce.getSource().destroy(ce);
            } catch (SQLException e) {
                LOG.warn("Exception closing unmanaged connection", e);
            }
        }
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(FBXADataSource.class.getName(), DataSourceFactory.class.getName(), null);

        FBAbstractCommonDataSource.updateReference(ref, this);

        return ref;
    }

}
