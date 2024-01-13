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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.NativeLibrary;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.Logger.Level.TRACE;
import static org.firebirdsql.gds.ng.jna.NativeResourceTracker.registerNativeResource;

/**
 * Common implementation for client library and embedded database factory.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractNativeDatabaseFactory implements FbDatabaseFactory {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private FbClientResource resource;

    @Override
    public JnaDatabase connect(IConnectionProperties connectionProperties) throws SQLException {
        configureSearchPath(connectionProperties);
        try {
            final JnaDatabaseConnection jnaDatabaseConnection = new JnaDatabaseConnection(getClientLibrary(),
                    filterProperties(connectionProperties));
            return jnaDatabaseConnection.identify();
        } catch (NativeLibraryLoadException e) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_failedToLoadNativeLibrary)
                    .cause(e)
                    .toSQLException();
        }
    }

    @Override
    public JnaService serviceConnect(IServiceProperties serviceProperties) throws SQLException {
        configureSearchPath(serviceProperties);
        try {
            final JnaServiceConnection jnaServiceConnection = new JnaServiceConnection(getClientLibrary(),
                    filterProperties(serviceProperties));
            return jnaServiceConnection.identify();
        } catch (NativeLibraryLoadException e) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_failedToLoadNativeLibrary)
                    .cause(e)
                    .toSQLException();
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
    @SuppressWarnings("java:S2589")
    protected FbClientLibrary getClientLibrary() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        if (resource == null) {
            readLock.unlock();
            Lock writeLock = rwLock.writeLock();
            writeLock.lock();
            try {
                if (resource == null) {
                    FbClientLibrary newLibrary = FbClientFeatureAccessHandler.decorateWithFeatureAccess(createClientLibrary());
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
     * @param disposedResource
     *         client resource to dispose
     * @param disposeAction
     *         Dispose action to run if {@code disposedResource} matches the current resource
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
     * @param attachProperties
     *         Attach properties
     * @param <T>
     *         Type of attach properties
     * @return Filtered properties
     */
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        return attachProperties;
    }

    /**
     * @return the default library names loaded by this factory
     */
    protected abstract Collection<String> defaultLibraryNames();

    private void configureSearchPath(IAttachProperties<?> connectionProperties) {
        // library already loaded (will check again under read lock below)
        if (resource != null) return;
        // NOTE: Although this configuration happens per native database factory, in practice the first one used "wins"
        String nativeLibraryPath = connectionProperties.getProperty(NativePropertyNames.nativeLibraryPath);
        if (nativeLibraryPath == null || nativeLibraryPath.isBlank()) return;
        String pathForJna = resolvePathForJna(nativeLibraryPath);
        if (pathForJna == null) return;

        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            if (resource != null) return;
            for (String library : defaultLibraryNames()) {
                NativeLibrary.addSearchPath(library, pathForJna);
            }
        } finally {
            readLock.unlock();
        }
    }

    private String resolvePathForJna(String nativeLibraryPath) {
        var log = System.getLogger(getClass().getName());
        try {
            Path actualPath = Path.of(nativeLibraryPath).toAbsolutePath();
            if (Files.isRegularFile(actualPath)) {
                actualPath = actualPath.getParent();
                log.log(TRACE, "nativeLibraryPath ''{0}'' was a file, using parent ''{1}''", nativeLibraryPath,
                        actualPath);
            }
            if (!Files.isDirectory(actualPath)) {
                log.log(TRACE, "nativeLibraryPath ''{0}'' does not resolve to an existing directory ({1})",
                        nativeLibraryPath, actualPath);
                return null;
            }
            log.log(TRACE, "resolved nativeLibraryPath ''{0}'' to path ''{1}''", nativeLibraryPath, actualPath);
            return actualPath.toString();
        } catch (InvalidPathException e) {
            log.log(TRACE, "nativeLibraryPath ''{0}'' is not a valid path: {1}", nativeLibraryPath, e);
            return null;
        }
    }

}
