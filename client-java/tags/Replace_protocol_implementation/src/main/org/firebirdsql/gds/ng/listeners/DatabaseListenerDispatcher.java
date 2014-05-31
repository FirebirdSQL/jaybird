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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbDatabase;

import java.sql.SQLWarning;

/**
 * Dispatcher to maintain and notify other {@link DatabaseListener}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class DatabaseListenerDispatcher extends AbstractListenerDispatcher<DatabaseListener> implements DatabaseListener {

    @Override
    public void detaching(FbDatabase database) {
        for (DatabaseListener listener : this) {
            try {
                listener.detaching(database);
            } catch (Exception e) {
                // Ignore // TODO: log
            }
        }
    }

    @Override
    public void detached(FbDatabase database) {
        for (DatabaseListener listener : this) {
            try {
                listener.detached(database);
            } catch (Exception e) {
                // Ignore // TODO: log
            }
        }
    }

    @Override
    public void warningReceived(FbDatabase database, SQLWarning warning) {
        for (DatabaseListener listener : this) {
            try {
                listener.warningReceived(database, warning);
            } catch (Exception e) {
                // Ignore // TODO: log
            }
        }
    }
}
