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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSServerVersionException;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbDatabase implements FbDatabase {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbDatabase.class, false);

    protected final DatabaseListenerDispatcher databaseListenerDispatcher = new DatabaseListenerDispatcher();
    private final WarningMessageCallback warningCallback = new WarningMessageCallback() {
        @Override
        public void processWarning(SQLWarning warning) {
            databaseListenerDispatcher.warningReceived(AbstractFbDatabase.this, warning);
        }
    };
    private short databaseDialect;
    private int odsMajor;
    private int odsMinor;
    private GDSServerVersion serverVersion;

    /**
     * @return The warning callback for this database.
     */
    protected final WarningMessageCallback getDatabaseWarningCallback() {
        return warningCallback;
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

    @Override
    public final void addDatabaseListener(DatabaseListener listener) {
        databaseListenerDispatcher.addListener(listener);
    }

    @Override
    public final void removeDatabaseListener(DatabaseListener listener) {
        databaseListenerDispatcher.removeListener(listener);
    }

    /**
     * Checks if the database is connected, and throws a {@link SQLException} if it isn't connected.
     */
    protected abstract void checkConnected() throws SQLException;

    /**
     * Actual implementation of database detach.
     * <p>
     * Implementations of this method should only be called from {@link #detach()}, and should <strong>not</strong> notify database
     * listeners of the database {@link DatabaseListener#detaching(FbDatabase)} and
     * {@link DatabaseListener#detached(FbDatabase)} events.
     * </p>
     */
    protected abstract void internalDetach() throws SQLException;

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Calls {@link #checkConnected()} and notifies database listeners of the detaching event, then
     * calls {@link #internalDetach()} and finally notifies database listeners of database detach and removes all listeners.
     * </p>
     */
    @Override
    public final void detach() throws SQLException {
        checkConnected();
        synchronized (getSynchronizationObject()) {
            databaseListenerDispatcher.detaching(this);
            try {
                internalDetach();
            } finally {
                databaseListenerDispatcher.detached(this);
                databaseListenerDispatcher.shutdown();
            }
        }
    }

    @Override
    public final int getOdsMajor() {
        return odsMajor;
    }

    /**
     * Sets the ODS (On Disk Structure) major version of the database associated
     * with this connection.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param odsMajor
     *         ODS major version
     */
    protected final void setOdsMajor(int odsMajor) {
        this.odsMajor = odsMajor;
    }

    @Override
    public final int getOdsMinor() {
        return odsMinor;
    }

    /**
     * Sets the ODS (On Disk Structure) minor version of the database associated
     * with this connection.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param odsMinor
     *         The ODS minor version
     */
    protected final void setOdsMinor(int odsMinor) {
        this.odsMinor = odsMinor;
    }

    @Override
    public final GDSServerVersion getServerVersion() {
        return serverVersion;
    }

    /**
     * Sets the Firebird version string.
     * <p>
     * This method should only be called by this instance.
     * </p>
     *
     * @param versionString
     *         Raw version string
     */
    protected final void setServerVersion(String versionString) {
        try {
            serverVersion = GDSServerVersion.parseRawVersion(versionString);
        } catch (GDSServerVersionException e) {
            log.error(String.format("Received unsupported server version \"%s\", replacing with dummy invalid version ", versionString), e);
            serverVersion = GDSServerVersion.INVALID_VERSION;
        }
    }

    @Override
    public <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        byte[] responseBuffer = getDatabaseInfo(requestItems, bufferLength);
        return infoProcessor.process(responseBuffer);
    }

    @Override
    public int iscVaxInteger(final byte[] buffer, final int startPosition, int length) {
        if (length > 4) {
            return 0;
        }
        int value = 0;
        int shift = 0;

        int index = startPosition;
        while (--length >= 0) {
            value += (buffer[index++] & 0xff) << shift;
            shift += 8;
        }
        return value;
    }

    @Override
    public long iscVaxLong(final byte[] buffer, final int startPosition, int length) {
        if (length > 8) {
            return 0;
        }
        long value = 0;
        int shift = 0;

        int index = startPosition;
        while (--length >= 0) {
            value += (buffer[index++] & 0xffL) << shift;
            shift += 8;
        }
        return value;
    }

    @Override
    public int iscVaxInteger2(final byte[] buffer, final int startPosition) {
        return (buffer[startPosition] & 0xff) | ((buffer[startPosition + 1] & 0xff) << 8);
    }
}
