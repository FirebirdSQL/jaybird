// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbService;

import java.sql.SQLWarning;

/**
 * @author Mark Rotteveel
 */
public final class ServiceListenerDispatcher extends AbstractListenerDispatcher<ServiceListener>
        implements ServiceListener {

    private static final System.Logger log = System.getLogger(ServiceListenerDispatcher.class.getName());

    @Override
    public void detaching(FbService service) {
        notify(listener -> listener.detaching(service), "detaching");
    }

    @Override
    public void detached(FbService service) {
        notify(listener -> listener.detached(service), "detached");
    }

    @Override
    public void warningReceived(FbService service, SQLWarning warning) {
        notify(listener -> listener.warningReceived(service, warning), "warningReceived");
    }

    @Override
    protected void logError(String message, Throwable throwable) {
        log.log(System.Logger.Level.ERROR, message, throwable);
    }
}
