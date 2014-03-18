/*
 * $Id$
 * 
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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.AbstractFbDatabase;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.InfoProcessor;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbWireDatabase extends AbstractFbDatabase implements FbWireDatabase {

    protected final AtomicBoolean attached = new AtomicBoolean();
    protected final ProtocolDescriptor protocolDescriptor;
    protected final WireConnection connection;
    private final Object syncObject = new Object();

    /**
     * Creates an AbstractFbWireDatabase instance.
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

    /**
     * Gets the XdrInputStream.
     *
     * @return Instance of XdrInputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the InputStream
     */
    protected final XdrInputStream getXdrIn() throws SQLException {
        return getXdrStreamAccess().getXdrIn();
    }

    /**
     * Gets the XdrOutputStream.
     *
     * @return Instance of XdrOutputStream
     * @throws SQLException
     *         If no connection is opened or when exceptions occur
     *         retrieving the OutputStream
     */
    protected final XdrOutputStream getXdrOut() throws SQLException {
        return getXdrStreamAccess().getXdrOut();
    }

    @Override
    public final XdrStreamAccess getXdrStreamAccess() {
        return connection.getXdrStreamAccess();
    }

    @Override
    public final boolean isAttached() {
        return attached.get() && connection.isConnected();
    }

    @Override
    public FbBlob createBlobForOutput(FbTransaction transaction) throws SQLException {
        return protocolDescriptor.createOutputBlob(this, (FbWireTransaction) transaction);
    }

    @Override
    public FbBlob createBlobForInput(FbTransaction transaction, long blobId) throws SQLException {
        return protocolDescriptor.createInputBlob(this, (FbWireTransaction) transaction, blobId);
    }

    @Override
    public <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        byte[] responseBuffer = getDatabaseInfo(requestItems, bufferLength);
        return infoProcessor.process(responseBuffer);
    }
}
