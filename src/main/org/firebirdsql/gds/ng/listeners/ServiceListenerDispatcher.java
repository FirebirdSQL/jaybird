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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLWarning;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class ServiceListenerDispatcher extends AbstractListenerDispatcher<ServiceListener>
        implements ServiceListener {

    private static final Logger log = LoggerFactory.getLogger(ServiceListenerDispatcher.class);

    @Override
    public void detaching(FbService service) {
        for (ServiceListener listener : this) {
            try {
                listener.detaching(service);
            }  catch (Exception e) {
                log.error("Error on notify detaching to listener " + listener, e);
            }
        }
    }

    @Override
    public void detached(FbService service) {
        for (ServiceListener listener : this) {
            try {
                listener.detached(service);
            } catch (Exception e) {
                log.error("Error on notify detached to listener " + listener, e);
            }
        }
    }

    @Override
    public void warningReceived(FbService service, SQLWarning warning) {
        for (ServiceListener listener : this) {
            try {
                listener.warningReceived(service, warning);
            } catch (Exception e) {
                log.error("Error on notify warningReceived to listener " + listener, e);
            }
        }
    }
}
