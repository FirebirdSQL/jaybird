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
 * This interface represents a connection that can be pinged. Pinging JDBC
 * connection is necessary when it is needed to check if connection is still
 * alive or not.
 * 
 *  @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
interface XPingableConnection {
	
	/**
	 * Ping this connection. This method checks if connection is alive and
	 * can be used further.
	 * 
	 * @return <code>true</code> if connection is alive and can be used further,
	 * otherwise <code>false</code>.
	 */
	boolean ping();
	
	/**
	 * Get the time when connection was pinged last time.
	 * 
	 * @return time of last ping time.
	 */
	long getLastPingTime();
	
}