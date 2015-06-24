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
package org.firebirdsql.ds;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Bare-bones implementation of {@link javax.sql.XADataSource}.
 * <p>
 * Use this class instead of the broken implementation of
 * {@link org.firebirdsql.pool.FBConnectionPoolDataSource}.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBXADataSource extends FBAbstractCommonDataSource implements XADataSource, Referenceable {

    private volatile transient FBDataSource internalDs;

    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(getUser(), getPassword());
    }

    public XAConnection getXAConnection(String user, String password) throws SQLException {
        if (internalDs == null) {
            initialize();
        }
        AbstractConnection connection = (AbstractConnection) internalDs.getConnection(user,
                password);
        return new FBXAConnection(connection);
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
                FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(gdsType,
                        getConnectionProperties());
                mcf.setDefaultConnectionManager(new XAConnectionManager());
                internalDs = (FBDataSource) mcf.createConnectionFactory();
                internalDs.setLogWriter(getLogWriter());
            } catch (ResourceException e) {
                throw new FBSQLException(e);
            }
        }
    }

    protected void checkNotStarted() throws IllegalStateException {
        if (internalDs != null) {
            throw new IllegalStateException("DataSource already in use. Change of this property is not allowed");
        }
    }

    private static class XAConnectionManager implements ConnectionManager, ConnectionEventListener {

        private static final long serialVersionUID = 2615167799315401379L;

        public Object allocateConnection(ManagedConnectionFactory mcf,
                ConnectionRequestInfo cxRequestInfo) throws ResourceException {

            FBManagedConnection mc = (FBManagedConnection) mcf.createManagedConnection(null, cxRequestInfo);
            mc.setManagedEnvironment(true);
            mc.setConnectionSharing(false);
            mc.addConnectionEventListener(this);
            return mc.getConnection(null, cxRequestInfo);
        }

        // javax.resource.spi.ConnectionEventListener implementation

        /**
         * <code>javax.resource.spi.ConnectionEventListener</code> callback for
         * when a <code>ManagedConnection</code> is closed.
         * 
         * @param ce
         *            contains information about the connection that has be
         *            closed
         */
        public void connectionClosed(ConnectionEvent ce) {
            PrintWriter externalLog = ((FBManagedConnection) ce.getSource()).getLogWriter();
            try {
                ((FBManagedConnection) ce.getSource()).destroy();
            } catch (ResourceException e) {
                if (externalLog != null)
                    externalLog.println("Exception closing unmanaged connection: " + e);
            }
        }

        /**
         * <code>javax.resource.spi.ConnectionEventListener</code> callback for
         * when a Local Transaction was rolled back within the context of a
         * <code>ManagedConnection</code>.
         * 
         * @param ce
         *            contains information about the connection
         */
        public void connectionErrorOccurred(ConnectionEvent ce) {
            PrintWriter externalLog = ((FBManagedConnection) ce.getSource()).getLogWriter();
            try {
                ((FBManagedConnection) ce.getSource()).destroy();
            } catch (ResourceException e) {
                if (externalLog != null)
                    externalLog.println("Exception closing unmanaged connection: " + e);
            }
        }

        // We are only supposed to be notified of local transactions that a
        // Connection started.
        // Not much we can do with this info...

        /**
         * Ignored event callback
         */
        public void localTransactionStarted(ConnectionEvent event) {
        }

        /**
         * Ignored event callback
         */
        public void localTransactionCommitted(ConnectionEvent event) {
        }

        /**
         * Ignored event callback
         */
        public void localTransactionRolledback(ConnectionEvent event) {
        }
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(getClass().getName(), DataSourceFactory.class.getName(), null);

        FBAbstractCommonDataSource.updateReference(ref, this);

        return ref;
    }

}
