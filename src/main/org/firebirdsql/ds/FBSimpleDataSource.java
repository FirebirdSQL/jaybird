/*
 SPDX-FileCopyrightText: Copyright 2002-2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPFX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2011-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.ds;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBDataSource;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.firebirdsql.jaybird.util.StringUtils.trimToNull;

/**
 * This is a simple implementation of {@link DataSource} interface. Connections
 * are physically opened in {@link DataSource#getConnection()} method and
 * physically closed in {@link Connection#close()} method.
 * <p>
 * If you need a standalone connection pool, consider using a connection pool implementation like HikariCP, c3p0 or
 * DBCP.
 * </p>
 *
 * @author Roman Rokytskyy
 * @author David Jencks
 */
public class FBSimpleDataSource extends AbstractConnectionPropertiesDataSource
        implements DataSource, Serializable, Referenceable {

    @Serial
    private static final long serialVersionUID = 3156578540634970427L;
    static final String REF_DESCRIPTION = "description";
    static final String REF_MCF = "mcf";

    protected final FBManagedConnectionFactory mcf;
    private final ReadWriteLock dsLock = new ReentrantReadWriteLock();
    protected transient FBDataSource ds;

    protected String description;

    /**
     * Creates an instance using the default GDS type (PURE_JAVA).
     */
    public FBSimpleDataSource() {
        this(GDSFactory.getDefaultGDSType());
    }

    /**
     * Creates an instance using the specified GDS type.
     *
     * @param type
     *         GDS type
     */
    public FBSimpleDataSource(GDSType type) {
        mcf = new FBManagedConnectionFactory(false, type);
    }

    /**
     * Creates an instance using an existing FBManagedConnectionFactory.
     *
     * @param mcf
     *         Managed connection factory
     * @see DataSourceFactory
     */
    FBSimpleDataSource(FBManagedConnectionFactory mcf) {
        this.mcf = mcf;
    }

    @Override
    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return mcf.getTransactionParameters(isolation);
    }

    @Override
    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        mcf.setTransactionParameters(isolation, tpb);
    }

    @Override
    public void setNonStandardProperty(String propertyMapping) {
        mcf.setNonStandardProperty(propertyMapping);
    }

    @Override
    public String getProperty(String name) {
        return mcf.getProperty(name);
    }

    @Override
    public void setProperty(String name, String value) {
        mcf.setProperty(name, value);
    }

    @Override
    public Integer getIntProperty(String name) {
        return mcf.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        mcf.setIntProperty(name, value);
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        return mcf.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        mcf.setBooleanProperty(name, value);
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return mcf.connectionPropertyValues();
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(FBSimpleDataSource.class.getName(), DataSourceFactory.class.getName(), null);
        ref.add(new StringRefAddr(REF_DESCRIPTION, getDescription()));
        byte[] data = DataSourceFactory.serialize(mcf);
        ref.add(new BinaryRefAddr(REF_MCF, data));
        return ref;
    }

    /**
     * Get JDBC connection with default credentials.
     *
     * @return new JDBC connection.
     * @throws SQLException
     *         if something went wrong.
     */
    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Get JDBC connection with the specified credentials.
     *
     * @param username
     *         username for the connection.
     * @param password
     *         password for the connection.
     * @return new JDBC connection.
     * @throws SQLException
     *         if something went wrong.
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    /**
     * Get description of this datasource.
     *
     * @return description of this datasource.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description of this datasource.
     *
     * @param description
     *         description of this datasource.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get underlying connection factory (an instance of {@link FBDataSource} class) that will provide JDBC connections.
     *
     * @return JDBC connection factory.
     * @throws SQLException
     *         if something went wrong.
     */
    @SuppressWarnings("java:S2583")
    protected DataSource getDataSource() throws SQLException {
        Lock readLock = dsLock.readLock();
        readLock.lock();
        try {
            if (ds != null) {
                return ds;
            }
        } finally {
            readLock.unlock();
        }
        Lock writeLock = dsLock.writeLock();
        writeLock.lock();
        try {
            if (ds != null) {
                return ds;
            }

            if (trimToNull(mcf.getDatabaseName()) == null) {
                throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_databasePathRequired);
            }
            return ds = (FBDataSource) mcf.createConnectionFactory();
        } finally {
            writeLock.unlock();
        }
    }

    // JDBC 4.0

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unableToUnwrap)
                    .messageParameter(iface != null ? iface.getName() : "(null)")
                    .toSQLException();
        }

        return iface.cast(this);
    }
}
