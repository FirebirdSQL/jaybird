/*
 * Firebird Open Source JDBC Driver
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

/**
 * Direction of fetch.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public enum FetchDirection {

    /**
     * Fetch forward (to end of cursor).
     */
    FORWARD,
    /**
     * Fetch reverse (to start of cursor).
     */
    REVERSE,
    /**
     * Fetch in place (doesn't change position).
     */
    IN_PLACE,
    /**
     * Fetch direction unknown (e.g. fetch FIRST or LAST).
     */
    UNKNOWN

}
