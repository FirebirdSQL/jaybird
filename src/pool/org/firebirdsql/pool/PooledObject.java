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

/**
 * Represents an object that can be stored in the pool.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public interface PooledObject {

    final long INSTANT_IN_USE = -1L;

    /**
     * Deallocate this object. This method deallocated the object
     * and releases all associated resources. This method is invoked when
     * object pool is shutdown and is needed to gracefully release resources.
     */
    void deallocate();
    
    /**
     * Check if this pooled object is still valid.
     * 
     * @return <code>true</code> when the object is valid.
     */
    boolean isValid();
    
    /**
     * Check whether this object is currently in pool or had been released
     * to the application.
     * 
     * @return <code>true</code> if the object is currently in pool. 
     */
    boolean isInPool();
    
    /**
     * Set the "inPool" flag to this object. This method should be called only
     * by the pool implementation.
     * 
     * @param inPool <code>true</code> if object is in pool, otherwise 
     * <code>false</code>.
     */
    void setInPool(boolean inPool);

    /**
     * @return The instant in time when this object was last used, or {@link #INSTANT_IN_USE} when object is currently in use.
     */
    long getInstantInPool();

    PooledConnectionQueue getOwningQueue();
}
