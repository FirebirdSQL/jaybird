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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Native;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.registerNativeResource;

/**
 * Common implementation for client library and embedded database factory.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractNativeDatabaseFactory implements FbDatabaseFactory {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private FbClientResource resource;

    @Override
    public JnaDatabase connect(IConnectionProperties connectionProperties) throws SQLException {
        try {
            final JnaDatabaseConnection jnaDatabaseConnection = new JnaDatabaseConnection(getClientLibrary(),
                    filterProperties(connectionProperties));
            return jnaDatabaseConnection.identify();
        } catch (NativeLibraryLoadException e) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_failedToLoadNativeLibrary)
                    .cause(e)
                    .toFlatSQLException();
        }
    }

    @Override
    public JnaService serviceConnect(IServiceProperties serviceProperties) throws SQLException {
        try {
            final JnaServiceConnection jnaServiceConnection = new JnaServiceConnection(getClientLibrary(),
                    filterProperties(serviceProperties));
            return jnaServiceConnection.identify();
        } catch (NativeLibraryLoadException e) {
            throw new FbExceptionBuilder()
                    .nonTransientConnectionException(JaybirdErrorCodes.jb_failedToLoadNativeLibrary)
                    .cause(e)
                    .toFlatSQLException();
        }
    }

    /**
     * Gets the current instance of the client library.
     * <p>
     * Most implementations should <b>not</b> override this method (contrary to previous versions of Jaybird), to ensure
     * native libraries are properly disposed of.
     * </p>
     *
     * @return Client library instance.
     */
    protected FbClientLibrary getClientLibrary() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        if (resource == null) {
            readLock.unlock();
            Lock writeLock = rwLock.writeLock();
            writeLock.lock();
            try {
                if (resource == null) {
                    FbClientLibrary newLibrary = syncWrapIfNecessary(createClientLibrary());
                    resource = registerNativeResource(new FbClientResource(newLibrary, this));
                }
                readLock.lock();
            } finally {
                writeLock.unlock();
            }
        }
        try {
            return resource.get();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Creates and initialize a new instance of the client library.
     * <p>
     * Instances should not be cached (caching - if any - is the responsibility of the caller).
     * </p>
     *
     * @return new client library instance
     * @since 4.0
     */
    protected abstract FbClientLibrary createClientLibrary();

    /**
     * Called when a resource registered by this factory is disposed.
     *
     * @param disposedResource client resource to dispose
     * @param disposeAction Dispose action to run if {@code disposedResource} matches the current resource
     * @since 4.0
     */
    final void disposing(FbClientResource disposedResource, Runnable disposeAction) {
        if (disposedResource == null) {
            throw new IllegalStateException("disposedResource was null");
        }
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            if (resource != disposedResource) {
                throw new IllegalStateException("disposedResource does not match current resource");
            }
        } finally {
            readLock.unlock();
        }
        // NOTE: we accept the potential race that may occur here between the read and write lock
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            resource = null;
            disposeAction.run();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Allows the database factory to perform modification of the attach properties before use.
     * <p>
     * Implementations should be prepared to handle immutable attach properties. Implementations are strongly
     * advised to copy the attach properties before modification and return this copy.
     * </p>
     *
     * @param attachProperties Attach properties
     * @param <T> Type of attach properties
     * @return Filtered properties
     */
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        return attachProperties;
    }

    private static FbClientLibrary syncWrapIfNecessary(FbClientLibrary clientLibrary) {
        if (JaybirdSystemProperties.isSyncWrapNativeLibrary()) {
            return (FbClientLibrary) Native.synchronizedLibrary(clientLibrary);
        }
        return clientLibrary;
    }

}
