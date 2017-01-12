/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.pool;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;


/**
 * Event listener that is notified about the events in the {@link javax.sql.PooledConnection}.
 *  
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public interface PooledConnectionEventListener extends ConnectionEventListener {

    /**
     * Notify listener that {@link javax.sql.PooledConnection#close()} method
     * is directly invoked by the application.
     * 
     * @param event instance of {@link ConnectionEvent} containing the instance 
     * of {@link javax.sql.PooledConnection} that generated this event. 
     */
    void physicalConnectionClosed(ConnectionEvent event);
    
    
    /**
     * Notify listener that {@link PooledObject#deallocate()} method
     * is invoked by the application.
     * 
     * @param event instance of {@link ConnectionEvent} containing the instance 
     * of {@link PooledObject} that generated this event. 
     */
    void physicalConnectionDeallocated(ConnectionEvent event);
}
