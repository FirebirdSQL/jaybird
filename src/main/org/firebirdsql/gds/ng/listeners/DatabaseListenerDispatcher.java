// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbDatabase;

import java.sql.SQLWarning;

/**
 * Dispatcher to maintain and notify other {@link DatabaseListener}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class DatabaseListenerDispatcher extends AbstractListenerDispatcher<DatabaseListener>
        implements DatabaseListener {

    private static final System.Logger log = System.getLogger(DatabaseListenerDispatcher.class.getName());

    @Override
    public void detaching(FbDatabase database) {
        notify(listener -> listener.detaching(database), "detaching");
    }

    @Override
    public void detached(FbDatabase database) {
        notify(listener -> listener.detached(database), "detached");
    }

    @Override
    public void warningReceived(FbDatabase database, SQLWarning warning) {
        notify(listener -> listener.warningReceived(database, warning), "warningReceived");
    }

    @Override
    protected void logError(String message, Throwable throwable) {
        log.log(System.Logger.Level.ERROR, message, throwable);
    }
}
