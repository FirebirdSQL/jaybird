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

import java.util.EventObject;

/**
 * Event object for object pool events.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public class PooledObjectEvent extends EventObject {
    
    private boolean connectionDeallocated;
    
	/**
     * Create instance of this object for the specified event source. This is
     * a shortcut call for <code>PooledObjectEvent(Object, false)</code>.
     * 
	 * @param eventSource event source.
	 */
	public PooledObjectEvent(Object eventSource) {
		this(eventSource, false);
	}
    
    /**
     * Create instance of this object for the specified event source and 
     * information if the physical connection is deallocated. 
     * 
     * @param eventSource event source.
     * 
     * @param connectionDeallocated <code>true</code> if physical connection
     * is closed.
     */
    public PooledObjectEvent(Object eventSource, boolean connectionDeallocated) {
        super(eventSource);
        
        this.connectionDeallocated = connectionDeallocated;
    }

    /**
     * Check if the physical connection that generated this event is deallocated.
     * 
     * @return <code>true</code> if connection was deallocated.
     */
    public boolean isDeallocated() {
        return connectionDeallocated;
    }
}
