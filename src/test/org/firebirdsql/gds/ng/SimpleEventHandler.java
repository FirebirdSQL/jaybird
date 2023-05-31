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

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author Mark Rotteveel
*/
public class SimpleEventHandler implements EventHandler {

    private final List<EventHandle> receivedEventHandles = Collections.synchronizedList(new ArrayList<EventHandle>());

    @Override
    public void eventOccurred(EventHandle eventHandle) {
        receivedEventHandles.add(eventHandle);
    }

    public List<EventHandle> getReceivedEventHandles() {
        synchronized (receivedEventHandles) {
            return new ArrayList<EventHandle>(receivedEventHandles);
        }
    }

    public void clearEvents() {
        receivedEventHandles.clear();
    }
}
