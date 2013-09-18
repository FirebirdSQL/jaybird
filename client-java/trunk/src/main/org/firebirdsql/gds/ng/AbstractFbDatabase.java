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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher;

import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public abstract class AbstractFbDatabase implements FbDatabase {

    protected final DatabaseListenerDispatcher databaseListenerDispatcher = new DatabaseListenerDispatcher();
    private final WarningMessageCallback warningCallback = new WarningMessageCallback() {
        @Override
        public void processWarning(SQLWarning warning) {
            databaseListenerDispatcher.warningReceived(AbstractFbDatabase.this, warning);
        }
    };
    private short databaseDialect;

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
        // TODO Don't register if closed?
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
     * Implementations of this method should only be called from {@link #detach()}, and should not notify database
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
                databaseListenerDispatcher.removeAllListeners();
            }
        }
    }

    // TODO Unregister all listeners on close
}
