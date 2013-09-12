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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public abstract class AbstractFbWireDatabase implements FbWireDatabase {

    protected final AtomicBoolean attached = new AtomicBoolean();
    protected final ProtocolDescriptor protocolDescriptor;
    protected final WireConnection connection;
    private final XdrStreamHolder xdrStreamHolder;
    private final Object syncObject = new Object();
    private short databaseDialect;

    /**
     * Creates a V10Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected AbstractFbWireDatabase(WireConnection connection, ProtocolDescriptor descriptor) {
        if (connection == null) throw new IllegalArgumentException("parameter connection should be non-null");
        if (descriptor == null) throw new IllegalArgumentException("parameter descriptor should be non-null");
        this.connection = connection;
        xdrStreamHolder = new XdrStreamHolder(connection);
        protocolDescriptor = descriptor;
    }

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public final short getConnectionDialect() {
        return connection.getConnectionDialect();
    }

    @Override
    public final IEncodingFactory getEncodingFactory() {
        return connection.getEncodingFactory();
    }

    @Override
    public final Encoding getEncoding() {
        return connection.getEncoding();
    }

    @Override
    public final XdrInputStream getXdrIn() throws SQLException {
        return xdrStreamHolder.getXdrIn();
    }

    @Override
    public final XdrOutputStream getXdrOut() throws SQLException {
        return xdrStreamHolder.getXdrOut();
    }

    @Override
    public final boolean isAttached() {
        return attached.get() && connection.isConnected();
    }

    @Override
    public final short getDatabaseDialect() {
        return databaseDialect;
    }

    /**
     * Sets the dialect of the database.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param dialect
     *         Dialect of the database/connection
     */
    protected final void setDatabaseDialect(short dialect) {
        this.databaseDialect = dialect;
    }
}
